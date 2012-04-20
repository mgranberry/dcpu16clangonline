/* Copyright 2012 Matthias Granberry

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os/exec"
)

type ClangHandler struct {
	ClangPath *string
	OptFlags  *string
}

type SingleFileRequest struct {
	Filename *string
	Contents *string
}

type CompilerInput interface {
	GetFilename() string
	GetContents() *string
	writeSource(w io.WriteCloser)
}

type JsonCompilerInput struct {
	Filename *string
	Contents *string
}

func (c *JsonCompilerInput) GetFilename() string {
	return *c.Filename
}

func (c *JsonCompilerInput) GetContents() *string {
	return c.Contents
}

func parseJson(input io.Reader) (*JsonCompilerInput, error) {
	body, err := ioutil.ReadAll(input)
	if err != nil {
		return nil, err
	}
	jsonInput := JsonCompilerInput{}
	err = json.Unmarshal(body, &jsonInput)
	if err != nil {
		return nil, err
	}
	return &jsonInput, nil
}

func emitJson(output *CompilerOutput) ([]byte, error) {
	return json.Marshal(*output)
}

type CompilerOutput struct {
	Assembly    string
	ErrorOutput string
	Filename    string
}

func (c *JsonCompilerInput) writeSource(w io.WriteCloser) {
	w.Write([]byte(*c.GetContents()))
	w.Close()
}

func (c *CompilerOutput) readAssembly(r io.Reader) {
	bytes, err := ioutil.ReadAll(r)
	if err != nil {
		fmt.Println(err)
	}
	c.Assembly = string(bytes)
}

func (c *CompilerOutput) readErrorOutput(r io.Reader) {
	bytes, err := ioutil.ReadAll(r)
	if err != nil {
		fmt.Println(err)
	}
	c.ErrorOutput = string(bytes)
}

type Compiler interface {
	Compile(ci CompilerInput) *CompilerOutput
}

type ClangCompiler struct {
	path     *string
	optflags *string
}

func (c *ClangCompiler) Compile(ci CompilerInput) *CompilerOutput {
	command := exec.Command(*c.path, "-x", "c", "-ccc-host-triple", "dcpu16", "-S", *c.optflags, "-", "-o", "-")
	stdin, err := command.StdinPipe()
	if err != nil {
		fmt.Println("error opening command stdin pipe:", err)
	}
	stdout, err := command.StdoutPipe()
	if err != nil {
		fmt.Println("error opening command stdout pipe:", err)
	}
	stderr, err := command.StderrPipe()
	if err != nil {
		fmt.Println("error opening command stderr pipe:", err)
	}
	var output CompilerOutput

	fmt.Println("starting command")
	go ci.writeSource(stdin)

	err = command.Start()
	if err != nil {
		fmt.Println("Error running command:\n", err)
	}
	go output.readAssembly(stdout)
	go output.readErrorOutput(stderr)
	output.Filename = ci.GetFilename()
	err = command.Wait()
	if err != nil {
		//fmt.Println("Error awaiting command: %s\n", err)
	}
	fmt.Println("finished command")
	return &output
}

func (c ClangHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	input, err := parseJson(r.Body)
	fmt.Println(r.RemoteAddr)
	fmt.Println(*input.Contents)
	if err != nil {
		fmt.Println("error parsing JSON:", err)
	}
	compiler := ClangCompiler{c.ClangPath, c.OptFlags}
	cout := compiler.Compile(input)
	output, err := emitJson(cout)
	w.Write(output)
	//rstr := "{\"Filename\":\"test.c\",\"Contents\":\"int main(){while(1);}\"}"

	//command := exec.Command("/bin/sleep", "20")
}

func main() {

	port := flag.Int("port", 8080, "HTTP port")
	httpDir := flag.String("html", "war", "Directory to serve HTML from")
	bindAddress := flag.String("listen", "0.0.0.0", "Address to bind to")
	clangPath := flag.String("clang", "/usr/local/bin/clang", "Path to clang")
	clangFlags := flag.String("oflag", "-O1", "Optimization flags")
	flag.Parse()
	handler := ClangHandler{clangPath, clangFlags}

	http.Handle("/compile", handler)
	http.Handle("/", http.FileServer(http.Dir(*httpDir)))

	err := http.ListenAndServe(fmt.Sprintf("%s:%d", *bindAddress, *port), nil)
	if err != nil {
		fmt.Println(err)
	}
}

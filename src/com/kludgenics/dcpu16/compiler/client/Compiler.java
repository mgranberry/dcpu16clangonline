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

package com.kludgenics.dcpu16.compiler.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalSplitPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Compiler implements EntryPoint {
	TextArea compilerOutput;
	TextArea assemblyArea;
	TextArea codeArea;

	RequestCallback callback = new RequestCallback() {

		@Override
		public void onResponseReceived(Request request, Response response) {
			JSONValue value = JSONParser.parseLenient(response.getText());
			JSONObject obj = (JSONObject) value;
			JSONString str =(JSONString)obj.get("Assembly");
			assemblyArea.setText(str.stringValue());
			str = (JSONString)obj.get("ErrorOutput");
			compilerOutput.setText(str.stringValue());
			
		}

		@Override
		public void onError(Request request, Throwable exception) {
			assemblyArea.setText("Error while contacting compile server.");
			compilerOutput.setText("Error while contacting compile server.");
		}
		
	};
	
	public void onModuleLoad() {
		RootPanel rootPanel = RootPanel.get("app");
		//rootPanel.setSize("100%", "100%");
		DockLayoutPanel panel = new DockLayoutPanel(Unit.PCT);
		codeArea = new TextArea();

		codeArea.setText(
"int fib(int n) {\n" +
"  int cur = 1;\n" +
"  int prev = 1;\n" +
"  for (int i = 0; i < n; i++) {\n" +
"    int next = cur+prev;\n" +
"    prev = cur;\n" +
"    cur = next;\n" +
"  }\n" +
"  return cur;\n" +
"}\n"+
"\n"+
"int main(void) {\n" +
"  return fib(5)+fib(6);\n" +
"}\n");

		DockLayoutPanel outputPanel = new DockLayoutPanel(Unit.EM);
		ListBox compiler = new ListBox();
		compiler.addItem("Clang/LLVM");
		compiler.setWidth("100%");
		outputPanel.addNorth(compiler, 2);
		assemblyArea = new TextArea();
		assemblyArea.setText("Asm");
		assemblyArea.setSize("98%", "98%");
		Button compileButton = new Button();
		compileButton.setText("Compile");
		compileButton.setWidth("100%");
		outputPanel.addSouth(compileButton, 2);
		outputPanel.add(assemblyArea);
		panel.addEast(outputPanel, 30);
		outputPanel.setSize("98%", "98%");
		
		compilerOutput = new TextArea();
		compilerOutput.setReadOnly(true);
		compilerOutput.setText("Compiler");
		//panel.addWest(codeArea, 800);
		panel.addSouth(compilerOutput, 20);
		compilerOutput.setSize("98%", "98%");
		panel.add(codeArea);
		codeArea.setSize("98%", "98%");
		rootPanel.add(panel);
		panel.setSize("100%", "100%");
		
		compileButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				try {
					compilerOutput.setText("Compiling...");
					assemblyArea.setText("Compiling...");
					CompileService.compile(null, "test.c", codeArea.getText(), callback);
					
				} catch (RequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		
		try {
			CompileService.compile(null, "test.c", codeArea.getText(), callback);
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//panel.setSize("440", "300");
	}
}

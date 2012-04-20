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

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class CompileService {
	static final String COMPILE_URL = "/compile";
	
	public static void compile(String compiler, String filename, String body, RequestCallback callback) throws RequestException {
		JSONObject requestData = new JSONObject();
		requestData.put("filename", new JSONString(filename));
		requestData.put("contents", new JSONString(body));
		
		// Send request to server and catch any errors.
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, COMPILE_URL);

	    builder.sendRequest(requestData.toString(), callback);
	   
	}


}

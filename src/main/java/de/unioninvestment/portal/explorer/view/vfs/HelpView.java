/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.unioninvestment.portal.explorer.view.vfs;

import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;;

public class HelpView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	public HelpView() {
		final RichTextArea rtarea = new RichTextArea();
		
		rtarea.setCaption("VFSFileExplorerPortlet");

		rtarea.setValue("<h1>Configuration Example</h1>\n" +
		    "<h2>File access</h2>\n" +
		    "windows : file:///C:/Temp\n" +
		    "unix: file:///home/someuser/somedir\n" +
		    "\n" +
		    "<h2>FTP access</h2>\n" +
		    "ftp://hostname[: port]\n " +
		    "plus username and password\n" +
		    "\n" +
		    "<h2>SFTP access</h2>\n" +
		    "sftp://hostname[: port][ absolute-path]\n " +		    
		    "plus username and password \n" +
		    "and / or\n " +
		    "path to keyfile like /home/user/keyfile/id_rsa ");
		rtarea.setReadOnly(true);
		addComponent(rtarea);
	}

}

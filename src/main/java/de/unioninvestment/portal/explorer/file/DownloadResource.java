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
package de.unioninvestment.portal.explorer.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;

@SuppressWarnings("serial")
public class DownloadResource extends StreamResource {

	private final String filename;

	public DownloadResource(File fileToDownload, Application application) throws FileNotFoundException {
		super(new FileStreamResource(fileToDownload), fileToDownload.getName(), application);

		this.filename = fileToDownload.getName();
	}

	public DownloadStream getStream() {

		DownloadStream stream = null;

		if (filename.endsWith(".pdf"))
			stream = new DownloadStream(getStreamSource().getStream(), "application/pdf", filename);
		if (filename.contains(".xls"))
			stream = new DownloadStream(getStreamSource().getStream(), "application/xls", filename);
		if (filename.endsWith(".zip"))
			stream = new DownloadStream(getStreamSource().getStream(), "application/zip", filename);
		else
			stream = new DownloadStream(getStreamSource().getStream(), "application/octet-stream", filename);
		stream.setParameter("Content-Disposition", "attachment;filename=" + filename);
		return stream;
	}

	private static class FileStreamResource implements StreamResource.StreamSource, Serializable {

		private final InputStream inputStream;

		public FileStreamResource(File fileToDownload) throws FileNotFoundException {
			inputStream = new MyFileInputStream(fileToDownload);
		}

		public InputStream getStream() {
			return inputStream;
		}

		public class MyFileInputStream extends FileInputStream implements Serializable {

			public MyFileInputStream(File fileToDownload) throws FileNotFoundException {
				super(fileToDownload);
			}

		}

	}
}
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.vaadin.ui.Upload.Receiver;

public class UploadReceiver implements Receiver {

private static final long serialVersionUID = -3688499851447366218L;
private String fileName;
   private String mtype;
   private ByteArrayOutputStream uploadedFile = null;

   /**
     * Callback method to begin receiving the upload.
     */
   public OutputStream receiveUpload(String filename, String MIMEType) {
    this.fileName = filename;
    this.mtype = MIMEType;
    
     uploadedFile = new ByteArrayOutputStream();
       return uploadedFile;
   }

   /**
     * Returns the filename
     * @return
     */
   public String getFileName() {
    return fileName;
   }

   /**
     * Returns the filetyp
     * @return
     */
   public String getMimeType() {
    return mtype;
   }
   
   /**
     * Returns the uploaded file as a byteArrayOutputStream
     * @return
     */
   public ByteArrayOutputStream getUploadedFile() {
    return uploadedFile;
   }
}

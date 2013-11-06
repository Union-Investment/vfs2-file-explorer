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

public class ConfigBean {

	boolean bDeleteEnabled;
	boolean bDownloadEnabled;
	boolean bUploadEnabled;

	String vfsUrl;
	String username;
	String password;

	String vfsType;
	String keyfile;
	String proxyHost;
	String proxyPort;

	String uploadRoles;
	String deleteRoles;

	public ConfigBean(String vfsType, boolean bDeleteEnabled, boolean bDownloadEnabled, boolean bUploadEnabled, String vfsUrl, String username,
			String password, String keyfile, String proxyHost, String proxyPort, String uploadRoles, String deleteRoles) {
		super();
		this.bDeleteEnabled = bDeleteEnabled;
		this.bDownloadEnabled = bDownloadEnabled;
		this.bUploadEnabled = bUploadEnabled;
		this.vfsUrl = vfsUrl;
		this.username = username;
		this.password = password;
		this.vfsType = vfsType;
		this.keyfile = keyfile;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.uploadRoles = uploadRoles;
		this.deleteRoles = deleteRoles;
	}

	public boolean isDeleteEnabled() {
		return bDeleteEnabled;
	}

	public void setDeleteEnabled(boolean bDeleteEnabled) {
		this.bDeleteEnabled = bDeleteEnabled;
	}

	public boolean isDownloadEnabled() {
		return bDownloadEnabled;
	}

	public void setDownloadEnabled(boolean bDownloadEnabled) {
		this.bDownloadEnabled = bDownloadEnabled;
	}

	public boolean isUploadEnabled() {
		return bUploadEnabled;
	}

	public void setUploadEnabled(boolean bUploadEnabled) {
		this.bUploadEnabled = bUploadEnabled;
	}

	public String getVfsUrl() {
		return vfsUrl;
	}

	public void setVfsUrl(String vfsUrl) {
		this.vfsUrl = vfsUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVfsType() {
		return vfsType;
	}

	public void setVfsType(String vfsType) {
		this.vfsType = vfsType;
	}

	public String getKeyfile() {
		return keyfile;
	}

	public void setKeyfile(String keyfile) {
		this.keyfile = keyfile;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getUploadRoles() {
		return uploadRoles;
	}

	public void setUploadRoles(String uploadRoles) {
		this.uploadRoles = uploadRoles;
	}

	public String getDeleteRoles() {
		return deleteRoles;
	}

	public void setDeleteRoles(String deleteRoles) {
		this.deleteRoles = deleteRoles;
	}

}

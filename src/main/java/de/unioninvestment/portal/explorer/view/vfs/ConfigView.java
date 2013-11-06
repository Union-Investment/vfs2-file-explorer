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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.PortletPreferences;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import de.unioninvestment.portal.VFSFileExplorerPortlet;
import de.unioninvestment.portal.explorer.event.ConfigChangedEvent;

public class ConfigView extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ConfigView.class.getName());

	public ConfigView(ConfigBean cb, VFSFileExplorerPortlet instance) {

		final OptionGroup group = new OptionGroup("Type");
		group.addItem("FILE");
		group.addItem("FTP");
		group.addItem("SFTP");
		group.setValue(cb.getVfsType());
		group.setImmediate(true);

		final TextField tfDirectory = new TextField("Directory");
		tfDirectory.setValue(cb.getVfsUrl());

		final TextField tfKeyFile = new TextField("Keyfile");
		tfKeyFile.setValue(cb.getKeyfile());

		final TextField tfProxyHost = new TextField("Proxy Host (sftp)");
		tfProxyHost.setValue(cb.getProxyHost());

		final TextField tfProxyPort = new TextField("Proxy Port (sftp)");
		tfProxyPort.setValue(cb.getProxyPort());

		final TextField tfUser = new TextField("User");
		tfUser.setValue(cb.getUsername());

		final PasswordField tfPw = new PasswordField("Password");
		tfPw.setValue(cb.getPassword());

		final CheckBox cbUploadEnabled = new CheckBox("Upload Enabled");
		if (cb.isUploadEnabled()) {
			cbUploadEnabled.setValue(true);
		} else
			cbUploadEnabled.setValue(false);

		final TextField tfRolesUpload = new TextField("Upload Rollen");
		tfRolesUpload.setValue(cb.getUploadRoles());

		final CheckBox cbDeleteEnabled = new CheckBox("Delete Enabled");
		if (cb.isDeleteEnabled()) {
			cbDeleteEnabled.setValue(true);
		} else
			cbDeleteEnabled.setValue(false);

		final TextField tfRolesDelete = new TextField("Delete Rollen");
		tfRolesDelete.setValue(cb.getDeleteRoles());

		group.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				setVisibleFields(group, tfKeyFile, tfProxyHost, tfProxyPort, tfUser, tfPw);

			}

		});

		setVisibleFields(group, tfKeyFile, tfProxyHost, tfProxyPort, tfUser, tfPw);

		Button saveProps = new Button("Save");
		final VFSFileExplorerPortlet app = instance;
		saveProps.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					PortletPreferences prefs = app.getPortletPreferences();

					String type = group.getValue().toString();
					prefs.setValue("type", type);

					String con = tfDirectory.getValue().toString();
					prefs.setValue("directory", con);

					String key = tfKeyFile.getValue().toString();
					prefs.setValue("keyfile", key);

					String proxyHost = tfProxyHost.getValue().toString();
					prefs.setValue("proxyHost", proxyHost);

					String proxyPort = tfProxyPort.getValue().toString();
					prefs.setValue("proxyPort", proxyPort);

					String uploadRoles = tfRolesUpload.getValue().toString();
					prefs.setValue("uploadRoles", uploadRoles);

					String deleteRoles = tfRolesDelete.getValue().toString();
					prefs.setValue("deleteRoles", deleteRoles);

					String username = tfUser.getValue().toString();
					prefs.setValue("username", username);

					String password = tfPw.getValue().toString();
					prefs.setValue("password", password);

					Boolean bDel = (Boolean) cbDeleteEnabled.getValue();
					Boolean bUpl = (Boolean) cbUploadEnabled.getValue();
					if (bDel)
						prefs.setValue("deleteEnabled", "true");
					else
						prefs.setValue("deleteEnabled", "false");

					if (bUpl)
						prefs.setValue("uploadEnabled", "true");
					else
						prefs.setValue("uploadEnabled", "false");

					prefs.store();

					logger.log(Level.INFO, "Roles Upload " + prefs.getValue("uploadEnabled", "-"));
					logger.log(Level.INFO, "Roles Delete " + prefs.getValue("deleteEnabled", "-"));

					ConfigBean cb = new ConfigBean(type, bDel, false, bUpl, con, username, password, key, proxyHost, proxyPort, uploadRoles,
							deleteRoles);

					app.getEventBus().fireEvent(new ConfigChangedEvent(cb));

				} catch (Exception e) {
					logger.log(Level.INFO, "Exception " + e.toString());
					e.printStackTrace();
				}
			}
		});
		addComponent(group);
		addComponent(tfDirectory);
		addComponent(tfKeyFile);
		addComponent(tfProxyHost);
		addComponent(tfProxyPort);
		addComponent(tfUser);
		addComponent(tfPw);

		HorizontalLayout ul = new HorizontalLayout();
		ul.setSpacing(true);
		ul.addComponent(cbUploadEnabled);
		ul.addComponent(tfRolesUpload);
		ul.setComponentAlignment(cbUploadEnabled, Alignment.MIDDLE_CENTER);
		ul.setComponentAlignment(tfRolesUpload, Alignment.MIDDLE_CENTER);
		addComponent(ul);

		HorizontalLayout dl = new HorizontalLayout();
		dl.setSpacing(true);
		dl.addComponent(cbDeleteEnabled);
		dl.addComponent(tfRolesDelete);
		dl.setComponentAlignment(cbDeleteEnabled, Alignment.MIDDLE_CENTER);
		dl.setComponentAlignment(tfRolesDelete, Alignment.MIDDLE_CENTER);
		addComponent(dl);
		addComponent(saveProps);
	}

	private void setVisibleFields(final OptionGroup group, final TextField tfKeyFile, final TextField tfProxyHost, final TextField tfProxyPort,
			final TextField tfUser, final PasswordField tfPw) {
		if (group.getValue() == null)
			group.setValue("FILE");
		String strGroup = group.getValue().toString();
		if (strGroup == "FTP") {
			tfKeyFile.setVisible(false);
			tfProxyHost.setVisible(false);
			tfProxyPort.setVisible(false);
			tfUser.setVisible(true);
			tfPw.setVisible(true);
		} else if (strGroup == "SFTP") {
			tfKeyFile.setVisible(true);
			tfProxyHost.setVisible(true);
			tfProxyPort.setVisible(true);
			tfUser.setVisible(true);
			tfPw.setVisible(true);
		} else {
			// File
			tfKeyFile.setVisible(false);
			tfProxyHost.setVisible(false);
			tfProxyPort.setVisible(false);
			tfUser.setVisible(false);
			tfPw.setVisible(false);

		}
	}

}

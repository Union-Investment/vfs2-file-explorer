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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeSelector;

import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

import com.vaadin.ui.themes.Reindeer;

import de.unioninvestment.portal.VFSFileExplorerPortlet;
import de.unioninvestment.portal.explorer.event.TableChangedEvent;

public class VFSMainView extends HorizontalLayout {

	private static final Logger logger = Logger.getLogger(VFSMainView.class.getName());

	private static final long serialVersionUID = 1L;
	private final Panel explorerPanel = new Panel();
	private final Panel filePanel = new Panel();

	private final Tree tree = new Tree();

	static String configDir = null;

	private static final Resource FOLDER = new ThemeResource("../runo/icons/16/folder.png");

	public VFSMainView(ConfigBean cb, VFSFileExplorerPortlet instance) throws Exception {

		final String vfsUrl = cb.getVfsUrl();
		if (vfsUrl.length() != 0) {
			removeAllComponents();
			explorerPanel.setStyleName(Reindeer.PANEL_LIGHT);
			filePanel.setStyleName(Reindeer.PANEL_LIGHT);
			FileSystemOptions opts = new FileSystemOptions();

			logger.log(Level.INFO, "Check Type ");

			if (cb.getVfsType().equalsIgnoreCase("FTP") || cb.getVfsType().equalsIgnoreCase("SFTP")) {
				if (cb.getUsername() != null && cb.getUsername().length() > 0) {
					StaticUserAuthenticator auth = new StaticUserAuthenticator(null, cb.getUsername(), cb.getPassword());
					DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
				}
			}

			if (cb.getVfsType().equalsIgnoreCase("FTP")) {
				FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
				FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
			}

			if (cb.getVfsType().equalsIgnoreCase("SFTP")) {
				SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
				if (cb.getKeyfile() != null && cb.getKeyfile().length() > 0) {
					logger.log(Level.INFO, "Keyfile " + cb.getKeyfile());
					SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
					File keyFile = new File(cb.getKeyfile());
					SftpFileSystemConfigBuilder.getInstance().setIdentities(opts, new File[] { keyFile });

				}

				SftpFileSystemConfigBuilder.getInstance().setProxyType(opts, SftpFileSystemConfigBuilder.PROXY_HTTP);
				if (cb.getProxyHost() != null && cb.getProxyHost().length() > 0) {
					SftpFileSystemConfigBuilder.getInstance().setProxyHost(opts, cb.getProxyHost());
					logger.log(Level.INFO, "ProxyHost " + cb.getProxyHost());
				}
				if (cb.getProxyPort() != null && cb.getProxyPort().length() > 0) {
					SftpFileSystemConfigBuilder.getInstance().setProxyPort(opts, Integer.valueOf(cb.getProxyPort()));
					logger.log(Level.INFO, "ProxyPort " + cb.getProxyPort());
				}
			}

			DefaultFileSystemManager fsManager = null;
			fsManager = getManager();

			final HorizontalSplitPanel panel = new HorizontalSplitPanel();
			panel.setHeight(500, UNITS_PIXELS);
			panel.setWidth(1400, UNITS_PIXELS);
			panel.setSplitPosition(350, Sizeable.UNITS_PIXELS);
			panel.setFirstComponent(explorerPanel);
			panel.setSecondComponent(filePanel);
			addComponent(panel);

			final Embedded image = new Embedded();
			image.setType(Embedded.TYPE_IMAGE);
			image.setSource(FOLDER);
			image.setHeight(15, Sizeable.UNITS_PIXELS);

			explorerPanel.setSizeFull();
			filePanel.setSizeFull();
			explorerPanel.addComponent(tree);

			filePanel.addComponent(new TableView(instance, fsManager, opts, cb));
			tree.setImmediate(true);

			tree.addListener(new ItemClickEvent.ItemClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void itemClick(ItemClickEvent event) {
					VFSFileExplorerPortlet app = (VFSFileExplorerPortlet) getApplication();
					String newDir = (String) event.getItemId();
					app.getEventBus().fireEvent(new TableChangedEvent(newDir));
				}
			});

			scanDirectory(fsManager, opts, vfsUrl);
		} else {

			addComponent(new Label("Please configure Portlet !"));
		}
	}

	public void scanDirectory(FileSystemManager fsManager, FileSystemOptions opts, String ftpconn) throws IOException {
		try {
			FileObject fileObject = fsManager.resolveFile(ftpconn, opts);
			FileObject[] files = fileObject.findFiles(new FileTypeSelector(FileType.FOLDER));
			HashMap<String, String> parentMap = new HashMap<String, String>();
			for (FileObject fo : files) {
				String objectName = fo.getName().toString();
				tree.addItem(objectName);
				tree.setItemIcon(objectName, FOLDER);
				if (fo.getParent() != null) {
					String parentName = fo.getParent().getName().toString();
					parentMap.put(objectName, parentName);
				} else
					tree.setItemCaption(objectName, "/");
			}

			// set parents
			logger.log(Level.INFO, "parentMap " + parentMap.size());
			if (parentMap.size() > 0) {
				Iterator<Map.Entry<String, String>> it = parentMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = it.next();
					tree.setParent(pairs.getKey(), pairs.getValue());
					String caption = pairs.getKey().toString().substring(pairs.getValue().toString().length());

					tree.setItemCaption(pairs.getKey(), removeSlash(caption));
					it.remove();
				}
			}
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}

	public static String removeSlash(String strNode) {
		if (strNode.startsWith("/"))
			strNode = strNode.substring(1);
		if (strNode.endsWith("/"))
			strNode = strNode.substring(0, strNode.length() - 1);
		return strNode;
	}

	public static DefaultFileSystemManager getManager() throws FileSystemException {
		DefaultFileSystemManager mngr = new DefaultFileSystemManager();

		mngr.addProvider("sftp", new SftpFileProvider());
		mngr.addProvider("ftp", new FtpFileProvider());
		mngr.addProvider("file", new DefaultLocalFileProvider());

		mngr.init();
		return mngr;

	}

}
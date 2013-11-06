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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.MultiFileUpload;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Button;

import com.vaadin.ui.HorizontalLayout;

import com.vaadin.ui.Table;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window.Notification;

import de.unioninvestment.portal.VFSFileExplorerPortlet;

import de.unioninvestment.portal.explorer.event.TableChangedEvent;
import de.unioninvestment.portal.explorer.event.TableChangedEventHandler;
import de.unioninvestment.portal.explorer.file.DownloadResource;

import de.unioninvestment.portal.explorer.file.UploadReceiver;

public class TableView extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TableView.class.getName());
	private static final String TABLE_PROP_FILE_NAME = "Name";
	private static final String TABLE_PROP_FILE_SIZE = "Size (bytes)";
	private static final String TABLE_PROP_FILE_DATE = "Date";
	String selectedDir;

	Collection markedRows = null;

	VFSFileExplorerPortlet instance;
	FileSystemManager fileSystemManager;
	FileSystemOptions opts;
	ConfigBean cb;

	MultiFileUpload multiFileUpload;
	Upload upload;

	//
	public TableView(VFSFileExplorerPortlet instance, FileSystemManager fileSystemManager, FileSystemOptions opts, ConfigBean cb) {

		this.instance = instance;
		this.fileSystemManager = fileSystemManager;
		this.opts = opts;
		this.cb = cb;

	}

	public void paint(PaintTarget target) throws PaintException {
		WebBrowser webBrowser = (WebBrowser) getWindow().getTerminal();

		if (webBrowser.isIE()) {
			multiFileUpload.setVisible(false);
			upload.setVisible(true);
		} else {
			multiFileUpload.setVisible(true);
			upload.setVisible(false);
		}

		super.paint(target);
	}

	public void attach() {
		
		selectedDir = cb.getVfsUrl();
		try {

			final VFSFileExplorerPortlet app = instance;
			final User user = (User) app.getUser();
			final FileSystemManager fFileSystemManager = fileSystemManager;
			final FileSystemOptions fOpts = opts;

			final Table table = new Table() {

				private static final long serialVersionUID = 1L;

				protected String formatPropertyValue(Object rowId, Object colId, Property property) {

					if (TABLE_PROP_FILE_NAME.equals(colId)) {
						if (property != null && property.getValue() != null) {
							return getDisplayPath(property.getValue().toString());
						}
					}
					if (TABLE_PROP_FILE_DATE.equals(colId)) {
						if (property != null && property.getValue() != null) {
							SimpleDateFormat sdf = new SimpleDateFormat("dd.MMM yyyy HH:mm:ss");
							return sdf.format((Date) property.getValue());
						}
					}
					return super.formatPropertyValue(rowId, colId, property);
				}

			};
			table.setSizeFull();
			table.setMultiSelect(true);
			table.setSelectable(true);
			table.setImmediate(true);
			table.addContainerProperty(TABLE_PROP_FILE_NAME, String.class, null);
			table.addContainerProperty(TABLE_PROP_FILE_SIZE, Long.class, null);
			table.addContainerProperty(TABLE_PROP_FILE_DATE, Date.class, null);
			if (app != null) {
				app.getEventBus().addHandler(TableChangedEvent.class, new TableChangedEventHandler() {
					private static final long serialVersionUID = 1L;

					@Override
					public void onValueChanged(TableChangedEvent event) {
						try {
							selectedDir = event.getNewDirectory();
							fillTableData(event.getNewDirectory(), table, fFileSystemManager, fOpts, null);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}

			table.addListener(new Table.ValueChangeListener() {
				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {

					Set<?> value = (Set<?>) event.getProperty().getValue();
					if (null == value || value.size() == 0) {
						markedRows = null;
					} else {
						markedRows = value;
					}
				}
			});

			fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);

			Button btDownload = new Button("Download File(s)");
			btDownload.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {
					if (markedRows == null || markedRows.size() == 0)
						getWindow().showNotification("No Files selected !", Window.Notification.TYPE_WARNING_MESSAGE);
					else {
						String[] files = new String[markedRows.size()];
						int fileCount = 0;

						for (Object item : markedRows) {
							Item it = table.getItem(item);
							files[fileCount] = it.getItemProperty(TABLE_PROP_FILE_NAME).toString();
							fileCount++;
						}

						File dlFile = null;
						if (fileCount == 1) {
							try {
								String fileName = files[0];
								dlFile = getFileFromVFSObject(fFileSystemManager, fOpts, fileName);
								logger.log(Level.INFO,"vfs2portlet: download file "+  fileName + " by " + user.getScreenName());
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							byte[] buf = new byte[1024];

							try {
								dlFile = File.createTempFile("Files", ".zip");
								ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dlFile.getAbsolutePath()));
								for (int i = 0; i < files.length; i++) {
									String fileName = files[i];
									logger.log(Level.INFO,"vfs2portlet: download file "+  fileName + " by " + user.getScreenName());
									File f = getFileFromVFSObject(fFileSystemManager, fOpts, fileName);
									FileInputStream in = new FileInputStream(f);
									out.putNextEntry(new ZipEntry(f.getName()));
									int len;
									while ((len = in.read(buf)) > 0) {
										out.write(buf, 0, len);
									}
									out.closeEntry();
									in.close();
								}
								out.close();
							} catch (IOException e) {
							}

						}

						if (dlFile != null) {
							try {
								DownloadResource downloadResource = new DownloadResource(dlFile, getApplication());
								getApplication().getMainWindow().open(downloadResource, "_new");
							} catch (FileNotFoundException e) {
								getWindow().showNotification("File not found !", Window.Notification.TYPE_ERROR_MESSAGE);
								e.printStackTrace();
							}

						}

						if (dlFile != null) {
							dlFile.delete();
						}
					}

				}
			});

			Button btDelete = new Button("Delete File(s)");
			btDelete.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					if (markedRows == null || markedRows.size() == 0)
						getWindow().showNotification("No Files selected !", Window.Notification.TYPE_WARNING_MESSAGE);
					else {
						for (Object item : markedRows) {
							Item it = table.getItem(item);
							String fileToDelete = it.getItemProperty(TABLE_PROP_FILE_NAME).toString();
							logger.log(Level.INFO, "Delete File " + fileToDelete);
							try {
								FileObject delFile = fFileSystemManager.resolveFile(fileToDelete, fOpts);
								logger.log(Level.INFO,"vfs2portlet: delete file "+  delFile.getName() + " by " + user.getScreenName());
								boolean b = delFile.delete();
								if (b)
									logger.log(Level.INFO, "delete ok");
								else
									logger.log(Level.INFO, "delete failed");
							} catch (FileSystemException e) {
								e.printStackTrace();
							}
						}
						try {
							fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			});

			Button selAll = new Button("Select All", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					table.setValue(table.getItemIds());
				}
			});

			Button selNone = new Button("Select None", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					table.setValue(null);
				}
			});

			final UploadReceiver receiver = new UploadReceiver();
			upload = new Upload(null, receiver);
			upload.setImmediate(true);
			upload.setButtonCaption("File Upload");

			upload.addListener((new Upload.SucceededListener() {

				private static final long serialVersionUID = 1L;

				public void uploadSucceeded(SucceededEvent event) {

					try {
						String fileName = receiver.getFileName();
						ByteArrayOutputStream bos = receiver.getUploadedFile();
						byte[] buf = bos.toByteArray();
						ByteArrayInputStream bis = new ByteArrayInputStream(buf);
						String fileToAdd = selectedDir + "/" + fileName;
						logger.log(Level.INFO, "vfs2portlet: add file " + fileToAdd + " by " + user.getScreenName());
						FileObject localFile = fFileSystemManager.resolveFile(fileToAdd, fOpts);
						localFile.createFile();
						OutputStream localOutputStream = localFile.getContent().getOutputStream();
						IOUtils.copy(bis, localOutputStream);
						localOutputStream.flush();
						fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);
						app.getMainWindow().showNotification("Upload " + fileName + " successful ! ", Notification.TYPE_TRAY_NOTIFICATION);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}));

			upload.addListener(new Upload.FailedListener() {
				private static final long serialVersionUID = 1L;

				public void uploadFailed(FailedEvent event) {
					System.out.println("Upload failed ! ");
				}
			});

			multiFileUpload = new MultiFileUpload() {

				private static final long serialVersionUID = 1L;

				protected void handleFile(File file, String fileName, String mimeType, long length) {
					try {
						byte[] buf = FileUtils.readFileToByteArray(file);
						ByteArrayInputStream bis = new ByteArrayInputStream(buf);
						String fileToAdd = selectedDir + "/" + fileName;
						logger.log(Level.INFO, "vfs2portlet: add file " + fileToAdd + " by " + user.getScreenName());
						FileObject localFile = fFileSystemManager.resolveFile(fileToAdd, fOpts);
						localFile.createFile();
						OutputStream localOutputStream = localFile.getContent().getOutputStream();
						IOUtils.copy(bis, localOutputStream);
						localOutputStream.flush();
						fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);
					} catch (FileSystemException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				protected FileBuffer createReceiver() {
					FileBuffer receiver = super.createReceiver();
					/*
					 * Make receiver not to delete files after they have been
					 * handled by #handleFile().
					 */
					receiver.setDeleteFiles(false);
					return receiver;
				}
			};
			multiFileUpload.setUploadButtonCaption("Upload File(s)");

			HorizontalLayout filterGrp = new HorizontalLayout();
			filterGrp.setSpacing(true);
			final TextField tfFilter = new TextField();
			Button btFileFilter = new Button("Filter", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					String filterVal = (String) tfFilter.getValue();
					try {
						if (filterVal == null || filterVal.length() == 0) {
							fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);
						} else {
							fillTableData(selectedDir, table, fFileSystemManager, fOpts, filterVal);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			Button btResetFileFilter = new Button("Reset", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent event) {
					try {
						tfFilter.setValue("");
						fillTableData(selectedDir, table, fFileSystemManager, fOpts, null);
					} catch (ReadOnlyException e) {
						e.printStackTrace();
					} catch (ConversionException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			filterGrp.addComponent(tfFilter);
			filterGrp.addComponent(btFileFilter);
			filterGrp.addComponent(btResetFileFilter);

			addComponent(filterGrp);

			addComponent(table);

			HorizontalLayout btGrp = new HorizontalLayout();

			btGrp.setSpacing(true);
			btGrp.addComponent(selAll);
			btGrp.setComponentAlignment(selAll, Alignment.MIDDLE_CENTER);
			btGrp.addComponent(selNone);
			btGrp.setComponentAlignment(selNone, Alignment.MIDDLE_CENTER);
			btGrp.addComponent(btDownload);
			btGrp.setComponentAlignment(btDownload, Alignment.MIDDLE_CENTER);

			
			List<Role> roles = null;
			boolean matchUserRole = false;
			try {

				if (user != null) {
					roles = user.getRoles();

				}
			} catch (SystemException e) {
				e.printStackTrace();
			}

			if (cb.isDeleteEnabled() && cb.getDeleteRoles().length() == 0) {
				btGrp.addComponent(btDelete);
				btGrp.setComponentAlignment(btDelete, Alignment.MIDDLE_CENTER);
			} else if (cb.isDeleteEnabled() && cb.getDeleteRoles().length() > 0) {
				matchUserRole = isUserInRole(roles, cb.getDeleteRoles());
				if (matchUserRole) {
					btGrp.addComponent(btDelete);
					btGrp.setComponentAlignment(btDelete, Alignment.MIDDLE_CENTER);
				}

			}
			if (cb.isUploadEnabled() && cb.getUploadRoles().length() == 0) {
				btGrp.addComponent(upload);
				btGrp.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
				btGrp.addComponent(multiFileUpload);
				btGrp.setComponentAlignment(multiFileUpload, Alignment.MIDDLE_CENTER);
			} else if (cb.isUploadEnabled() && cb.getUploadRoles().length() > 0) {

				matchUserRole = isUserInRole(roles, cb.getUploadRoles());
				if (matchUserRole) {
					btGrp.addComponent(upload);
					btGrp.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
					btGrp.addComponent(multiFileUpload);
					btGrp.setComponentAlignment(multiFileUpload, Alignment.MIDDLE_CENTER);
				}
			}
			addComponent(btGrp);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean isUserInRole(List<Role> roles, String deleteRoles) {
		StringTokenizer roleTokens = new StringTokenizer(deleteRoles, ",");
		while (roleTokens.hasMoreTokens()) {
			String curTok = roleTokens.nextToken();
			for (Role r : roles) {
				if (r.getName().equalsIgnoreCase(curTok))
					return true;
			}
		}
		return false;
	}

	private void fillTableData(String selectedDir, Table table, FileSystemManager fsManager, FileSystemOptions opts, String filterVal)
			throws IOException {
		table.removeAllItems();

		FileObject fileObject = fsManager.resolveFile(selectedDir, opts);
		FileObject[] files = fileObject.getChildren();
		for (FileObject file : files) {

			if (filterVal == null) {
				addTableItem(table, file);
			} else {
				String regex = filterVal.replace("?", ".?").replace("*", ".*?");
				String name = getDisplayPath(file.getName().toString());
				if (name.matches(regex))
					addTableItem(table, file);
			}

		}
	}

	private void addTableItem(Table table, FileObject file) throws FileSystemException {
		if (file.getType() == FileType.FILE) {
			Item item = table.addItem(file.getName());
			item.getItemProperty(TABLE_PROP_FILE_NAME).setValue(file.getName().toString());
			item.getItemProperty(TABLE_PROP_FILE_SIZE).setValue(file.getContent().getSize());
			item.getItemProperty(TABLE_PROP_FILE_DATE).setValue(new Date(file.getContent().getLastModifiedTime()));
		}
	}

	private static String getDisplayPath(String fn) {

		int i = fn.lastIndexOf("/");
		return fn.substring(i + 1);
	}

	private File getFileFromVFSObject(FileSystemManager fileSystemManager, FileSystemOptions opts, String vfsPath) {
		OutputStream outputStream = null;
		try {
			FileObject downloadFile = fileSystemManager.resolveFile(vfsPath, opts);
			FileContent fc = downloadFile.getContent();
			InputStream inputStream = fc.getInputStream();
			String fn = getDisplayPath(vfsPath);
			String tDir = System.getProperty("java.io.tmpdir");
			File locFile = new File(tDir, fn);
			outputStream = new FileOutputStream(locFile);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			return locFile;

		} catch (FileSystemException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

}

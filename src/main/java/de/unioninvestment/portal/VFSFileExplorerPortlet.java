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
package de.unioninvestment.portal;


import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2.PortletListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;


import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import de.unioninvestment.portal.explorer.event.ConfigChangedEvent;
import de.unioninvestment.portal.explorer.event.ConfigChangedEventHandler;
import de.unioninvestment.portal.explorer.view.vfs.ConfigBean;
import de.unioninvestment.portal.explorer.view.vfs.ConfigView;
import de.unioninvestment.portal.explorer.view.vfs.VFSMainView;
import de.unioninvestment.portal.explorer.view.vfs.HelpView;
import de.unioninvestment.portal.util.event.EventBus;

@SuppressWarnings("serial")
public class VFSFileExplorerPortlet extends Application implements PortletListener, PortletRequestListener {

	private EventBus eventBus;
	private static ThreadLocal<PortletPreferences> portletPreferences = new ThreadLocal<PortletPreferences>();
	VFSMainView viewContent = null;
	ConfigView editContent = null;
	HelpView helpContent = new HelpView();
	final Window mainWindow = new Window();


    @Override
    public void init() {

		
    	mainWindow.setImmediate(true);
        if (getContext() instanceof PortletApplicationContext2) {
			PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
			ctx.addPortletListener(this, this);
		} else {
			mainWindow.showNotification("Not running in portal", Notification.TYPE_ERROR_MESSAGE);
		}
       

        final ConfigBean cb = getCurrentConfigBean();
			try {
				viewContent = new VFSMainView(cb, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			editContent = new ConfigView(cb, this);
			mainWindow.setContent(viewContent);
  
		getEventBus().addHandler(ConfigChangedEvent.class, new ConfigChangedEventHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onValueChanged(ConfigChangedEvent event) {
				try {

					viewContent = new VFSMainView(event.getConfigBean(),(VFSFileExplorerPortlet)getMainWindow().getApplication());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
        
        
        setMainWindow(mainWindow);
    }
    
    public EventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new EventBus();
		}
		return eventBus;
	}

	@Override
	public void onRequestStart(PortletRequest request, PortletResponse response) {
    
		portletPreferences.set(request.getPreferences());
		if (getUser() == null) {
            try {
                User user = PortalUtil.getUser(request);
                setUser(user);
            } catch (PortalException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void onRequestEnd(PortletRequest request, PortletResponse response) {

	}
	
	public PortletPreferences getPortletPreferences() {
		return portletPreferences.get();
	}

	@Override
	public void handleRenderRequest(RenderRequest request,
			RenderResponse response, Window window) {

		
	}

	@Override
	public void handleActionRequest(ActionRequest request,
			ActionResponse response, Window window) {

		
	}

	@Override
	public void handleEventRequest(EventRequest request,
			EventResponse response, Window window) {

		
	}

	@Override
	public void handleResourceRequest(ResourceRequest request,
			ResourceResponse response, Window window) {
		if (request.getPortletMode() == PortletMode.EDIT){
			window.setContent(editContent);
		}
		else if (request.getPortletMode() == PortletMode.VIEW){
			if (window != null)
				window.setContent(viewContent);
		}
		else if (request.getPortletMode() == PortletMode.HELP)
			window.setContent(new HelpView());

	}
	
	
	public static ConfigBean getCurrentConfigBean(){
		String emptyString = "";
        String type = portletPreferences.get().getValue("type", emptyString);
        String vfsPath = portletPreferences.get().getValue("directory", emptyString);
        String username = portletPreferences.get().getValue("username", emptyString);
        String keyfile = portletPreferences.get().getValue("keyfile", emptyString);
        String password = portletPreferences.get().getValue("password", emptyString);
	    String proxyHost = portletPreferences.get().getValue("proxyHost", emptyString);
        String proxyPort = portletPreferences.get().getValue("proxyPort", emptyString);
        String uploadRoles = portletPreferences.get().getValue("uploadRoles", emptyString);
        String deleteRoles = portletPreferences.get().getValue("deleteRoles", emptyString);
    	String uploadOn = portletPreferences.get().getValue("uploadEnabled", null);
    	String deleteOn = portletPreferences.get().getValue("deleteEnabled", null);
    	String downloadOn = portletPreferences.get().getValue("deleteEnabled", null);
    	
    	boolean bUploadOn = false;
    	boolean bDeleteOn = false;
    	boolean bDownloadOn = false;
    	
    	if (uploadOn != null && uploadOn.equalsIgnoreCase("true"))
    		bUploadOn = true;
    	
    	if (deleteOn != null && deleteOn.equalsIgnoreCase("true"))
    		bDeleteOn = true;
    	
    	if (downloadOn != null && downloadOn.equalsIgnoreCase("true"))
    		bDownloadOn = true;
    	
    	
    	final ConfigBean cb = new ConfigBean(type,bDeleteOn, bDownloadOn, bUploadOn, vfsPath, username, password,keyfile, proxyHost, proxyPort,uploadRoles, deleteRoles);
    	return cb;
		
	}



}

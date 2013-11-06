package de.unioninvestment.portal;
import javax.portlet.PortletRequest;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;

public class VFSResourceApplicationPortlet extends ApplicationPortlet2 {

    protected String getStaticFilesLocation(PortletRequest request) {
        return request.getContextPath();
    }

}
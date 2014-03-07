<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@page import="java.util.Calendar"%>
<html:xhtml/>
<dt:format pattern="yyyy/MM/dd HH:mm:ss"><%= Calendar.getInstance().getTimeInMillis() %></dt:format>
<br /><br />

Current Version: <%= pt.ist.fenixframework.FenixFramework.getProject().getVersion() %>
<br /><br />

	<bean:message bundle="MANAGER_RESOURCES" key="label.server.name"/><%= request.getServerName() %>
	<br/>
	<bean:message bundle="MANAGER_RESOURCES" key="label.real.server.name"/><%= System.getenv("HOSTNAME") %>
	<br/>

	<bean:message bundle="MANAGER_RESOURCES" key="message.memory.units"/>
	<br/>
	<strong><bean:message bundle="MANAGER_RESOURCES" key="label.system.properties"/></strong>
	<br/>
<strong><bean:message bundle="MANAGER_RESOURCES" key="label.system.env.properties"/></strong>
<br/>
<logic:iterate id="property" name="systemEnv">
	<bean:write name="property" property="key"/>=<bean:write name="property" property="value"/><br />
</logic:iterate>

<br/><br/>
<strong><bean:message bundle="MANAGER_RESOURCES" key="label.request.headers"/></strong>
<br/>
<%
	for (final Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
	    final Object o = e.nextElement();
	    %>
	    <strong>
	    <%= o.toString() %>
	    </strong>
	    =
	    <%= request.getHeader(o.toString()) %>
	    <br/>
	    <%
	}
%>
<%@ page import="com.sforce.soap.partner.sobject.SObject"%>

<%  
  SObject[] contacts = (SObject[])request.getAttribute("contacts");
  String name = (String)request.getAttribute("accountName");
%>

<%@ include file="top.jsp" %>

<p><a href="/home">Home</a></p>

<h3>Contacts for <%= name %></h3>

<% if (contacts.length > 0) { %>
    <ol>
    <% for (SObject contact : contacts) { %>
        <li><%= (String)contact.getField("Name") %></li>
    <% } %>
    </ol>
<% } else { %>
    No contacts found. Try "ACME Corp".
<% } %>
    
<%@ include file="bottom.jsp" %>

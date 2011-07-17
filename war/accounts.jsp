<%@ page import="com.sforce.soap.partner.sobject.SObject"%>

<%  
  SObject[] accounts = (SObject[])request.getAttribute("accounts");
%>

<%@ include file="top.jsp" %>

<p><a href="/home">Home</a></p>

<h3>Accounts from Salesforce.com</h3>

<p>Select an Account to view its Contacts.</p>

<% if (accounts.length > 0) { %>
    <ol>
    <% for (SObject account : accounts) { %>
        <li><a href="/contacts?accountId=<%= (String)account.getId()%>"><%= (String)account.getField("Name") %></a></li>
    <% } %>
    </ol>
<% } %>
    
<%@ include file="bottom.jsp" %>

<%  
Boolean authorized = (Boolean)request.getAttribute("isAuthorized");
%>

<%@ include file="top.jsp" %>

This application demonstrates the use of the OAuth protocol with Salesforce.com's Remote Access applications. 
It uses OAuth to access Accounts and Contacts via the Web Services API. 

<% if (!authorized) { %>
    <p>This application hasn't be authorized by Salesforce.com. <a href="login">Login to Salesforce.com</a> and authorize this
       application using OAuth</p>
<% } else { %>
    <p>This application has been authorized by Salesforce.com. You can <a href="accounts">fetch a list of accounts</a> 
    and view their contacts.</p>
    <p>You can also <a href="login">authorize this application again</a> with Salesforce.com using OAuth.
<% } %>
    
<%@ include file="bottom.jsp" %>
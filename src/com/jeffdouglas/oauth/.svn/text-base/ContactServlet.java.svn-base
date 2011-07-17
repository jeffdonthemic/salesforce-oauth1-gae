package com.jeffdouglas.oauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeffdouglas.oauth.service.ConnectionManager;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Servlet that selects contacts for specific account in Salesforce
 * 
 * @author Jeff Douglas
 */
@SuppressWarnings("serial")
public class ContactServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(ContactServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    PartnerConnection connection = ConnectionManager.getConnectionManager()
        .getConnection();

    try {

      // query for the contacts for the account
      QueryResult results = connection
          .query("SELECT Id, Name from Contact where AccountId = '"
              + req.getParameter("accountId") + "' Limit 10");

      // get the account so we can use the name
      SObject[] account = connection.retrieve("Id, Name", "Account",
          new String[] { (String) req.getParameter("accountId") });

      req.setAttribute("contacts", results.getRecords());
      req.setAttribute("accountName", account[0].getField("Name"));

      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
          "/contacts.jsp");
      dispatcher.forward(req, resp);

    } catch (ServletException e) {
      log.severe("Servlet exception=" + e.toString());
    } catch (Exception e) {
      log.severe("Query exception=" + e.toString());
    }

  }

}

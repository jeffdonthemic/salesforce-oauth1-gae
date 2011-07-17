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

/**
 * Servlet that selects 10 accounts in Salesforce
 * 
 * @author Jeff Douglas
 */
@SuppressWarnings("serial")
public class AccountServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(AccountServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    PartnerConnection connection = ConnectionManager.getConnectionManager()
        .getConnection();

    try {

      QueryResult results = connection
          .query("SELECT Id, Name from Account Limit 10");
      req.setAttribute("accounts", results.getRecords());

      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
          "/accounts.jsp");
      dispatcher.forward(req, resp);

    } catch (ServletException e) {
      log.severe("Servlet exception=" + e.toString());
    } catch (Exception e) {
      log.severe("Query exception=" + e.toString());
    }

  }

}

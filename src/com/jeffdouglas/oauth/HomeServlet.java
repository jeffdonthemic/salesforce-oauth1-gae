package com.jeffdouglas.oauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeffdouglas.oauth.service.ConnectionManager;

/**
 * The "home" servlet that displays the welcome page and actions to perform
 * 
 * @author Jeff Douglas
 */
@SuppressWarnings("serial")
public class HomeServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(HomeServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    ConnectionManager connection = ConnectionManager.getConnectionManager();

    req.setAttribute("isAuthorized", connection.hasBeenAuthorizeded());
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/home.jsp");

    try {
      dispatcher.forward(req, resp);
    } catch (ServletException e) {
      log.severe("Servlet exception=" + e.toString());
    }

  }

}
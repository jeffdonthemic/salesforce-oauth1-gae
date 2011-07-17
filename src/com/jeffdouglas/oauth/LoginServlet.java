package com.jeffdouglas.oauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeffdouglas.oauth.utils.OauthHelperUtils;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

/**
 * Servlet that request a new OAuth request token and redirects the user the
 * authorization page
 * 
 * @author Jeff Douglas
 */
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(LoginServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    OAuthAccessor accessor = new OAuthAccessor(new OAuthConsumer(
        OauthSettings.URL_CALLBACK, OauthSettings.CONSUMER_KEY,
        OauthSettings.CONSUMER_SECRET, null));

    String response = OauthHelperUtils.getRequestToken(accessor,
        OauthSettings.URL_REQUEST_TOKEN, OauthSettings.URL_CALLBACK);

    log.info("Request token=" + accessor.requestToken);
    log.info("Request token secret=" + accessor.tokenSecret);
    log.info("Resonse=" + response);

    // see if the token request failed
    if (response.startsWith("<")) {
      log.warning("Failed to get request token.");
      resp.setContentType("text/html; charset=UTF-8");
      resp.getWriter().println("Request token failure!!");
      resp.getWriter().println(response);
      return;
    }

    OauthHelperUtils.REQUEST_TOKENS.put(accessor.requestToken,
        accessor.tokenSecret);

    try {
      String authUrl = OauthHelperUtils.buildAuthorizationUrl(accessor,
          OauthSettings.URL_AUTHORIZATION);
      log.info("Authorization URL=" + authUrl);
      resp.sendRedirect(authUrl);
    } catch (Exception e) {
      log.severe("Exception=" + e.toString());
    }

  }

}

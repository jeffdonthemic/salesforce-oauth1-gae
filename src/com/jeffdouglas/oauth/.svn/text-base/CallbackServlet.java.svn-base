package com.jeffdouglas.oauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeffdouglas.oauth.service.ConnectionManager;
import com.jeffdouglas.oauth.utils.OauthHelperUtils;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

/**
 * The servlet that is called from Salesforce after the app is authorized. It
 * stores the access tokens in bigtable and caches the session info
 * 
 * @author Jeff Douglas
 */
@SuppressWarnings("serial")
public class CallbackServlet extends HttpServlet {

  protected static final Logger log = Logger.getLogger(CallbackServlet.class
      .getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    try {

      String oauthToken = (String) req.getParameter(OAuth.OAUTH_TOKEN);
      String oauthConsumerKey = (String) req
          .getParameter(OAuth.OAUTH_CONSUMER_KEY);
      String oauthVerifier = (String) req.getParameter("oauth_verifier");

      log.info("oauthToken=" + oauthToken);
      log.info("oauthConsumerKey=" + oauthConsumerKey);
      log.info("oauthVerifier=" + oauthVerifier);

      OAuthAccessor accessor = new OAuthAccessor(new OAuthConsumer(
          OauthSettings.URL_CALLBACK, OauthSettings.CONSUMER_KEY,
          OauthSettings.CONSUMER_SECRET, null));
      accessor.requestToken = oauthToken;
      accessor.tokenSecret = OauthHelperUtils.REQUEST_TOKENS.get(oauthToken);
      log.info("accessor.requestToken=" + accessor.requestToken);

      String response = OauthHelperUtils.getAccessToken(accessor,
          oauthVerifier, OauthSettings.URL_ACCESS_TOKEN);

      log.info("access token=" + accessor.accessToken);
      log.info("access token secret=" + accessor.tokenSecret);
      log.info("response=" + response);
      log.info("Logging into Salesforce.com since now have the access tokens");

      String loginResponse = OauthHelperUtils.getNewSfdcSession(accessor,
          OauthSettings.URL_API_LOGIN);

      if (loginResponse.startsWith("<")) {
        OauthHelperUtils.XmlResponseHandler xmlHandler = OauthHelperUtils
            .parseResponse(loginResponse);
        String serviceEndpoint = xmlHandler.getServerUrl();
        String sessionId = xmlHandler.getSessionId();
        log.info("service endpoint=" + serviceEndpoint);
        log.info("sessionId=" + sessionId);

        // get the connection
        ConnectionManager connection = ConnectionManager.getConnectionManager();
        // save the access tokens for future use
        connection.saveTokens(accessor.accessToken, accessor.tokenSecret);
        // set the connection
        connection.cacheSessionProps(OauthSettings.URL_AUTH_ENDPOINT,
            serviceEndpoint, sessionId);
      }

      RequestDispatcher rd = req.getRequestDispatcher("home");
      rd.forward(req, resp);

    } catch (Exception e) {
      log.info("Callback servlet exception=" + e.toString());
    }

  }

}

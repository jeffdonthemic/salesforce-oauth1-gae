package com.jeffdouglas.oauth.utils;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import net.oauth.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Contains utility methods for handling session, token
 * and parsing of XML responses. Some code from willywu
 * at salesforce. 
 * 
 * @author Jeff Douglas
 */
public class OauthHelperUtils {

  protected static final Logger log = Logger.getLogger(OauthHelperUtils.class.getName());
  public static final HashMap<String, String> REQUEST_TOKENS = new HashMap<String, String>();

  public static final String OAUTH_CALLBACK = "oauth_callback";
  public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
  public static final String OAUTH_VERIFIER = "oauth_verifier";
  
  static final SAXParserFactory PARSER;
  static final String KEY_SERVER_URL = "serverUrl";
  static final String KEY_METADATA_SERVER_URL = "metadataServerUrl";
  static final String KEY_SESSION_ID = "sessionId";
  static final String KEY_SANDBOX = "sandbox";
  static final String KEY_RESPONSE = "response";
  static final String KEY_MESSAGE = "message";
  static final String KEY_ERROR = "error";

  static {
    PARSER = SAXParserFactory.newInstance();
    PARSER.setNamespaceAware(false);
  }
  
  // returns the response with the access token
  public static String getAccessToken(OAuthAccessor accessor, String verifier,
      String accessTokenUrl) throws Exception {
    
    log.info("fetching access token...");
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(OAUTH_VERIFIER, verifier);
    params.put(OAuth.OAUTH_TOKEN, accessor.requestToken);
    
    try {
      String response = doOauthGetRequest(accessor, accessTokenUrl, params);
      
      if (!response.startsWith("<")) {
        HashMap<String, String> responseParams = parseResponseParams(response);
        accessor.tokenSecret = responseParams.get(OAuth.OAUTH_TOKEN_SECRET);
        accessor.accessToken = responseParams.get(OAuth.OAUTH_TOKEN);
      }
      
      return response;
    } catch (Exception e) {
      return e.getMessage();
    }
    
  }

  // returns the response with the initial request token
  public static String getRequestToken(OAuthAccessor accessor,
      String requestTokenUrl, String callbackUrl) {
    
    log.info("Fetching request tokens..");
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(OAUTH_CALLBACK, callbackUrl);
    
    try {
      String response = doOauthGetRequest(accessor, requestTokenUrl, params);
      
      if (!response.startsWith("<")) {
        HashMap<String, String> responseParams = parseResponseParams(response);
        accessor.requestToken = responseParams.get(OAuth.OAUTH_TOKEN);
        accessor.tokenSecret = responseParams.get(OAuth.OAUTH_TOKEN_SECRET);
      }
      return response;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  // requests a new session from salesforce using the login url
  public static String getNewSfdcSession(OAuthAccessor accessor, String loginUrl) {
    
    HashMap<String, String> parameters = new HashMap<String, String>();
    try {
      
      OAuthMessage m = accessor.newRequestMessage("POST", loginUrl, parameters
          .entrySet());
      
      URL endpoint = new URL(loginUrl);
      HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      
      String urlParameters = OAuth.addParameters(m.URL, m.getParameters());
      String params = urlParameters.split("\\?")[1];

      log.info("POSTing session request to=" + loginUrl);
      log.info("Using OAuth parameters=" + params);

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(params);
      writer.close();

      log.info("Response code=" + connection.getResponseCode());
      return readInputStream(connection.getInputStream());

    } catch (Exception e) {
      return e.getMessage();
    }
    
  }

  // builds the url for the authorizatin reqeust
  public static String buildAuthorizationUrl(OAuthAccessor accessor,
      String authPage) throws Exception {
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(authPage);
    sb.append("?" + OAuth.OAUTH_CONSUMER_KEY + "="
        + URLEncoder.encode(accessor.consumer.consumerKey, "UTF-8"));
    sb.append("&" + OAuth.OAUTH_TOKEN + "="
        + URLEncoder.encode(accessor.requestToken, "UTF-8"));
    
    return sb.toString();
    
  }
  
  // performs a GET request for tokens
  public static String doOauthGetRequest(OAuthAccessor accessor,
      String loginUrl, HashMap<String, String> parameters) throws Exception {
    
    OAuthMessage m = accessor.newRequestMessage("GET", loginUrl, parameters
        .entrySet());
    
    String urlParameters = OAuth.addParameters(m.URL, m.getParameters());

    log.info("GETting request to=" + urlParameters);
    
    URL endpoint = new URL(urlParameters);
    HttpURLConnection connection = (HttpURLConnection) endpoint
        .openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(true);

    log.info("Response code=" + connection.getResponseCode());
    return readInputStream(connection.getInputStream());
    
  }
  
  // reads the input stream
  public static String readInputStream(InputStream input) {
    
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

    try {
      String line = reader.readLine();
      while (line != null) {
        sb.append(line + "\n");
        line = reader.readLine();
      }
      
    } catch (IOException e) {
      log.severe("Error reading input stream=" + e.toString());
      
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        log.severe("Error reading input stream=" + e.toString());
      }
    }
    return sb.toString().trim();
    
  }

  // parses the params from the response
  public static HashMap<String, String> parseResponseParams(String body)
      throws Exception {
    
    HashMap<String, String> results = new HashMap<String, String>();
    for (String keyValuePair : body.split("&")) {
      String[] kvp = keyValuePair.split("=");
      results.put(kvp[0], URLDecoder.decode(kvp[1], "UTF-8"));
    }
    return results;
    
  }

  // parses xml returned in the resonse
  public static XmlResponseHandler parseResponse(String response)
      throws Exception {
    
    XmlResponseHandler handler = new XmlResponseHandler();
    PARSER.newSAXParser().parse(
        new ByteArrayInputStream(response.getBytes()), handler);
    return handler;
    
  }

  // Inner class to hanlde returned XML response from Salesforce 
  public static class XmlResponseHandler extends DefaultHandler {

    private Map<String, String> xmlContent = new HashMap<String, String>();
    private String thisXmlTag;

    @Override
    public void startElement(String namespaceURI, String localName,
        String qualifiedName, Attributes atts) throws SAXException {
      thisXmlTag = qualifiedName;
    }

    @Override
    public void endElement(String namespaceURI, String localName,
        String qualifiedName) throws SAXException {
      thisXmlTag = null;
    }

    @Override
    public void characters(char[] text, int start, int length)
        throws SAXException {
      if (thisXmlTag != null) {
        xmlContent.put(thisXmlTag, new String(text, start, length));
      }
    }
    
    /**
     * @return the associated session
     */
    public String getSessionId() {
      return xmlContent.get(KEY_SESSION_ID);
    }
    
    /**
     * @return the server url
     */
    public String getServerUrl() {
      return xmlContent.get(KEY_SERVER_URL);
    }

    /**
     * @return the url for the metadata 
     */
    public String getMetadataServerUrl() {
      return xmlContent.get(KEY_METADATA_SERVER_URL);
    }

    /**
     * @return boolean if sandbox org
     */
    public boolean isSandbox() {
      return Boolean.valueOf(xmlContent.get(KEY_SANDBOX));
    }

    /**
     * @return the associated message
     */
    public String getMessage() {
      return xmlContent.get(KEY_MESSAGE);
    }
    
    /**
     * @return the associated error
     */
    public String getError() {
      return xmlContent.get(KEY_ERROR);
    }

  }
}

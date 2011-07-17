package com.jeffdouglas.oauth.service;

import java.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.jeffdouglas.oauth.SfdcCredentials;
import com.jeffdouglas.oauth.model.AccessCredentials;
import com.jeffdouglas.oauth.utils.OauthHelperUtils;
import com.jeffdouglas.oauth.OauthSettings;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;


/**
 * Singleton to handle the web services api connection to salesforce
 * 
 * @author Jeff Douglas
 */
public class ConnectionManager {
  
  protected static final Logger log = Logger.getLogger(ConnectionManager.class.getName());
  private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
  private static final String KEY_AUTH_POINT = "authEndPoint";
  private static final String KEY_END_POINT = "serviceEndPoint";
  private static final String KEY_SESSION = "sessionId";
  private static ConnectionManager ref;
  private PartnerConnection connection;
  Cache cache = null;

  private ConnectionManager() { }
  
  public static ConnectionManager getConnectionManager() {
    if (ref == null)
         ref = new ConnectionManager();
    return ref;
  }
  
  // gets the current connection
  public PartnerConnection getConnection() {
       
    try {
      
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      cache = cacheFactory.createCache(new HashMap());
      
      /**
       * See if a session exists in the cache and if so, create a new connection. 
       * Can't cache the request in app engine as the PartnerConnection is not serializable. 
       * Could extend but beyond the scope of this app.
       */
      if (cache.containsKey(KEY_AUTH_POINT)) {
        
        log.info("Connection via cached session.");
        
        ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint((String)cache.get(KEY_AUTH_POINT));
        config.setServiceEndpoint((String)cache.get(KEY_END_POINT));
        config.setSessionId((String)cache.get(KEY_SESSION));
        config.setValidateSchema(true);
        connection = new PartnerConnection(config);
        connection.setSessionHeader((String)cache.get(KEY_SESSION));
        
      // if a session does not exist in the cache  
      } else {
        
        // if the app has been authorized but no session. Request a new session with tokens from db.
        if (hasBeenAuthorizeded()) {
          
          log.info("Authorized but no Salesforce session...");
          
          // fetch the tokens from the database
          PersistenceManager pm = PMF.getPersistenceManager();
          String query = "select from " + AccessCredentials.class.getName();
          List<AccessCredentials> records = (List<AccessCredentials>) pm.newQuery(query).execute();
          
          OAuthAccessor accessor = new OAuthAccessor(new OAuthConsumer(OauthSettings.URL_CALLBACK, 
              OauthSettings.CONSUMER_KEY, OauthSettings.CONSUMER_SECRET, null));
          
          accessor.accessToken = records.get(0).getAccessToken();
          accessor.tokenSecret = records.get(0).getAccessTokenSecret();
          
          log.info("accessToken set to="+records.get(0).getAccessToken());
          log.info("tokenSecret set to="+records.get(0).getAccessTokenSecret());
          
          String loginResponse = OauthHelperUtils.getNewSfdcSession(accessor, OauthSettings.URL_API_LOGIN);
          
          log.info("Returned session response="+loginResponse);
          
          if (loginResponse.startsWith("<")) {
            
            OauthHelperUtils.XmlResponseHandler xmlHandler = null;
            try {
              xmlHandler = OauthHelperUtils.parseResponse(loginResponse);
            } catch (Exception e) {
              log.info("Error parsing response sent from Salesforce");
            }
            
            // cache the connection info
            cacheSessionProps(OauthSettings.URL_AUTH_ENDPOINT, xmlHandler.getServerUrl(), xmlHandler.getSessionId());
            
            // create a new session
            ConnectorConfig config = new ConnectorConfig();
            config.setAuthEndpoint(OauthSettings.URL_AUTH_ENDPOINT);
            config.setServiceEndpoint(xmlHandler.getServerUrl());
            config.setSessionId(xmlHandler.getSessionId());
            config.setConnectionTimeout(7200); // timeout for 2 hours
            config.setValidateSchema(true);
            connection = new PartnerConnection(config);
            connection.setSessionHeader(xmlHandler.getSessionId());
            
          }
          
        // convenience for developing locally  
        } else {
        
          log.info("TESTING -- create connection for Salesforce with u/p");
          
          // use the hard coded u/p of the cache doesn't exist
          ConnectorConfig config = new ConnectorConfig();
          config.setUsername(SfdcCredentials.SFDC_USERNAME);
          config.setPassword(SfdcCredentials.SFDC_PASSWORD);
          connection = Connector.newConnection(config);
        
        }
        
      }
    
    } catch (CacheException e) {
      log.info("cache exception="+e.getMessage());
    } catch (ConnectionException e) {
      log.info("connection exception="+e.getMessage());
    }
    
    return connection;
    
  }
  
  // searches db to determine if app has been authrozied
  public Boolean hasBeenAuthorizeded() {
    
    PersistenceManager pm = PMF.getPersistenceManager();
    String query = "select from " + AccessCredentials.class.getName();
    List<AccessCredentials> records = (List<AccessCredentials>) pm.newQuery(query).execute();    
    
    return records.size() == 0 ? false : true;
    
  }
  
  // saves the acces token and secret to the database
  public void saveTokens(String accessToken, String accessSecret) {
    
    // persist the access token and secret to bigtable
    AccessCredentials creds = new AccessCredentials();
    creds.setAccessToken(accessToken);
    creds.setAccessTokenSecret(accessSecret);
    
    PersistenceManager pm = PMF.getPersistenceManager();
    try {
      
      // delete any existing entries for oauth access credentials
      Query query = pm.newQuery(AccessCredentials.class);
      query.deletePersistentAll();
      // add the new credentials
      pm.makePersistent(creds);
      
    } catch(Exception e) {
      log.info("pmf exception="+e.getMessage());
    } finally {
      pm.close();
    }
    
  }
  
  // caches the auth endpoint, service endpoint and session id
  public void cacheSessionProps(String authEndpoint, String serviceEndpoint, String sessionId) {
    
    // use the partner api, not the enterprise
    serviceEndpoint = serviceEndpoint.replace("/c/", "/u/");
    
    log.info("Caching service end point=" + serviceEndpoint);  
    log.info("Caching auth end point=" + authEndpoint);  
    log.info("Caching session id=" + sessionId);
    
    Map props = new HashMap();
    props.put(GCacheFactory.EXPIRATION_DELTA, 5400); // cache for 90 minutes
    props.put(MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT, true);
    
    try {
      
      // cache the connection for appengine
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      cache = cacheFactory.createCache(props);
      cache.put(KEY_AUTH_POINT, authEndpoint);
      cache.put(KEY_END_POINT, serviceEndpoint);
      cache.put(KEY_SESSION, sessionId);     
    
    } catch (CacheException e) {
      log.info("cache exception="+e.getMessage());
    }
    
  }
  
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  
}

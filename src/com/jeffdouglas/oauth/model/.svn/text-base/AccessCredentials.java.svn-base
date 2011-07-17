package com.jeffdouglas.oauth.model;

import javax.jdo.annotations.IdGeneratorStrategy; 
import javax.jdo.annotations.IdentityType; 
import javax.jdo.annotations.PersistenceCapable; 
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * JDO bean for app engine to persist
 * 
 * @author Jeff Douglas
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class AccessCredentials {
  
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String accessToken;
  
  @Persistent
  private String accessTokenSecret;
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getAccessToken() {
    return accessToken;
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
  public String getAccessTokenSecret() {
    return accessTokenSecret;
  }
  public void setAccessTokenSecret(String accessTokenSecret) {
    this.accessTokenSecret = accessTokenSecret;
  }

}
package com.flipkart.flux.client.config;

public class FluxClientConfiguration {

  private String url = "http://localhost:9998";
  private long socketTimeout = 1000;
  private long connectionTimeout = 1000;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public long getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(long socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public long getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }
}

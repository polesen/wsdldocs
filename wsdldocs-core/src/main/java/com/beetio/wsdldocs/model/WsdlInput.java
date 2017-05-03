package com.beetio.wsdldocs.model;

public class WsdlInput {

  private String name;
  private WsdlMessage message;

  public WsdlInput(String name, WsdlMessage message) {
    this.name = name;
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public WsdlMessage getMessage() {
    return message;
  }
}

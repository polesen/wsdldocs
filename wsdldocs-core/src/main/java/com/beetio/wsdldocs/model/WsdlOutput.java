package com.beetio.wsdldocs.model;

public class WsdlOutput {

  private String name;
  private WsdlMessage message;

  public WsdlOutput(String name, WsdlMessage wsdlMessage) {
    this.name = name;
    this.message = wsdlMessage;
  }

  public String getName() {
    return name;
  }

  public WsdlMessage getMessage() {
    return message;
  }
}

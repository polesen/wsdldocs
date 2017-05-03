package com.beetio.wsdldocs.model;

public class WsdlMessage {

  private WsdlPart part;

  public WsdlMessage(WsdlPart part) {
    this.part = part;
  }

  public WsdlPart getPart() {
    return part;
  }
}

package com.beetio.wsdldocs.model;

public class WsdlFault {

  private String name;
  private WsdlMessage message;

  public WsdlFault(String name, WsdlMessage message) {
    this.name = name;
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public WsdlMessage getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WsdlFault wsdlFault = (WsdlFault) o;

    return name.equals(wsdlFault.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}

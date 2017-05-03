package com.beetio.wsdldocs.model;

/**
 * Part of a message.
 */
public class WsdlPart {

  private String name;

  private String rawSchemaType;

  public WsdlPart(String name, String rawSchemaType) {
    this.name = name;
    this.rawSchemaType = rawSchemaType;
  }

  public String getName() {
    return name;
  }

  public String getRawSchemaType() {
    return rawSchemaType;
  }
}

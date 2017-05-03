package com.beetio.wsdldocs.model;

import java.util.ArrayList;
import java.util.List;

public class WsdlPortType {

  private String name;
  private List<WsdlOperation> operations = new ArrayList<>();

  public WsdlPortType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void addOperation(WsdlOperation operation) {
    operations.add(operation);
  }

  public List<WsdlOperation> getOperations() {
    return operations;
  }

}

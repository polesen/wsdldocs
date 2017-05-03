package com.beetio.wsdldocs.model;

import java.util.ArrayList;
import java.util.List;

public class WsdlOperation {

  private String name;
  private String documentation;

  private WsdlInput input;
  private WsdlOutput output;

  private List<WsdlFault> faults = new ArrayList<>();

  public WsdlOperation(String name, String operationDocumentation, WsdlInput input, WsdlOutput output) {
    this.name = name;
    this.documentation = operationDocumentation;
    this.input = input;
    this.output = output;
  }

  public String getName() {
    return name;
  }

  public String getDocumentation() {
    return documentation;
  }

  public WsdlInput getInput() {
    return input;
  }

  public WsdlOutput getOutput() {
    return output;
  }

  public boolean hasOutput() {
    return getOutput() != null;
  }

  public void addFault(WsdlFault fault) {
    faults.add(fault);
  }

  public List<WsdlFault> getFaults() {
    return faults;
  }

  public boolean hasFaults() {
    return !getFaults().isEmpty();
  }
}

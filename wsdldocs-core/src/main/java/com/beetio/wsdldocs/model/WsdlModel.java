package com.beetio.wsdldocs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WsdlModel {

  private List<WsdlPortType> portTypes = new ArrayList<>();

  public void addPortType(WsdlPortType portType) {
    portTypes.add(portType);
  }

  public List<WsdlPortType> getPortTypes() {
    return portTypes;
  }

  public Collection<WsdlFault> getAllFaults() {
    Set<WsdlFault> faults = new HashSet<>();
    for (WsdlPortType portType : portTypes) {
      for (WsdlOperation operation : portType.getOperations()) {
        faults.addAll(operation.getFaults());
      }
    }
    return faults;
  }
}

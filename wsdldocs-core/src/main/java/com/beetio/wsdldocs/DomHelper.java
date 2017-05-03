package com.beetio.wsdldocs;

import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class DomHelper {

  public static String getAttributeValue(Node node, String attrName) {
    return node.getAttributes().getNamedItem(attrName).getNodeValue();
  }

  public static String stripNsPrefix(String value) {
    int idx;
    if (value != null && (idx = value.indexOf(":")) != -1) {
      value = value.substring(idx + 1);
    }
    return value;
  }

  public static String nodeToString(Node node) throws TransformerException {
    StringWriter sw = new StringWriter();
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute("indent-number", 2);
    Transformer t = transformerFactory.newTransformer();
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.setOutputProperty(OutputKeys.METHOD, "html");
    t.transform(new DOMSource(node), new StreamResult(sw));
    return sw.toString();
  }
}

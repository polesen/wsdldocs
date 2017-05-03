package com.beetio.wsdldocs;

import com.beetio.wsdldocs.model.WsdlFault;
import com.beetio.wsdldocs.model.WsdlInput;
import com.beetio.wsdldocs.model.WsdlMessage;
import com.beetio.wsdldocs.model.WsdlModel;
import com.beetio.wsdldocs.model.WsdlOperation;
import com.beetio.wsdldocs.model.WsdlOutput;
import com.beetio.wsdldocs.model.WsdlPart;
import com.beetio.wsdldocs.model.WsdlPortType;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.beetio.wsdldocs.DomHelper.getAttributeValue;
import static com.beetio.wsdldocs.DomHelper.stripNsPrefix;

public class WsdlDocs {

  private static final String NAMESPACE_URI_WSDL = "http://schemas.xmlsoap.org/wsdl/";

  class DocumentBuilderErrorHandler implements ErrorHandler {
    public void warning(SAXParseException exception) throws SAXException {
      exception.printStackTrace();
    }

    public void error(SAXParseException exception) throws SAXException {
      exception.printStackTrace();
    }

    public void fatalError(SAXParseException exception) throws SAXException {
      exception.printStackTrace();
    }
  }

  private Configuration templatingConfiguration;
  private String wsdlContent;

  public WsdlDocs(TemplateLoader templateLoader, String wsdlContent) throws IOException {
    this.templatingConfiguration = configureTemplateEngine(templateLoader);
    this.wsdlContent = wsdlContent;
  }

  private Configuration configureTemplateEngine(TemplateLoader templateLoader) throws IOException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(templateLoader);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    return cfg;
  }

  public void run(String templateName, Object documentModel, Writer output) throws ParseException, IOException, TemplateException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
    Document document = parseToDom(wsdlContent);

    WsdlModel wsdlModel = domToModel(document);

    emit(templatingConfiguration, templateName, documentModel, wsdlModel, output);
  }

  private Document parseToDom(String wsdl) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    dbf.setNamespaceAware(true);
    dbf.setIgnoringComments(true);
    dbf.setCoalescing(true);
    dbf.setExpandEntityReferences(true);

    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
    docBuilder.setErrorHandler(new DocumentBuilderErrorHandler());

    return docBuilder.parse(new ByteArrayInputStream(wsdl.getBytes("UTF8")));
  }

  private WsdlModel domToModel(Document document) throws XPathExpressionException, TransformerException {
    XPath xpath = XPathFactory.newInstance().newXPath();

    WsdlModel wsdlModel = new WsdlModel();

    NodeList portTypes = (NodeList) xpath.evaluate("/*[local-name() = 'definitions']/*[local-name() = 'portType']", document, XPathConstants.NODESET);
    for (int i = 0; i < portTypes.getLength(); i++) {
      Node portType = portTypes.item(i);
      String portTypeName = getAttributeValue(portType, "name");

      WsdlPortType wsdlPortType = new WsdlPortType(portTypeName);

      NodeList operations = (NodeList) xpath.evaluate("./*[local-name() = 'operation']", portType, XPathConstants.NODESET);
      for (int j = 0; j < operations.getLength(); j++) {
        Node operation = operations.item(j);

        String operationName = getAttributeValue(operation, "name");
        Node operationDocumentationNode = (Node) xpath.evaluate("./*[local-name() = 'documentation']", operation, XPathConstants.NODE);
        String operationDocumentation = operationDocumentationNode == null ? null : operationDocumentationNode.getTextContent();

        Node inputNode = (Node) xpath.evaluate("./*[local-name() = 'input']", operation, XPathConstants.NODE);
        String inputName = getAttributeValue(inputNode, "name");
        String inputMessageName = stripNsPrefix(getAttributeValue(inputNode, "message"));

        WsdlMessage wsdlInputMessage = getWsdlMessage(document, xpath, inputMessageName);
        WsdlInput wsdlInput = new WsdlInput(inputName, wsdlInputMessage);

        Node outputNode = (Node) xpath.evaluate("./*[local-name() = 'output']", operation, XPathConstants.NODE);
        WsdlOutput wsdlOutput = null;
        if (outputNode != null) {
          String outputName = getAttributeValue(outputNode, "name");
          String outputMessageName = stripNsPrefix(getAttributeValue(outputNode, "message"));

          WsdlMessage wsdlOutputMessage = getWsdlMessage(document, xpath, outputMessageName);
          wsdlOutput = new WsdlOutput(outputName, wsdlOutputMessage);
        }

        WsdlOperation wsdlOperation = new WsdlOperation(operationName, operationDocumentation, wsdlInput, wsdlOutput);

        NodeList faults = (NodeList) xpath.evaluate("./*[local-name() = 'fault']", operation, XPathConstants.NODESET);
        for (int k = 0; k < faults.getLength(); k++) {
          Node faultNode = faults.item(k);

          String faultName = getAttributeValue(faultNode, "name");
          String faultMessageName = stripNsPrefix(getAttributeValue(faultNode, "message"));

          WsdlMessage wsdlFaultMessage = getWsdlMessage(document, xpath, faultMessageName);

          wsdlOperation.addFault(new WsdlFault(faultName, wsdlFaultMessage));
        }

        wsdlPortType.addOperation(wsdlOperation);
      }

      wsdlModel.addPortType(wsdlPortType);
    }

    return wsdlModel;
  }

  private WsdlMessage getWsdlMessage(Document document, XPath xpath, String messageName) throws XPathExpressionException, TransformerException {
    Node messageNode = (Node) xpath.evaluate("/*[local-name() = 'definitions']/*[local-name() = 'message' and @name='" + messageName + "']", document, XPathConstants.NODE);
    Node messagePartNode = (Node) xpath.evaluate("./*[local-name() = 'part']", messageNode, XPathConstants.NODE);

    String messagePartName = getAttributeValue(messagePartNode, "name");
    String messagePartElementName = stripNsPrefix(getAttributeValue(messagePartNode, "element"));

    Node partElementNode = (Node) xpath.evaluate("/*[local-name() = 'definitions']/*[local-name() = 'types']/*[local-name() = 'schema']/*[local-name() = 'complexType' and @name='" + messagePartElementName + "']", document, XPathConstants.NODE);

    String rawSchemaType = DomHelper.nodeToString(partElementNode);
    WsdlPart wsdlPart = new WsdlPart(messagePartName, rawSchemaType);

    return new WsdlMessage(wsdlPart);
  }

  private void emit(Configuration templatingConfiguration, String templateName, Object documentModel, WsdlModel wsdlModel, Writer output) throws IOException, TemplateException {
    Template template = templatingConfiguration.getTemplate(templateName);
    Map<String, Object> model = new HashMap<>();
    model.put("wsdl", wsdlModel);
    if (documentModel != null) {
      model.put("document", documentModel);
    }
    template.process(model, output);
  }



}

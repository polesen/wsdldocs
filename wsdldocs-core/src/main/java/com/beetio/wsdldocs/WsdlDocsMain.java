package com.beetio.wsdldocs;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

public class WsdlDocsMain {

  public static void main(String[] args) throws ParseException, IOException, TemplateException {
    Options options = new Options();
    options.addRequiredOption("w", "wsdl-location", true, "The location of the WSDL document to process (URL or local file reference)");
    options.addRequiredOption("f", "output-format", true, "Which output format to produce. One of: 'asciidoc', 'custom'");
    options.addOption("t", "output-template", true, "When 'output-format' is set to 'custom', this specifies the template to render output with");
    options.addOption("o", "output-file", true, "Filename to write output to. Use '-' for stdout, which is also the default if not given.");
    options.addOption("m", "document-model-file", true, "Yaml data file describing key aspects of the WSDL file that is not part of the WSDL itself. Available in the output template context as 'document'.");
    options.addOption("d", "debug", false, "Adds extra output when running");

    boolean debug = false;
    try {
      CommandLineParser cmdLineParser = new DefaultParser();
      CommandLine commandLine = cmdLineParser.parse(options, args);

      debug = commandLine.hasOption("debug");

      String wsdlLocation = commandLine.getOptionValue("wsdl-location");
      String wsdlContent = loadWsdlContent(wsdlLocation);

      String outputFormat = commandLine.getOptionValue("output-format");

      String templateName;
      TemplateLoader templateLoader;
      if (outputFormat.equals("custom")) {
        if (!commandLine.hasOption("output-template")) {
          syntaxAndExit(options, "Missing 'output-template' option when using custom output format");
        }

        String outputTemplate = commandLine.getOptionValue("output-template");
        File outputTemplateFile = new File(outputTemplate);
        if (!outputTemplateFile.exists() && outputTemplateFile.canRead()) {
          syntaxAndExit(options, "Unable to access output template file: " + outputTemplateFile.getAbsolutePath());
        }

        templateName = outputTemplateFile.getName();
        File templateDir = outputTemplateFile.getParentFile();
        if (templateDir == null) {
          templateDir = new File(".");
        }
        templateLoader = new FileTemplateLoader(templateDir);

      } else {
        if (commandLine.hasOption("output-template")) {
          syntaxAndExit(options, "Option 'output-template' only makes sense for 'custom' output formats");
        }

        // load the pre-packages templates
        templateLoader = new ClassTemplateLoader(WsdlDocsMain.class, "/templates");
        templateName = outputFormat + ".ftl";
      }

      Writer output;
      if (commandLine.hasOption("output-file")) {
        String outputFileOptionValue = commandLine.getOptionValue("output-file");
        if ("-".equals(outputFileOptionValue.trim())) {
          output = new OutputStreamWriter(System.out);
        } else {
          output = new FileWriter(outputFileOptionValue);
        }
      } else {
        output = new OutputStreamWriter(System.out);
      }

      Object documentModel = null;
      if (commandLine.hasOption("document-model-file")) {
        File documentModelFile = new File(commandLine.getOptionValue("document-model-file"));
        if (!documentModelFile.exists() || !documentModelFile.canRead()) {
          syntaxAndExit(options, "Unable to access: " + documentModelFile.getAbsolutePath());
        }

        Yaml yaml = new Yaml();
        documentModel = yaml.load(new FileReader(documentModelFile));
      }
      new WsdlDocs(templateLoader, wsdlContent).run(templateName, documentModel, output);

    } catch (Exception e) {
      if (debug) {
        e.printStackTrace();
      }
      syntaxAndExit(options, e.getMessage());
    }
  }

  private static void syntaxAndExit(Options options, String possibleErrorMessage) {
    int exitCode = 0;
    if (possibleErrorMessage != null) {
      exitCode = 1;
      System.err.println(possibleErrorMessage);
    }
    new HelpFormatter().printHelp("wsdldocs", options);
    System.exit(exitCode);
  }

  private static String loadWsdlContent(String wsdlLocation) throws IOException {
    InputStream wsdlStream;
    if (wsdlLocation.startsWith("http://") || wsdlLocation.startsWith("https://")) {
      URL wsdlUrl = new URL(wsdlLocation);
      wsdlStream = wsdlUrl.openStream();
    } else {
      File wsdlFile = new File(wsdlLocation);
      if (!wsdlFile.exists() || !wsdlFile.canRead()) {
        throw new IOException("Unable to read wsdl file: " + wsdlFile.getAbsolutePath());
      }
      wsdlStream = new FileInputStream(wsdlFile);
    }

    BufferedReader wsdlReader = new BufferedReader(new InputStreamReader(wsdlStream, "UTF8"));
    StringBuilder wsdlContent = new StringBuilder();
    String line;
    while ((line = wsdlReader.readLine()) != null) {
      wsdlContent.append(line);
    }

    return wsdlContent.toString();
  }


}

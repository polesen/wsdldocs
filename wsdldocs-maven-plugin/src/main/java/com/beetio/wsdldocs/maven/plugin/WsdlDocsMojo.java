package com.beetio.wsdldocs.maven.plugin;

import com.beetio.wsdldocs.OutputFormat;
import com.beetio.wsdldocs.WsdlDocsMain;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

@Mojo(name = "wsdldocs", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class WsdlDocsMojo extends AbstractMojo {

  @Parameter
  private File wsdlFile;

  @Parameter
  private URL wsdlUrl;

  @Parameter(required = true)
  private String outputFile;

  @Parameter(required = true)
  private OutputFormat outputFormat;

  @Parameter
  private File outputTemplateFile;

  @Parameter
  private File documentModelFile;

  @Parameter
  private boolean debug;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (wsdlFile == null && wsdlUrl == null) {
      throw new MojoExecutionException("One of 'wsdlFile' or 'wsdlUrl' must be provided");
    }
    if (wsdlFile != null && wsdlUrl != null) {
      throw new MojoExecutionException("Use either 'wsdlFile' or 'wsdlUrl', not both");
    }

    String wsdlLocation = wsdlFile == null ? wsdlUrl.toExternalForm() : wsdlFile.getAbsolutePath();

    Collection<String> args = new ArrayList<>();
    args.add("--wsdl-location");
    args.add(wsdlLocation);

    args.add("--output-format");
    args.add(outputFormat.name().toLowerCase());

    args.add("--output-file");
    args.add(outputFile);

    if (outputTemplateFile != null) {
      args.add("--output-template");
      args.add(outputTemplateFile.getAbsolutePath());
    }

    if (documentModelFile != null) {
      args.add("--document-model-file");
      args.add(documentModelFile.getAbsolutePath());
    } else {
      getLog().warn("Option 'documentModelFile' missing. Consider using it to apply extra document information to output (see freemarker template)");
    }

    if (debug) {
      args.add("--debug");
    }

    try {
      WsdlDocsMain.main(args.toArray(new String[args.size()]));
      getLog().info("Rendered output file: " + outputFile);
    } catch (Exception e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }
}

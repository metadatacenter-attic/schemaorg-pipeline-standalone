package org.metadatacenter.schemaorg.pipeline.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import org.metadatacenter.schemaorg.pipeline.Pipeline;
import org.metadatacenter.schemaorg.pipeline.operation.extract.XsltTransformer;
import org.metadatacenter.schemaorg.pipeline.operation.transform.SchemaToRdf;
import org.metadatacenter.schemaorg.pipeline.operation.transform.XmlToSchema;
import org.metadatacenter.schemaorg.pipeline.operation.translate.MapNodeTranslator;
import org.metadatacenter.schemaorg.pipeline.operation.translate.XsltTranslatorHandler;

public class GraphDBPipeline {

  private static final NumberFormat numberFormatter = NumberFormat.getInstance();

  private static double counter = 0;

  /*
   * Example use:
   * $ java GraphDBPipeline "mapping.caml" "data.xml" 
   */
  public static void main(String[] args) {
    
    String mappingArgument = args[0];
    Path mappingPath = Paths.get(mappingArgument);
    
    String dataArgument = args[1];
    Path dataSourcePath = Paths.get(dataArgument);
    
    String mappingString = FileUtils.readDocument(mappingPath);
    String stylesheet = MapNodeTranslator.translate(new XsltTranslatorHandler(), mappingString);
    XsltTransformer transformer = XsltTransformer.newTransformer(stylesheet);
    
    counter = 0;
    if (isDirectory(dataSourcePath)) {
      processInputDirectory(dataSourcePath, transformer);
    } else {
      processInputFile(dataSourcePath, transformer);
    }
    System.out.println("Successfully generating " + numberFormatter.format(counter) + " pages.");
  }

  private static boolean isDirectory(Path path) {
    return Files.isDirectory(path);
  }

  private static void processInputDirectory(Path inputPath, final XsltTransformer transformer) {
    try {
      Files.walk(inputPath)
        .filter(path -> path.toString().endsWith(".xml"))
        .forEach(path -> processInputFile(path, transformer));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processInputFile(Path inputPath, final XsltTransformer transformer) {
    System.out.println("Processing " + inputPath.getFileName());
    String input = FileUtils.readDocument(inputPath);
    String output = Pipeline.create()
        .pipe(transformer::transform)
        .pipe(XmlToSchema::transform)
        .pipe(SchemaToRdf::transform)
        .run(input);
    Path outputPath = FileUtils.renameFileExtension(inputPath, "rdf");
    FileUtils.writeDocument(output, outputPath);
    counter++;
  }
}

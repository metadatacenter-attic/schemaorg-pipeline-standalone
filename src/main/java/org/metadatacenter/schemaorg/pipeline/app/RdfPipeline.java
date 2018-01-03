package org.metadatacenter.schemaorg.pipeline.app;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.metadatacenter.schemaorg.pipeline.Pipeline;
import org.metadatacenter.schemaorg.pipeline.operation.embed.SchemaToHtml;
import org.metadatacenter.schemaorg.pipeline.operation.extract.SparqlEndpointClient;
import org.metadatacenter.schemaorg.pipeline.operation.transform.RdfToSchema;
import org.metadatacenter.schemaorg.pipeline.operation.translate.MapNodeTranslator;
import org.metadatacenter.schemaorg.pipeline.operation.translate.SparqlConstructTranslatorHandler;
import org.metadatacenter.schemaorg.pipeline.operation.translate.TranslatorHandler;

public class RdfPipeline {

  /*
   * Example use:
   * $ java RdfPipeline "mapping.caml" "http://bio2rdf.org/sparql" "http://bio2rdf.org/drugbank:DB00001"
   */
  public static void main(String[] args) {

    String mappingArgument = args[0];
    Path mappingPath = Paths.get(mappingArgument);
    
    String endpointAddress = args[1];
    String drugIdentifier = args[2];

    String mappingString = FileUtils.readDocument(mappingPath);
    TranslatorHandler  handler = new SparqlConstructTranslatorHandler();
    String query = MapNodeTranslator.translate(handler, mappingString);

    SparqlEndpointClient sparqlClient = new SparqlEndpointClient(endpointAddress);

    try {
      System.out.println("Processing " + drugIdentifier);
      String output = Pipeline.create()
          .pipe(s -> sparqlClient.evaluatePreparedQuery(s, drugIdentifier))
          .pipe(RdfToSchema::transform)
          .pipe(SchemaToHtml::transform)
          .run(query);
      FileUtils.writeDocument(output, Paths.get("output.html"));
      System.out.println("Done.");
    } catch (Exception e) {
      System.err.println("Failed " + drugIdentifier);
    }
  }
}

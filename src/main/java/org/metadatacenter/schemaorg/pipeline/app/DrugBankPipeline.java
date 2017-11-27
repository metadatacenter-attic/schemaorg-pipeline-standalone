package org.metadatacenter.schemaorg.pipeline.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.metadatacenter.schemaorg.pipeline.Pipeline;
import org.metadatacenter.schemaorg.pipeline.operation.embed.SchemaToHtml;
import org.metadatacenter.schemaorg.pipeline.operation.extract.SparqlEndpointClient;
import org.metadatacenter.schemaorg.pipeline.operation.transform.RdfToSchema;
import org.metadatacenter.schemaorg.pipeline.operation.translate.MapNodeTranslator;
import org.metadatacenter.schemaorg.pipeline.operation.translate.SparqlConstructTranslatorHandler;
import org.metadatacenter.schemaorg.pipeline.operation.translate.TranslatorHandler;

public class DrugBankPipeline {

  /*
   * Example use:
   * $ java DrugBankPipeline "http://bio2rdf.org/drugbank:DB00001"
   */
  public static void main(String[] args) {

    String drugIdentifier = args[0];

    TranslatorHandler  handler = new SparqlConstructTranslatorHandler();
    String query = MapNodeTranslator.translate(handler, DRUGBANK_MAPPING);

    SparqlEndpointClient bio2rdf = SparqlEndpointClient.BIO2RDF;

    try {
      System.out.println("Processing " + drugIdentifier);
      String output = Pipeline.create()
          .pipe(s -> bio2rdf.evaluatePreparedQuery(s, drugIdentifier))
          .pipe(RdfToSchema::transform)
          .pipe(SchemaToHtml::transform)
          .run(query);
      writeDocument(output);
      System.out.println("Done.");
    } catch (Exception e) {
      System.err.println("Failed " + drugIdentifier);
    }
  }

  private static void writeDocument(String content) {
    try {
      Files.write(Paths.get("output.html"), content.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String DRUGBANK_MAPPING =
      "@prefix:              ('schema', 'http://schema.org/')\n" + 
      "@prefix:              ('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')\n" +
      "@prefix:              ('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')\n" + 
      "@prefix:              ('dcterms', 'http://purl.org/dc/terms/')\n" + 
      "@prefix:              ('db', 'http://bio2rdf.org/drugbank_vocabulary:')\n" + 
      "@prefix:              ('bio2rdf', 'http://bio2rdf.org/bio2rdf_vocabulary:')\n" + 
      "@type:                ('Drug', 'db:Drug')\n" + 
      "name:                 /dcterms:title\n" + 
      "description:          /dcterms:description\n" + 
      "identifier:           /dcterms:identifier\n" + 
      "url:                  /bio2rdf:uri\n" + 
      "sameAs:               /rdfs:seeAlso\n" + 
      "proprietaryName:      /db:brand/dcterms:title\n" + 
      "nonProprietaryName:   /db:synonym/dcterms:title\n" + 
      "clinicalPharmacology: /db:pharmacodynamics/dcterms:description\n" + 
      "drugClass:            /db:category/dcterms:title\n" + 
      "cost:                 /db:product\n" + 
      "   @type:             'DrugCost'\n" + 
      "   costPerUnit:       /db:price\n" + 
      "   costCurrency:      'USD'\n" + 
      "   drugUnit:          /dcterms:title\n" +
      "availableStrength:    /db:dosage\n" + 
      "   @type:             'DrugStrength'\n" + 
      "   description:       /dcterms:title\n" + 
      "administrationRoute:  /db:dosage/db:route/dcterms:title\n" + 
      "administrationForm:   /db:dosage/db:form/dcterms:title\n" + 
      "mechanismOfAction:    /db:mechanism-of-action/dcterms:description\n" + 
      "interactingDrug:      /db:ddi-interactor-in/dcterms:title\n" + 
      "foodWarning:          /db:food-interaction/rdf:value\n" + 
      "legalStatus:          /db:group/bio2rdf:identifier\n" + 
      "manufacturer:         /db:manufacturer\n" + 
      "   @type:             'Organization'\n" + 
      "   name:              /rdf:value";
}

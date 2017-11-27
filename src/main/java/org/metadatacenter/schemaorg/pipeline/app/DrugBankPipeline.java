package org.metadatacenter.schemaorg.pipeline.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
   * $ java DrugBankPipeline "src/test/resource/clinicaltrials.xml"
   */
  public static void main(String[] args) {

    String input = args[0];

    TranslatorHandler  handler = new SparqlConstructTranslatorHandler();
    String query = MapNodeTranslator.translate(handler, DRUGBANK_MAPPING);

    SparqlEndpointClient bio2rdf = SparqlEndpointClient.BIO2RDF;

    List<String> drugIdentifiers = getIdentifiersFromInput(input);
    for (String drugIdentifier : drugIdentifiers) {
      try {
        System.out.println("Processing " + drugIdentifier);
        String output = Pipeline.create()
            .pipe(s -> bio2rdf.evaluatePreparedQuery(s, drugIdentifier))
            .pipe(RdfToSchema::transform)
            .pipe(SchemaToHtml::transform)
            .run(query);
        writeDocument(toHtmlFile(drugIdentifier), output);
      } catch (Exception e) {
        System.err.println("Failed " + drugIdentifier);
      }
    }
  }

  private static List<String> getIdentifiersFromInput(String input) {
    return Arrays.stream(input.split(","))
        .map(String::trim)
        .collect(Collectors.toList());
  }

  private static void writeDocument(String path, String content) {
    try {
      Files.write(Paths.get(path), content.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String toHtmlFile(String graphIri) {
    try {
      return URLEncoder.encode(graphIri, "UTF-8") + ".html";
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String DRUGBANK_MAPPING =
      "@prefix:              ('schema', 'http://schema.org/')\n" + 
      "@prefix:              ('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')\n" + 
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
      "   @type:             DrugCost\n" + 
      "   costPerUnit:       /db:price\n" + 
      "   costCurrency:      USD\n" + 
      "   drugUnit:          /dcterms:title\n" +
      "availableStrength:    /db:dosage\n" + 
      "   @type:             DrugStrength\n" + 
      "   description:       /dcterms:title\n" + 
      "administrationRoute:  /db:dosage/db:route/dcterms:title\n" + 
      "administrationForm:   /db:dosage/db:form/dcterms:title\n" + 
      "mechanismOfAction:    /db:mechanism-of-action/dcterms:description\n" + 
      "interactingDrug:      /db:ddi-interactor-in/dcterms:title\n" + 
      "foodWarning:          /db:food-interaction/rdf:value\n" + 
      "availableStrength:    /db:dosage\n" + 
      "   @type:             DrugStrength\n" + 
      "   description:       /dcterms:title\n" + 
      "legalStatus:          /db:group/bio2rdf:identifier\n" + 
      "manufacturer:         /db:manufacturer\n" + 
      "   @type:             Organization\n" + 
      "   name:              /rdf:value";
}

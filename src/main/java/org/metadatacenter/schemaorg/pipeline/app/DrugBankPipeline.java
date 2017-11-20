package org.metadatacenter.schemaorg.pipeline.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.metadatacenter.schemaorg.pipeline.api.Pipeline;
import org.metadatacenter.schemaorg.pipeline.embed.SchemaToHtml;
import org.metadatacenter.schemaorg.pipeline.extract.SparqlEndpointClient;
import org.metadatacenter.schemaorg.pipeline.mapping.MapNodeTranslator;
import org.metadatacenter.schemaorg.pipeline.mapping.TranslatorHandler;
import org.metadatacenter.schemaorg.pipeline.mapping.translator.SparqlConstructTranslatorHandler;
import org.metadatacenter.schemaorg.pipeline.transform.RdfToSchema;

public class DrugBankPipeline {

  public static void main(String[] args) {

    String inputFile = args[0];

    TranslatorHandler handler = createTranslatorHandler();
    String query = MapNodeTranslator.translate(handler, DRUGBANK_MAPPING);

    SparqlEndpointClient bio2rdf = SparqlEndpointClient.BIO2RDF;

    for (String graphIri : getInputList(inputFile)) {
      try {
        System.out.println("Processing " + graphIri);
        String output = Pipeline.create()
            .pipe(s -> bio2rdf.evaluatePreparedQuery(s, graphIri))
            .pipe(RdfToSchema::transform)
            .pipe(SchemaToHtml::transform)
            .run(query);
        writeDocument(toHtmlFile(graphIri), output);
      } catch (Exception e) {
        System.err.println("Failed " + graphIri);
      }
    }
  }

  private static SparqlConstructTranslatorHandler createTranslatorHandler() {
    SparqlConstructTranslatorHandler handler = new SparqlConstructTranslatorHandler();
    handler.addPrefix("schema", "http://schema.org/");
    handler.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    handler.addPrefix("db", "http://bio2rdf.org/drugbank_vocabulary:");
    handler.addPrefix("bio2rdf", "http://bio2rdf.org/bio2rdf_vocabulary:");
    handler.setInstanceType("db:Drug");
    return handler;
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

  private static List<String> getInputList(String path) {
    List<String> list = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
      list = br.lines().collect(Collectors.toList());
      return list;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String DRUGBANK_MAPPING =
      "@type:                Drug\n" + 
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

package org.metadatacenter.schemaorg.pipeline.app;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.metadatacenter.schemaorg.pipeline.Pipeline;
import org.metadatacenter.schemaorg.pipeline.operation.embed.SchemaToHtml;
import org.metadatacenter.schemaorg.pipeline.operation.extract.XsltTransformer;
import org.metadatacenter.schemaorg.pipeline.operation.transform.XmlToSchema;
import org.metadatacenter.schemaorg.pipeline.operation.translate.MapNodeTranslator;
import org.metadatacenter.schemaorg.pipeline.operation.translate.XsltTranslatorHandler;

public class ClinicalTrialPipeline {

  /*
   * Example use:
   * $ java ClinicalTrialPipeline "src/test/resources/clinicaltrials.xml"
   */
  public static void main(String[] args) {
    
    String inputFileLocation = args[0];
    
    String stylesheet = MapNodeTranslator.translate(new XsltTranslatorHandler(), CLINICAL_TRIALS_MAPPING);
    
    XsltTransformer transformer = XsltTransformer.newTransformer(stylesheet);
    
    System.out.println("Processing " + inputFileLocation);
    String output = Pipeline.create()
        .pipe(transformer::transform)
        .pipe(XmlToSchema::transform)
        .pipe(SchemaToHtml::transform)
        .run(readDocument(inputFileLocation));
    writeDocument(output);
    System.out.println("Done.");
  }

  private static String readDocument(String path) {
    try (InputStream in = new FileInputStream(path)) {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
      return result.toString("UTF-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void writeDocument(String content) {
    try {
      Files.write(Paths.get("output.html"), content.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final String CLINICAL_TRIALS_MAPPING =
      "@type:                       'MedicalTrial'\n" + 
      "additionalType:              'clinicaltrials'\n" +
      "name:                        /clinical_study/official_title\n" + 
      "alternateName:               /clinical_study/brief_title\n" + 
      "alternateName:               /clinical_study/acronym\n" +
      "identifier:                  /clinical_study/id_info/org_study_id\n" + 
      "identifier:                  /clinical_study/id_info/nct_id\n" + 
      "identifier:                  /clinical_study/id_info/secondary_id\n" + 
      "status:                      /clinical_study/overall_status\n" +
      "description:                 /clinical_study/detailed_description/textblock\n" +
      "disambiguatingDescription:   /clinical_study/brief_summary/textblock\n" +
      "studySubject:                /clinical_study/condition\n" +
      "code:                        /clinical_study/keyword\n" +
      "    @type:                   'MedicalCode'\n" +
      "    codeValue:               /.\n" +
      "phase:                       /clinical_study/phase\n" +
      "trialDesign:                 /clinical_study/study_design_info/intervention_model\n" +
      "population:                  /clinical_study/eligibility/criteria/textblock\n" +
      "sponsor:                     /clinical_study/sponsors/lead_sponsor\n" + 
      "    @type:                   'Organization'\n" + 
      "    name:                    /agency\n" + 
      "    additionalType:          'Lead Sponsor'\n" + 
      "sponsor:                     /clinical_study/sponsors/collaborator\n" + 
      "    @type:                   'Organization'\n" + 
      "    name:                    /agency\n" + 
      "    additionalType:          'Collaborator'\n" + 
      "studyLocation:               /clinical_study/location/facility\n" + 
      "    @type:                   'AdministrativeArea'\n" + 
      "    name:                    /name\n" +
      "    additionalType:          'Facility'\n" +
      "    address:                 /address\n" +
      "        @type:               'PostalAddress'\n" +
      "        addressLocality:     /city\n" + 
      "        addressRegion:       /state\n" + 
      "        postalCode:          /zip\n" + 
      "        addressCountry:      /country\n" +
      "studyLocation:               /clinical_study/location_countries\n" +
      "    @type:                   'AdministrativeArea'\n" +
      "    name:                    /country\n" +
      "    additionalType:          'Country Location'\n" +
      "subjectOf:                   /clinical_study/references\n" +
      "    @type:                   'CreativeWork'\n" +
      "    additionalType:          'pubmed'\n" +
      "    identifier:              /PMID\n" +
      "    alternateName:           /citation\n" +
      "subjectOf:                   /clinical_study/results_reference\n" +
      "    @type:                   'CreativeWork'\n" +
      "    additionalType:          'pubmed'\n" +
      "    identifier:              /PMID\n" +
      "    alternateName:           /citation\n" +
      "subjectOf:                   /clinical_study/link\n" +
      "    @type:                   'WebPage'\n" +
      "    url:                     /url\n" +
      "    description:             /description";
}

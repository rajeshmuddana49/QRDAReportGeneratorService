package com.digitalhie.QRDAReportGenerator.util;

import com.digitalhie.QRDAReportGenerator.model.Address;
import com.digitalhie.QRDAReportGenerator.model.PatientData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class CCDGenerator {

    Logger logger = LoggerFactory.getLogger(CCDGenerator.class);
    public String createQRDA3(String templateFilePath, JsonNode input) throws Exception {

        Map<String, String> referenceValues = new HashMap<>();
        referenceValues.put("initial-population","IPOP");
        referenceValues.put("numerator","NUMER");
        referenceValues.put("denominator","DENOM");
        referenceValues.put("denominator-exclusion","DENEX");

        Map<String, String> period = extractPeriod(input);
        Map<String, Integer> populationCounts =  extractPopulationCounts(input, referenceValues);

        ConsolPackage.eINSTANCE.eClass();
        InputStream cpResource = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(templateFilePath);
        ClinicalDocument oClinicalDocument = CDAUtil.load(cpResource); //Loads CDADocument.

        // Find measure section with templateId
        oClinicalDocument.getSections().forEach(section -> {
            for(II templateId: section.getTemplateIds()) {
                // Check by templateId for measures section
                if(templateId.getRoot() !=null && templateId.getRoot().equals("2.16.840.1.113883.10.20.24.2.2")) {
                    section.getActs().forEach(act -> {
                        if(act.getCode().getDisplayName().equalsIgnoreCase("Observation Parameters")) {
                            IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS(period.get("start"), period.get("end"));
                            act.setEffectiveTime(effectiveTime);
                        }
                    });
                    section.getOrganizers().forEach(organizer -> {
                        organizer.getComponents().forEach(component4 -> {
                            Observation observation = component4.getObservation();
                            observation.getEntryRelationships().forEach(entryRelationship -> {
                                if(entryRelationship.getObservation().getCode().getCode().equalsIgnoreCase("MSRAGG")) {
                                    String code = ((CD)observation.getValues().stream().findFirst().get()).getCode();
                                    if(populationCounts.containsKey(code)) {
                                        INT value = (INT) entryRelationship.getObservation().getValues().stream().findFirst().get();
                                        value.setValue(populationCounts.get(code));
                                    }
                                }
                            });
                        });
                    });
                }
            }
        });

        // write to file
        String fileName = UUID.randomUUID()+"_qrda3_ccd_file.xml";
        FileOutputStream fos = new FileOutputStream(fileName);
        CDAUtil.save(oClinicalDocument, fos);
        fos.close();
        if(cpResource!=null)
            cpResource.close();

        return fileName;
    }

    public String createQRDA1(String templateFilePath, JsonNode input) throws Exception {

        ConsolPackage.eINSTANCE.eClass();
        InputStream cpResource = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(templateFilePath);
        ClinicalDocument oClinicalDocument = CDAUtil.load(cpResource); //Loads CDADocument.

        PatientData patientData = extractPatientData(input);

        PatientRole patientRole = oClinicalDocument.getPatientRoles().get(0);
        for(II id : patientRole.getIds()) {
            if(id.getRoot().equalsIgnoreCase("2.16.840.1.113883.3.249.15")) {
                id.setExtension(patientData.getPatientId());
            }
        }
        AD address = patientRole.getAddrs().get(0);
        FeatureMap addressMixed = address.getMixed();
        addressMixed.forEach(a -> {
            if(a.getEStructuralFeature().getName().equalsIgnoreCase("streetAddressLine")) {
                EStructuralFeature eStructuralFeature = a.getEStructuralFeature();
                ADXP addr = (ADXP) a.getValue();
                addr.getMixed().setValue(0, patientData.getAddress().getStreetAddressLine());
            } else if(a.getEStructuralFeature().getName().equalsIgnoreCase("city")) {
                EStructuralFeature eStructuralFeature = a.getEStructuralFeature();
                ADXP addr = (ADXP) a.getValue();
                addr.getMixed().setValue(0, patientData.getAddress().getCity());
            } else if(a.getEStructuralFeature().getName().equalsIgnoreCase("postalCode")) {
                EStructuralFeature eStructuralFeature = a.getEStructuralFeature();
                ADXP addr = (ADXP) a.getValue();
                addr.getMixed().setValue(0, patientData.getAddress().getPostalCode());
            } else if(a.getEStructuralFeature().getName().equalsIgnoreCase("country")) {
                EStructuralFeature eStructuralFeature = a.getEStructuralFeature();
                ADXP addr = (ADXP) a.getValue();
                addr.getMixed().setValue(0, patientData.getAddress().getCountry());
            } else if(a.getEStructuralFeature().getName().equalsIgnoreCase("state")) {
                EStructuralFeature eStructuralFeature = a.getEStructuralFeature();
                ADXP addr = (ADXP) a.getValue();
                addr.getMixed().setValue(0, " ");
            }
        });
        Patient patient = patientRole.getPatient();

        patient.getAdministrativeGenderCode().setCode(patientData.getGender());
        patient.getBirthTime().setValue(patientData.getDob());
        PN name = patient.getNames().stream().findFirst().get();
        ENXP givenName = name.getGivens().stream().findFirst().get();
        givenName.getMixed().setValue(0, patientData.getFirstName());
        ENXP familyName = name.getFamilies().stream().findFirst().get();
        familyName.getMixed().setValue(0, patientData.getLastName());

        // write to file
        String fileName = UUID.randomUUID()+"_qrda1_ccd_file.xml";
        FileOutputStream fos = new FileOutputStream(fileName);
        CDAUtil.save(oClinicalDocument, fos);
        fos.close();
        if(cpResource!=null)
            cpResource.close();

        return fileName;
    }

    private PatientData extractPatientData(JsonNode input) {
        PatientData patientData = new PatientData();
        ArrayNode arrayNode = (ArrayNode) input.get("contained");
        arrayNode.forEach(e -> {
            if(e.get("resourceType").asText().equalsIgnoreCase("Patient")) {
                patientData.setPatientId(e.get("id").asText());
                JsonNode name = ((ArrayNode) e.get("name")).get(0);
                patientData.setFirstName(((ArrayNode) name.get("given")).get(0).asText());
                patientData.setLastName(name.get("family").asText());
                patientData.setDob(formatDate(e.get("birthDate").asText()));
                patientData.setGender(getGenderCode(e.get("gender").asText()));
                Address address = new Address();
                JsonNode addr = ((ArrayNode)e.get("address")).get(0);
                address.setStreetAddressLine( ((ArrayNode)addr.get("line")).get(0).asText());
                address.setCity(addr.get("city").asText());
                address.setPostalCode(addr.get("postalCode").asText());
                address.setCountry(addr.get("country").asText());
                patientData.setAddress(address);
            }
        });
        return patientData;
    }
    private String getGenderCode(String gender) {
        if(gender.equalsIgnoreCase("male")) {
            return "M";
        } else {
            return "F";
        }
    }
    private String formatDate(String date) {
        return date.replaceAll("-","").substring(0,8);
    }


    private Map<String, String> extractPeriod(JsonNode input) {
        Map<String, String> period = new HashMap<>();
        period.put("start", formatDate(input.get("period").get("start").asText()));
        period.put("end", formatDate(input.get("period").get("end").asText()));
        return period;
    }

    private Map<String, Integer> extractPopulationCounts(JsonNode input, Map<String, String> referenceValues) {
        Map<String, Integer> counts = new HashMap<>();
        ArrayNode arrayNode = (ArrayNode)((input.get("group")).get(0).get("population"));
        arrayNode.forEach(e -> {
            String code = e.get("code").get("coding").get(0).get("code").asText();
            Integer count = e.get("count").asInt();
            counts.put(referenceValues.getOrDefault(code, code), count);
        });
        return counts;
    }
}

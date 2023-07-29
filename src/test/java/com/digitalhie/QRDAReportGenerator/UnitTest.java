package com.digitalhie.QRDAReportGenerator;

import com.digitalhie.QRDAReportGenerator.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/*
    This file used for local testing. Do not delete this file till we finish the project
 */
public class UnitTest {

    public static void main(String[] args) throws Exception {

        String templateFilePath =  "templates/2023-CMS-QRDA-I-v1.2-Sample-File.xml";
        ObjectMapper mapper = new ObjectMapper();
        File from = new File("src/test/resources/templates/sample-qrda1-input.json");
        JsonNode input = mapper.readTree(from);

        Map<String, String> referenceValues = new HashMap<>();
        referenceValues.put("initial-population","IPOP");
        referenceValues.put("numerator","NUMER");
        referenceValues.put("denominator","DENOM");
        referenceValues.put("denominator-exclusion","DENEX");

        PatientData patientData = extractPatientData(input);
        System.out.println(patientData);

        EncounterData encounterData = extractEncounterData(input);
        System.out.println(encounterData);

        ObservationData observationData = extractObservationData(input);
        System.out.println(observationData);

        /*Map<String, String> period = extractPeriod(input);
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
        });*/

        // write to file
        /*String fileName = UUID.randomUUID()+"_qrda3_ccd_file.xml";
        FileOutputStream fos = new FileOutputStream(fileName);
        CDAUtil.save(oClinicalDocument, fos);
        fos.close();
        if(cpResource!=null)
            cpResource.close();*/

    }

    private static Map<String, String> extractPeriod(JsonNode input) {
        Map<String, String> period = new HashMap<>();
        period.put("start", formatDate(input.get("period").get("start").asText()));
        period.put("end", formatDate(input.get("period").get("end").asText()));
        return period;
    }

    private static Map<String, Integer> extractPopulationCounts(JsonNode input, Map<String, String> referenceValues) {
        Map<String, Integer> counts = new HashMap<>();
        ArrayNode arrayNode = (ArrayNode)((input.get("group")).get(0).get("population"));
        arrayNode.forEach(e -> {
            String code = e.get("code").get("coding").get(0).get("code").asText();
            Integer count = e.get("count").asInt();
            counts.put(referenceValues.getOrDefault(code, code), count);
        });
        return counts;
    }
    private static PatientData extractPatientData(JsonNode input) {
        PatientData patientData = new PatientData();
        ArrayNode arrayNode = (ArrayNode) input.get("entry");
        for(JsonNode e : arrayNode) {
            if(e.get("resource").get("resourceType").asText().equalsIgnoreCase("Patient")) {
                patientData.setPatientId(e.get("resource").get("id").asText());
                JsonNode name = ((ArrayNode) e.get("resource").get("name")).get(0);
                patientData.setFirstName(((ArrayNode) name.get("given")).get(0).asText());
                patientData.setLastName(name.get("family").asText());
                patientData.setDob(formatDate(e.get("resource").get("birthDate").asText()));
                patientData.setGender(getGenderCode(e.get("resource").get("gender").asText()));
                /*
                Address address = new Address();
                JsonNode addr = ((ArrayNode)e.get("address")).get(0);
                address.setStreetAddressLine( ((ArrayNode)addr.get("line")).get(0).asText());
                address.setCity(addr.get("city").asText());
                address.setPostalCode(addr.get("postalCode").asText());
                address.setCountry(addr.get("country").asText());
                patientData.setAddress(address);
                */
                ArrayNode extensions = (ArrayNode) e.get("resource").get("extension");
                extensions.forEach(extension -> {
                    if(extension.get("url").asText().endsWith("us-core-race")) {
                        ((ArrayNode) extension.get("extension")).forEach(ext -> {
                            if(ext.get("url").asText().equalsIgnoreCase("ombCategory")) {
                                patientData.setRaceCode(ext.get("valueCoding").get("code").asText());
                                patientData.setRaceDisplayName(ext.get("valueCoding").get("display").asText());
                            }
                        });
                    }
                    else if(extension.get("url").asText().endsWith("us-core-ethnicity")) {
                        ((ArrayNode) extension.get("extension")).forEach(ext -> {
                            if(ext.get("url").asText().equalsIgnoreCase("ombCategory")) {
                                patientData.setEthnicityCode(ext.get("valueCoding").get("code").asText());
                                patientData.setEthnicityDisplayName(ext.get("valueCoding").get("display").asText());
                            }
                        });
                    }
                });
                break;
            }
        }
        return patientData;
    }

    private static EncounterData extractEncounterData(JsonNode input) {
        EncounterData encounterData = new EncounterData();
        ArrayNode arrayNode = (ArrayNode) input.get("entry");
        for(JsonNode e : arrayNode) {
            if(e.get("resource").get("resourceType").asText().equalsIgnoreCase("Encounter")) {
                encounterData.setEncounterId(e.get("resource").get("id").asText());
                JsonNode coding = (((e.get("resource").get("type")).get(0)).get("coding")).get(0);
                encounterData.setCode(coding.get("code").asText());
                encounterData.setDisplayName(coding.get("display").asText());
                encounterData.setPeriodStartDate(formatDate(e.get("resource").get("period").get("start").asText()));
                encounterData.setPeriodEndDate(formatDate(e.get("resource").get("period").get("end").asText()));
                break;
            }
        }
        return encounterData;
    }

    private static ObservationData extractObservationData(JsonNode input) {
        ObservationData observationData = new ObservationData();
        ArrayNode arrayNode = (ArrayNode) input.get("entry");
        for(JsonNode e : arrayNode) {
            if(e.get("resource").get("resourceType").asText().equalsIgnoreCase("Observation")) {
                observationData.setObservationId(e.get("resource").get("id").asText());
                JsonNode coding = (e.get("resource").get("code").get("coding")).get(0);
                observationData.setCode(coding.get("code").asText());
                observationData.setDisplayName(coding.get("display").asText());
                observationData.setEffectiveDateTime(formatDate(e.get("resource").get("effectiveDateTime").asText()));

                JsonNode participantRoleCoding = (e.get("resource").get("valueCodeableConcept").get("coding")).get(0);
                observationData.setParticipantRoleCode(participantRoleCoding.get("code").asText());
                observationData.setParticipantRoleDisplayName(participantRoleCoding.get("display").asText());

                ArrayNode components = (ArrayNode) e.get("resource").get("component");
                List<ComponentData> componentDataList = new ArrayList<>();
                components.forEach(component -> {
                    ComponentData componentData = new ComponentData();
                    componentData.setCode(component.get("code").get("coding").get(0).get("code").asText());
                    componentData.setDisplayName(component.get("code").get("coding").get(0).get("display").asText());
                    componentData.setValue(component.get("valueQuantity").get("value").asText());
                    componentData.setUnit(component.get("valueQuantity").get("unit").asText());
                    componentData.setValueCode(component.get("valueQuantity").get("code").asText());
                    componentDataList.add(componentData);
                });
                observationData.setComponents(componentDataList);
                break;
            }
        }
        return observationData;
    }
    private static String getGenderCode(String gender) {
        if(gender.equalsIgnoreCase("male")) {
            return "M";
        } else {
            return "F";
        }
    }
    private static String formatDate(String date) {
        return date.replaceAll("-","").substring(0, 8);
    }
}

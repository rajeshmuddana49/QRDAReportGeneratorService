package com.digitalhie.QRDAReportGenerator.util;

import com.digitalhie.QRDAReportGenerator.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
        EncounterData encounterData = extractEncounterData(input);
        ObservationData observationData = extractObservationData(input);

        PatientRole patientRole = oClinicalDocument.getPatientRoles().get(0);
        for(II id : patientRole.getIds()) {
            if(id.getRoot().equalsIgnoreCase("2.16.840.1.113883.3.249.15")) {
                id.setExtension(patientData.getPatientId());
            }
        }
        /*
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
        */
        Patient patient = patientRole.getPatient();

        patient.getAdministrativeGenderCode().setCode(patientData.getGender());

        patient.getBirthTime().setValue(patientData.getDob());

        PN name = patient.getNames().stream().findFirst().get();
        ENXP givenName = name.getGivens().stream().findFirst().get();
        givenName.getMixed().setValue(0, patientData.getFirstName());
        ENXP familyName = name.getFamilies().stream().findFirst().get();
        familyName.getMixed().setValue(0, patientData.getLastName());

        CE raceCode = patient.getRaceCode();
        raceCode.setCode(patientData.getRaceCode());
        raceCode.setDisplayName(patientData.getRaceDisplayName());

        CE ethnicGroupCode = patient.getEthnicGroupCode();
        ethnicGroupCode.setCode(patientData.getEthnicityCode());
        ethnicGroupCode.setDisplayName(patientData.getEthnicityDisplayName());

        // Find Patient section with templateId
        Section patientSection = oClinicalDocument.getSections().stream()
                .filter(section -> {
                    for(II templateId: section.getTemplateIds()) {
                        if(templateId.getRoot() !=null && templateId.getRoot().equals("2.16.840.1.113883.10.20.17.2.4")) {
                            return true;
                        }
                    }
                    return false;
                }).findFirst().orElseThrow(() -> new Exception("Patient section not found at the template"));

        Encounter encounterEntry = patientSection.getEncounters().stream().findFirst().get();
        modifyEncounter(encounterEntry, encounterData);

        Observation diagnosticStudyObservation = patientSection.getObservations().stream()
                .filter(o -> {
                            for(II templateId: o.getTemplateIds()) {
                                if(templateId.getRoot() !=null && templateId.getRoot().equals("2.16.840.1.113883.10.20.24.3.18")) {
                                    return true;
                                }
                            }
                            return false;
                }).findFirst().orElseThrow(() -> new Exception("Diagnostic Study observation not found at the template"));
        modifyDiagnosticStudyObservation(diagnosticStudyObservation, observationData);

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

    private EncounterData extractEncounterData(JsonNode input) {
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

    private ObservationData extractObservationData(JsonNode input) {
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

    private void modifyEncounter(Encounter e, EncounterData encounterData) {
        CD code = e.getCode();
        code.setCode(encounterData.getCode());
        code.setDisplayName(encounterData.getDisplayName());

        IVL_TS effectiveTime = e.getEffectiveTime();
        effectiveTime.getLow().setValue(encounterData.getPeriodStartDate());
        effectiveTime.getHigh().setValue(encounterData.getPeriodEndDate());

        ED ed = DatatypesFactory.eINSTANCE.createED("Encounter, Performed: "+encounterData.getDisplayName());
        e.setText(ed);
    }

    private void modifyDiagnosticStudyObservation(Observation o, ObservationData observationData) {
        CD code = o.getCode();
        code.setCode(observationData.getCode());
        code.setDisplayName(observationData.getDisplayName());
        Participant2 participant = o.getParticipants().stream().findFirst().get();
        participant.getTime().getLow().setValue(observationData.getEffectiveDateTime());
        ParticipantRole participantRole = participant.getParticipantRole();
        participantRole.getCode().setCode(observationData.getParticipantRoleCode());
        participantRole.getCode().setDisplayName(observationData.getParticipantRoleDisplayName());

        ED ed = DatatypesFactory.eINSTANCE.createED("Diagnostic Study, Performed: "+observationData.getDisplayName());
        o.setText(ed);
    }

}

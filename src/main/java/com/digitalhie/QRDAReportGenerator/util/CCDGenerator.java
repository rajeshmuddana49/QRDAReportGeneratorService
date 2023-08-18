package com.digitalhie.QRDAReportGenerator.util;

import com.digitalhie.QRDAReportGenerator.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.openhealthtools.mdht.uml.cda.*;
import org.openhealthtools.mdht.uml.cda.consol.ConsolFactory;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.vocab.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.*;

@Service
public class CCDGenerator {

    Logger logger = LoggerFactory.getLogger(CCDGenerator.class);

    public String createQRDA1(JsonNode input) throws Exception {

        PatientData patientData = extractPatientData(input);
        EncounterData encounterData = extractEncounterData(input);
        ObservationData observationData = extractObservationData(input);

        ClinicalDocument oClinicalDocument = CDAFactory.eINSTANCE.createClinicalDocument();
        InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
        typeId.setExtension("POCD_HD000040");
        typeId.setRoot("2.16.840.1.113883.1.3");
        oClinicalDocument.setTypeId(typeId);
        ST docTitle = DatatypesFactory.eINSTANCE.createST("QRDA I Report");
        oClinicalDocument.setTitle(docTitle);
        CE code = DatatypesFactory.eINSTANCE.createCE();
        code.setCodeSystemName("LOINC");
        code.setDisplayName("Quality Measure Report");
        code.setCodeSystem("2.16.840.1.113883.6.1");
        code.setCode("55182-0");
        oClinicalDocument.setCode(code);
        oClinicalDocument.setLanguageCode(DatatypesFactory.eINSTANCE.createCS("en-US"));

        /*
        II id = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.19.4", "c266");
        oClinicalDocument.setId(id);
        TS effectiveTime = DatatypesFactory.eINSTANCE.createTS("20230402091000");
        oClinicalDocument.setEffectiveTime(effectiveTime);

        CE confidentialityCode = DatatypesFactory.eINSTANCE.createCE("N", "2.16.840.1.113883.5.25");
        confidentialityCode.setCodeSystem("HL7Confidentiality");
        oClinicalDocument.setConfidentialityCode(confidentialityCode);
        */

        RecordTarget recordTarget = CDAFactory.eINSTANCE.createRecordTarget();
        oClinicalDocument.getRecordTargets().add(recordTarget);

        PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
        patientRole.getIds().add(DatatypesFactory.eINSTANCE.createII(patientData.getPatientId()));

        //TODO Address not available to add
        /*
        AD ad = DatatypesFactory.eINSTANCE.createAD();
        ad.addStreetAddressLine(patientData.getAddress().getStreetAddressLine());
        ad.addCity(patientData.getAddress().getCity());
        ad.addState(patientData.getAddress().getState());
        ad.addCountry(patientData.getAddress().getCountry());
        ad.addPostalCode(patientData.getAddress().getPostalCode());
        patientRole.getAddrs().add(ad);
        */

        //TODO telephone number not available to add
        /*
        TEL telephone = DatatypesFactory.eINSTANCE.createTEL("");
        patientRole.getTelecoms().add(telephone);
        */

        recordTarget.setPatientRole(patientRole);

        org.openhealthtools.mdht.uml.cda.Patient patient = CDAFactory.eINSTANCE.createPatient();
        patientRole.setPatient(patient);

        PN name = DatatypesFactory.eINSTANCE.createPN();
        name.addGiven(patientData.getFirstName()).addFamily(patientData.getLastName());
        patient.getNames().add(name);

        CE administrativeGenderCode = DatatypesFactory.eINSTANCE.createCE(patientData.getGender(),"2.16.840.1.113883.5.1");
        patient.setAdministrativeGenderCode(administrativeGenderCode);

        TS birthTime = DatatypesFactory.eINSTANCE.createTS(patientData.getDob());
        patient.setBirthTime(birthTime);

        CE raceCode = DatatypesFactory.eINSTANCE.createCE(patientData.getRaceCode(),"2.16.840.1.113883.6.238");
        raceCode.setDisplayName(patientData.getRaceDisplayName());
        patient.setRaceCode(raceCode);

        CE ethnicGroupCode = DatatypesFactory.eINSTANCE.createCE(patientData.getEthnicityCode(),"2.16.840.1.113883.6.238");
        ethnicGroupCode.setDisplayName(patientData.getEthnicityDisplayName());
        patient.setEthnicGroupCode(ethnicGroupCode);

        Section patientSection = CDAFactory.eINSTANCE.createSection();
        ST title = DatatypesFactory.eINSTANCE.createST("Patient Data");
        patientSection.setTitle(title);
        patientSection.getEntries().add(createEntryWithEncounter(encounterData));
        patientSection.getEntries().add(createEntryWithObservation(observationData));

        oClinicalDocument.addSection(patientSection);
        // write to file
        String fileName = UUID.randomUUID()+"_qrda1_ccd_file.xml";
        FileOutputStream fos = new FileOutputStream(fileName);
        CDAUtil.save(oClinicalDocument, fos);
        fos.close();

        return fileName;
    }
    public String createQRDA3(JsonNode input) throws Exception {

        Map<String, String> referenceValues = new HashMap<>();
        referenceValues.put("initial-population","IPOP");
        referenceValues.put("numerator","NUMER");
        referenceValues.put("denominator","DENOM");
        referenceValues.put("denominator-exclusion","DENEX");

        Map<String, String> params = extractParams(input);
        Map<String, Integer> populationCounts =  extractPopulationCounts(input, referenceValues);

        ClinicalDocument oClinicalDocument = CDAFactory.eINSTANCE.createClinicalDocument();
        InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
        typeId.setExtension("POCD_HD000040");
        typeId.setRoot("2.16.840.1.113883.1.3");
        oClinicalDocument.setTypeId(typeId);
        ST docTitle = DatatypesFactory.eINSTANCE.createST("QRDA III Report");
        oClinicalDocument.setTitle(docTitle);
        CE code = DatatypesFactory.eINSTANCE.createCE();
        code.setCodeSystemName("LOINC");
        code.setDisplayName("Quality Reporting Document Architecture Calculated Summary Report");
        code.setCodeSystem("2.16.840.1.113883.6.1");
        code.setCode("55184-6");
        oClinicalDocument.setCode(code);
        oClinicalDocument.setLanguageCode(DatatypesFactory.eINSTANCE.createCS("en-US"));

        RecordTarget recordTarget = CDAFactory.eINSTANCE.createRecordTarget();
        oClinicalDocument.getRecordTargets().add(recordTarget);

        PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
        patientRole.setNullFlavor(NullFlavor.NA);
        recordTarget.setPatientRole(patientRole);

        Section measuresSection = CDAFactory.eINSTANCE.createSection();
        ST title = DatatypesFactory.eINSTANCE.createST("Measure Section");
        measuresSection.setTitle(title);
        measuresSection.getEntries().add(createEntryAct(params));
        measuresSection.getEntries().add(createEntryWithOrganizer(params, populationCounts));

        oClinicalDocument.addSection(measuresSection);

        // write to file
        String fileName = UUID.randomUUID()+"_qrda3_ccd_file.xml";
        FileOutputStream fos = new FileOutputStream(fileName);
        CDAUtil.save(oClinicalDocument, fos);
        fos.close();

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
                encounterData.setCodeSystemName(coding.get("system").asText());
                encounterData.setPeriodStartDate(formatDate(e.get("resource").get("period").get("start").asText()));
                encounterData.setPeriodEndDate(formatDate(e.get("resource").get("period").get("end").asText()));
                encounterData.setStatus(e.get("resource").get("status").asText());
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
                observationData.setCodeSystemName(coding.get("system").asText());
                observationData.setDisplayName(coding.get("display").asText());
                observationData.setEffectiveDateTime(formatDate(e.get("resource").get("effectiveDateTime").asText()));

                JsonNode participantRoleCoding = (e.get("resource").get("valueCodeableConcept").get("coding")).get(0);
                observationData.setParticipantRoleCode(participantRoleCoding.get("code").asText());
                observationData.setParticipantRoleDisplayName(participantRoleCoding.get("display").asText());
                observationData.setParticipantRoleCodeSystemName(participantRoleCoding.get("system").asText());
                observationData.setStatus(e.get("resource").get("status").asText());

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


    private Map<String, String> extractParams(JsonNode input) {
        Map<String, String> params = new HashMap<>();
        params.put("start", formatDate(input.get("period").get("start").asText()));
        params.put("end", formatDate(input.get("period").get("end").asText()));
        params.put("status", input.get("status").asText());
        return params;
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

    private Entry createEntryWithEncounter(EncounterData encounterData) {
        Entry entry = CDAFactory.eINSTANCE.createEntry();
        entry.setTypeCode(x_ActRelationshipEntry.DRIV);
        CD code = DatatypesFactory.eINSTANCE.createCD();
        code.setCodeSystemName(encounterData.getCodeSystemName());
        code.setDisplayName(encounterData.getDisplayName());
        code.setCodeSystem("2.16.840.1.113883.5.6");
        code.setCode(encounterData.getCode());
        Encounter e = CDAFactory.eINSTANCE.createEncounter();
        e.setCode(code);
        e.setClassCode(ActClass.ENC);
        e.setMoodCode(x_DocumentEncounterMood.RQO);
        ED ed = DatatypesFactory.eINSTANCE.createED("Encounter Performed: "+encounterData.getDisplayName());
        e.setText(ed);
        e.setStatusCode(DatatypesFactory.eINSTANCE.createCS(encounterData.getStatus()));
        IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS(encounterData.getPeriodStartDate(), encounterData.getPeriodEndDate());
        e.setEffectiveTime(effectiveTime);
        /*
        EntryRelationship er = CDAFactory.eINSTANCE.createEntryRelationship();
        Observation o = CDAFactory.eINSTANCE.createObservation();
        CD value =  DatatypesFactory.eINSTANCE.createCD();
        value.setCodeSystem(encounterData.getEncounterId());
        o.getValues().add(value);
        er.setObservation(o);
        e.getEntryRelationships().add(er);
        */
        entry.setEncounter(e);
        return entry;
    }

    private Entry createEntryWithObservation(ObservationData observationDetail) {
        Entry entry = CDAFactory.eINSTANCE.createEntry();
        entry.setTypeCode(x_ActRelationshipEntry.DRIV);
        CD code = DatatypesFactory.eINSTANCE.createCD();
        code.setCodeSystemName(observationDetail.getCodeSystemName());
        code.setCodeSystem("2.16.840.1.113883.6.1");
        code.setDisplayName(observationDetail.getDisplayName());
        code.setCode(observationDetail.getCode());
        Observation o = CDAFactory.eINSTANCE.createObservation();
        o.setCode(code);
        o.setClassCode(ActClassObservation.OBS);
        o.setMoodCode(x_ActMoodDocumentObservation.EVN);
        ED ed = DatatypesFactory.eINSTANCE.createED("Diagnostic Study, Performed: "+observationDetail.getDisplayName());
        o.setStatusCode(DatatypesFactory.eINSTANCE.createCS(observationDetail.getStatus()));
        o.setText(ed);
        Participant2 participant = CDAFactory.eINSTANCE.createParticipant2();
        IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS();
        IVXB_TS low = DatatypesFactory.eINSTANCE.createIVXB_TS();
        low.setValue(observationDetail.getEffectiveDateTime());
        effectiveTime.setLow(low);
        participant.setTime(effectiveTime);
        ParticipantRole participantRole = CDAFactory.eINSTANCE.createParticipantRole();
        CE participantRoleCode = DatatypesFactory.eINSTANCE.createCE(observationDetail.getParticipantRoleCode(), "2.16.840.1.113883.6.96");
        participantRoleCode.setDisplayName(observationDetail.getParticipantRoleDisplayName());
        participantRoleCode.setCodeSystemName(observationDetail.getParticipantRoleCodeSystemName());
        participantRole.setCode(participantRoleCode);
        participant.setParticipantRole(participantRole);
        o.getParticipants().add(participant);

        entry.setObservation(o);

        return entry;
    }

    private Entry createEntryWithOrganizer(Map<String, String> params, Map<String, Integer> populationCounts) {
        Entry entry = CDAFactory.eINSTANCE.createEntry();
        Organizer organizer = CDAFactory.eINSTANCE.createOrganizer();
        organizer.setClassCode(x_ActClassDocumentEntryOrganizer.CLUSTER);
        organizer.setMoodCode(ActMood.EVN);
        CS status = DatatypesFactory.eINSTANCE.createCS(params.get("status"));
        organizer.setStatusCode(status);
        populationCounts.forEach((metric, count) -> {
            Component4 component = CDAFactory.eINSTANCE.createComponent4();
            Observation o = CDAFactory.eINSTANCE.createObservation();
            CD code = DatatypesFactory.eINSTANCE.createCD();
            code.setCodeSystemName("ActCode");
            code.setCodeSystem("2.16.840.1.113883.5.4");
            code.setDisplayName("Assertion");
            code.setCode("ASSERTION");
            o.setCode(code);
            o.setClassCode(ActClassObservation.OBS);
            o.setMoodCode(x_ActMoodDocumentObservation.EVN);
            o.setStatusCode(DatatypesFactory.eINSTANCE.createCS("completed"));
            CD value = DatatypesFactory.eINSTANCE.createCD();
            value.setCodeSystemName("ActCode");
            value.setCodeSystem("2.16.840.1.113883.5.4");
            value.setCode(metric);
            o.getValues().add(value);
            EntryRelationship entryRelationship = CDAFactory.eINSTANCE.createEntryRelationship();
            entryRelationship.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
            entryRelationship.setInversionInd(true);

            Observation observation = CDAFactory.eINSTANCE.createObservation();
            CD code2 = DatatypesFactory.eINSTANCE.createCD();
            code2.setCodeSystemName("ActCode");
            code2.setCodeSystem("2.16.840.1.113883.5.4");
            code2.setDisplayName("rate aggregation");
            code2.setCode("MSRAGG");
            observation.setCode(code2);
            observation.setClassCode(ActClassObservation.OBS);
            observation.setMoodCode(x_ActMoodDocumentObservation.EVN);
            INT value2 = DatatypesFactory.eINSTANCE.createINT();
            value2.setValue(count);
            observation.getValues().add(value2);
            CE methodCode = DatatypesFactory.eINSTANCE.createCE("COUNT", "2.16.840.1.113883.5.84");
            methodCode.setCodeSystemName("ObservationMethod");
            methodCode.setDisplayName("Count");
            observation.getMethodCodes().add(methodCode);
            entryRelationship.setObservation(observation);

            o.getEntryRelationships().add(entryRelationship);
            component.setObservation(o);
            organizer.getComponents().add(component);
        });

        entry.setOrganizer(organizer);

        return entry;
    }

    private Entry createEntryAct(Map<String, String> params) {
        Entry entry = CDAFactory.eINSTANCE.createEntry();
        Act act = CDAFactory.eINSTANCE.createAct();
        act.setClassCode(x_ActClassDocumentEntryAct.ACT);
        act.setMoodCode(x_DocumentActMood.EVN);
        IVL_TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS(params.get("start"), params.get("end"));
        act.setEffectiveTime(effectiveTime);
        CD code = DatatypesFactory.eINSTANCE.createCD();
        code.setCodeSystem("2.16.840.1.113883.6.96");
        code.setDisplayName("Observation Parameters");
        code.setCode("252116004");
        act.setCode(code);
        entry.setAct(act);
        return entry;
    }
}

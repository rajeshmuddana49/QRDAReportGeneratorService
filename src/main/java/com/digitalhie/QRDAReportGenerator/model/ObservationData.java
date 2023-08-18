package com.digitalhie.QRDAReportGenerator.model;

import java.util.List;

public class ObservationData {

    private String observationId;

    private String code;

    private String displayName;

    private String effectiveDateTime;

    private String participantRoleCode;

    private String participantRoleDisplayName;

    private String participantRoleCodeSystemName;

    private String codeSystemName;

    private String status;

    private List<ComponentData> components;

    public String getObservationId() {
        return observationId;
    }

    public void setObservationId(String observationId) {
        this.observationId = observationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public String getParticipantRoleCode() {
        return participantRoleCode;
    }

    public void setParticipantRoleCode(String participantRoleCode) {
        this.participantRoleCode = participantRoleCode;
    }

    public String getParticipantRoleDisplayName() {
        return participantRoleDisplayName;
    }

    public void setParticipantRoleDisplayName(String participantRoleDisplayName) {
        this.participantRoleDisplayName = participantRoleDisplayName;
    }

    public List<ComponentData> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentData> components) {
        this.components = components;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParticipantRoleCodeSystemName() {
        return participantRoleCodeSystemName;
    }

    public void setParticipantRoleCodeSystemName(String participantRoleCodeSystemName) {
        this.participantRoleCodeSystemName = participantRoleCodeSystemName;
    }
}

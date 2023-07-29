package com.digitalhie.QRDAReportGenerator.model;

public class EncounterData {

    private String encounterId;

    private String code;

    private String displayName;

    private String periodStartDate;

    private String periodEndDate;

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
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

    public String getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(String periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public String getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(String periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    @Override
    public String toString() {
        return "Encounter{" +
                "encounterId='" + encounterId + '\'' +
                ", code='" + code + '\'' +
                ", displayName='" + displayName + '\'' +
                ", periodStartDate='" + periodStartDate + '\'' +
                ", periodEndDate='" + periodEndDate + '\'' +
                '}';
    }
}

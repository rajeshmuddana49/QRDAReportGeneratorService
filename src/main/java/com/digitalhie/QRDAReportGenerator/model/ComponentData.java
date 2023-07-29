package com.digitalhie.QRDAReportGenerator.model;

public class ComponentData {

    private String code;

    private String displayName;

    private String value;

    private String unit;

    private String valueCode;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    @Override
    public String toString() {
        return "ComponentData{" +
                "code='" + code + '\'' +
                ", displayName='" + displayName + '\'' +
                ", value='" + value + '\'' +
                ", unit='" + unit + '\'' +
                ", valueCode='" + valueCode + '\'' +
                '}';
    }
}

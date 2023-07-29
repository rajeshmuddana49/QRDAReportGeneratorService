package com.digitalhie.QRDAReportGenerator.model;

public class PatientData {
    private String patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private String dob;

    private String raceCode;

    private String raceDisplayName;

    private String ethnicityCode;

    private String ethnicityDisplayName;

    private Address address;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getRaceCode() {
        return raceCode;
    }

    public void setRaceCode(String raceCode) {
        this.raceCode = raceCode;
    }

    public String getRaceDisplayName() {
        return raceDisplayName;
    }

    public void setRaceDisplayName(String raceDisplayName) {
        this.raceDisplayName = raceDisplayName;
    }

    public String getEthnicityCode() {
        return ethnicityCode;
    }

    public void setEthnicityCode(String ethnicityCode) {
        this.ethnicityCode = ethnicityCode;
    }

    public String getEthnicityDisplayName() {
        return ethnicityDisplayName;
    }

    public void setEthnicityDisplayName(String ethnicityDisplayName) {
        this.ethnicityDisplayName = ethnicityDisplayName;
    }

    @Override
    public String toString() {
        return "PatientData{" +
                "patientId='" + patientId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", dob='" + dob + '\'' +
                ", raceCode='" + raceCode + '\'' +
                ", raceDisplayName='" + raceDisplayName + '\'' +
                ", ethnicityCode='" + ethnicityCode + '\'' +
                ", ethnicityDisplayName='" + ethnicityDisplayName + '\'' +
                ", address=" + address +
                '}';
    }
}

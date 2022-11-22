package org.smartregister.reveal.model;


public class SurveyCardDetails extends CardDetails {
    private String dateCreated;
    private String owner;
    private String structureNumber;

    public SurveyCardDetails(String status, String dateCreated, String owner,String structureNumber) {
        super(status);
        this.dateCreated = dateCreated;
        this.owner = owner;
        this.structureNumber = structureNumber;
    }

    public String getStructureNumber() {
        return structureNumber;
    }

    public void setStructureNumber(final String structureNumber) {
        this.structureNumber = structureNumber;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

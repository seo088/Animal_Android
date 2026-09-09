package com.animalloo.data.model;

public class PetFriendlyFilter {

    private final String petType;
    private final String sizeLimit;
    private final Boolean indoorAllowed;
    private final Boolean carrierRequired;
    private final Boolean leashRequired;

    public PetFriendlyFilter(String petType, String sizeLimit,
                             Boolean indoorAllowed, Boolean carrierRequired,
                             Boolean leashRequired) {
        this.petType = petType;
        this.sizeLimit = sizeLimit;
        this.indoorAllowed = indoorAllowed;
        this.carrierRequired = carrierRequired;
        this.leashRequired = leashRequired;
    }

    public String getPetType() {
        return petType;
    }

    public String getSizeLimit() {
        return sizeLimit;
    }

    public Boolean getIndoorAllowed() {
        return indoorAllowed;
    }

    public Boolean getCarrierRequired() {
        return carrierRequired;
    }

    public Boolean getLeashRequired() {
        return leashRequired;
    }

    public static PetFriendlyFilter empty() {
        return new PetFriendlyFilter("전체", "전체", null, null, null);
    }
}

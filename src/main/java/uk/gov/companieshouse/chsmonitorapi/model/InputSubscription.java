package uk.gov.companieshouse.chsmonitorapi.model;

import javax.validation.constraints.NotNull;

/**
 * InputSubscription represents the JSON input.
 */
public class InputSubscription {

    public InputSubscription() {
    }

    public InputSubscription(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @NotNull
    private String companyNumber;

    /**
     * Gets company number.
     *
     * @return the company number
     */
    public String getCompanyNumber() {
        return companyNumber;
    }

    /**
     * Sets company number.
     *
     * @param companyNumber the company number
     */
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}


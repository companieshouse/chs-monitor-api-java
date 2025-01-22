package uk.gov.companieshouse.chsmonitorapi.model;

import javax.validation.constraints.NotNull;

/**
 * InputSubscription represents the JSON input.
 */
public class InputSubscription {

    @NotNull
    private String companyNumber;

    public InputSubscription() {
    }

    public InputSubscription(String companyNumber) {
        this.companyNumber = companyNumber;
    }

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


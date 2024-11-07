package uk.gov.companieshouse.chsmonitorapi.model;

/**
 * InputSubscription represents the JSON input.
 */
public class InputSubscription {

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


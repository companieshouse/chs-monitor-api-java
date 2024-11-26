package uk.gov.companieshouse.chsmonitorapi.exception;

public class SubscriptionNotFound extends RuntimeException {

    public SubscriptionNotFound(String companyName, String userId) {
        super("Could not find subscription for company %s and user %s".formatted(companyName,
                userId));
    }
}

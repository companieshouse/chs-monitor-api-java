package uk.gov.companieshouse.chsmonitorapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.Subscription;
import uk.gov.companieshouse.chsmonitorapi.service.CompanyProfileService;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

public class SubscriptionServiceImpl implements SubscriptionService {

    private final CompanyProfileService companyProfileService;
    private final Logger logger;

    @Autowired
    public SubscriptionServiceImpl(Logger logger, CompanyProfileService companyProfileService) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
    }

    @Override
    public List<Subscription> getSubscriptions(String companyNumber, int startIndex,
            int itemsPerPage) throws ArrayIndexOutOfBoundsException {

        // TODO: actually get these once mongo interface exists
        List<Subscription> subscriptions = new ArrayList<>();
        if (subscriptions.isEmpty()) {
            return subscriptions;
        }

        subscriptions.forEach(subscription -> {
            //TODO: why? to account for name updates since subscription entry was added?
            try {
                subscription.setCompanyName(
                        companyProfileService.getCompanyDetails(subscription.getCompanyNumber())
                                .getCompanyName());
            } catch (ServiceException ex) {
                // TODO: figure this out
                throw new RuntimeException(ex);
            }
        });

        if (itemsPerPage <= 0) {
            itemsPerPage = subscriptions.size();
        }

        if (startIndex > subscriptions.size() - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int lastItem = startIndex + itemsPerPage;
        if (lastItem > subscriptions.size()) {
            lastItem = subscriptions.size();
        }

        return subscriptions.subList(startIndex, lastItem);
    }

    @Override
    public Subscription getSubscription(String companyNumber) throws ServiceException {
        // TODO: actually get this
        Subscription subscription = new Subscription();
        if (subscription == null) {
            throw new RuntimeException();
        }

        // TODO: why? to account for name updates since subscription entry was added?
        try {
            subscription.setCompanyName(
                    companyProfileService.getCompanyDetails(subscription.getCompanyName())
                            .getCompanyName());
            return subscription;
        } catch (ServiceException ex) {
            // TODO: figure this out
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void createSubscription(String companyNumber) throws ServiceException {

    }

    @Override
    public void deleteSubscription(String companyNumber) throws ServiceException {

    }
}

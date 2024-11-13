package uk.gov.companieshouse.chsmonitorapi.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.repository.MonitorMongoRepository;
import uk.gov.companieshouse.chsmonitorapi.service.CompanyProfileService;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final CompanyProfileService companyProfileService;
    private final Logger logger;
    private final MonitorMongoRepository mongoRepository;

    @Autowired
    public SubscriptionServiceImpl(Logger logger, CompanyProfileService companyProfileService,
            MonitorMongoRepository mongoRepository) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Page<SubscriptionDocument> getSubscriptions(String userId, String companyNumber,
            int startIndex, int itemsPerPage) throws ArrayIndexOutOfBoundsException {

        PageRequest pageRequest = PageRequest.of(startIndex / itemsPerPage, itemsPerPage);

        Page<SubscriptionDocument> pagedSubscriptions =
                mongoRepository.findSubscriptionsByUserIdAndCompanyNumber(
                userId, companyNumber, pageRequest);

        if (pagedSubscriptions.get().findFirst().isEmpty()) {
            return pagedSubscriptions;
        }

        pagedSubscriptions.forEach(subscriptionDocument -> {
            try {
                subscriptionDocument.setCompanyName(companyProfileService.getCompanyDetails(
                        subscriptionDocument.getCompanyNumber()).getCompanyName());
            } catch (ServiceException | ApiErrorResponseException | URIValidationException ex) {
                throw new RuntimeException(ex);
            }

            if (startIndex > pagedSubscriptions.getSize() - 1) {
                throw new ArrayIndexOutOfBoundsException();
            }
        });

        return pagedSubscriptions;
    }

    @Override
    public SubscriptionDocument getSubscription(String userId, String companyNumber)
            throws ServiceException {
        Optional<SubscriptionDocument> optionalSubscription =
                mongoRepository.findSubscriptionByUserIdAndCompanyNumber(
                userId, companyNumber);

        if (optionalSubscription.isEmpty()) {
            // TODO: confirm this works the same on the FE as returning nil in the golang service
            return new SubscriptionDocument();
        }

        SubscriptionDocument subscription = optionalSubscription.get();

        try {
            subscription.setCompanyName(
                    companyProfileService.getCompanyDetails(subscription.getCompanyNumber())
                            .getCompanyName());
            return subscription;
        } catch (ServiceException ex) {
            // TODO: figure this out
            throw new RuntimeException(ex);
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSubscription(String userId, String companyNumber) throws ServiceException {

    }

    @Override
    public void deleteSubscription(String userId, String companyNumber) throws ServiceException {

    }
}

package uk.gov.companieshouse.chsmonitorapi.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.exception.SubscriptionNotFound;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.repository.MonitorMongoRepository;
import uk.gov.companieshouse.chsmonitorapi.service.CompanyProfileService;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

/**
 * The type Subscription service.
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final CompanyProfileService companyProfileService;
    private final Logger logger;
    private final MonitorMongoRepository mongoRepository;

    /**
     * Instantiates a new Subscription service.
     *
     * @param logger                the logger
     * @param companyProfileService the company profile service
     * @param mongoRepository       the mongo repository
     */
    @Autowired
    public SubscriptionServiceImpl(Logger logger, CompanyProfileService companyProfileService,
            MonitorMongoRepository mongoRepository) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Page<SubscriptionDocument> getSubscriptions(String userId, Pageable pageable) {
        Page<SubscriptionDocument> pagedSubscriptions =
                mongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(
                userId, pageable);
        if (pagedSubscriptions.get().findFirst().isEmpty()) {
            return pagedSubscriptions;
        }

        pagedSubscriptions.forEach(subscriptionDocument -> {
            subscriptionDocument.setCompanyName(
                    companyProfileService.getCompanyDetails(subscriptionDocument.getCompanyNumber())
                            .getCompanyName());
        });

        return pagedSubscriptions;
    }

    @Override
    public SubscriptionDocument getSubscription(String userId, String companyNumber)
            throws ServiceException, SubscriptionNotFound {
        Optional<SubscriptionDocument> optionalSubscription =
                mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(
                userId, companyNumber);

        if (optionalSubscription.isEmpty()) {
            throw new SubscriptionNotFound(userId, companyNumber);
        }

        SubscriptionDocument subscription = optionalSubscription.get();

        subscription.setCompanyName(
                companyProfileService.getCompanyDetails(subscription.getCompanyNumber())
                        .getCompanyName());
        return subscription;
    }

    @Override
    public void createSubscription(String userId, String companyNumber) throws ServiceException {
        if (mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(userId,
                companyNumber).isPresent()) {
            String exceptionMessage = String.format(
                    "Active subscription already exists for userId: %s companyNumber: %s", userId,
                    companyNumber);
            ServiceException exception = new ServiceException(exceptionMessage);
            logger.error(exceptionMessage, exception);
            throw exception;
        }
        if (mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsFalse(userId,
                companyNumber).isPresent()) {
            logger.debug(String.format(
                    "Inactive subscription exists for userId: %s companyNumber: %s, toggling it "
                            + "to active", userId, companyNumber));
            mongoRepository.findAndSetActiveByUserIdAndCompanyNumber(userId, companyNumber, true);
            return;
        }
        logger.debug(
                String.format("Creating new subscription for userId: %s companyNumber: %s", userId,
                        companyNumber));
        SubscriptionDocument subscriptionDocument = new SubscriptionDocument(userId, companyNumber,
                null, null, true, LocalDateTime.now(), LocalDateTime.now());
        mongoRepository.save(subscriptionDocument);
    }

    @Override
    public void deleteSubscription(String userId, String companyNumber) throws ServiceException {
        Optional<SubscriptionDocument> subscription =
                mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(
                userId, companyNumber);

        if (subscription.isEmpty()) {
            String exceptionMessage = String.format(
                    "No active subscription exists for userId: %s companyNumber: %s", userId,
                    companyNumber);
            SubscriptionNotFound exception = new SubscriptionNotFound(companyNumber, userId);
            logger.error(exceptionMessage, exception);
            throw exception;
        }

        logger.debug(String.format(
                "Active subscription exists for userId: %s companyNumber: %s, toggling it "
                        + "to inactive", userId, companyNumber));
        mongoRepository.findAndSetActiveByUserIdAndCompanyNumber(userId, companyNumber, false);
    }
}

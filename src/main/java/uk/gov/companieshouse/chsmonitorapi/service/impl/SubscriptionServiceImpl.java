package uk.gov.companieshouse.chsmonitorapi.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    public Page<SubscriptionDocument> getSubscriptions(String userId, int startIndex,
            int itemsPerPage) throws ServiceException {
        PageRequest pageRequest = PageRequest.of(startIndex / itemsPerPage, itemsPerPage);
        return getSubscriptions(userId, pageRequest);
    }

    @Override
    public Page<SubscriptionDocument> getSubscriptions(String userId, Pageable pageable) {
        Page<SubscriptionDocument> pagedSubscriptions = mongoRepository.findSubscriptionsByUserId(
                userId, pageable);
        if (pagedSubscriptions.get().findFirst().isEmpty()) {
            return pagedSubscriptions;
        }

        pagedSubscriptions.forEach(subscriptionDocument -> {
            subscriptionDocument.setCompanyName(
                    companyProfileService.getCompanyDetails(subscriptionDocument.getCompanyNumber())
                            .getCompanyName());

            if (pageable.getOffset() > pagedSubscriptions.getSize() - 1) {
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

        subscription.setCompanyName(
                companyProfileService.getCompanyDetails(subscription.getCompanyNumber())
                        .getCompanyName());
        return subscription;
    }

    @Override
    public void createSubscription(String userId, String companyNumber) throws ServiceException {
        // TODO: figure out query value, handle save issues (duplication?)
        SubscriptionDocument subscriptionDocument = new SubscriptionDocument(userId, companyNumber,
                null, null, true, LocalDateTime.now(), LocalDateTime.now());
        mongoRepository.save(subscriptionDocument);
    }

    @Override
    public void deleteSubscription(String userId, String companyNumber) throws ServiceException {
        // TODO: does this work?
        mongoRepository.deleteAllByUserIdAndCompanyNumber(userId, companyNumber);
    }
}

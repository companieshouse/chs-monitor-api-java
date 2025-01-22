package uk.gov.companieshouse.chsmonitorapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

/**
 * The interface Subscription service.
 */
public interface SubscriptionService {

    /**
     * Gets subscriptions.
     *
     * @param userId   the user id
     * @param pageable the pageable
     * @return the subscriptions
     */
    Page<SubscriptionDocument> getSubscriptions(String userId, Pageable pageable);

    /**
     * Gets subscription.
     *
     * @param userId        the user id
     * @param companyNumber the company number
     * @return the subscription
     * @throws ServiceException the service exception
     */
    SubscriptionDocument getSubscription(String userId, String companyNumber)
            throws ServiceException;

    /**
     * Create subscription.
     *
     * @param userId        the user id
     * @param companyNumber the company number
     * @throws ServiceException the service exception
     */
    void createSubscription(String userId, String companyNumber) throws ServiceException;

    /**
     * Delete subscription.
     *
     * @param userId        the user id
     * @param companyNumber the company number
     * @throws ServiceException the service exception
     */
    void deleteSubscription(String userId, String companyNumber) throws ServiceException;

}

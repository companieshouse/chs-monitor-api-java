package uk.gov.companieshouse.chsmonitorapi.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.Subscription;

public interface SubscriptionService {

    List<Subscription> getSubscriptions(String userId, String companyNumber, int startIndex, int itemsPerPage)
            throws ServiceException;

    Subscription getSubscription(String userId, String companyNumber) throws ServiceException;

    void createSubscription(String userId, String companyNumber) throws ServiceException;

    void deleteSubscription(String userId, String companyNumber) throws ServiceException;

}

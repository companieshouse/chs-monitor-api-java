package uk.gov.companieshouse.chsmonitorapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.exception.SubscriptionNotFound;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.repository.MonitorMongoRepository;
import uk.gov.companieshouse.chsmonitorapi.service.impl.CompanyProfileServiceImpl;
import uk.gov.companieshouse.chsmonitorapi.service.impl.SubscriptionServiceImpl;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SubscriptionServiceTest {

    private static final String USER_ID = "userId";
    private static final String COMPANY_NUMBER = "companyNumber";
    private static final String COMPANY_NAME = "companyName";
    private static final String QUERY = "query";
    private static final Boolean ACTIVE = true;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @MockBean(name = "filterChain")
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private MonitorMongoRepository mongoRepository;

    @MockBean
    private CompanyProfileServiceImpl companyProfileService;

    @MockBean
    private EnvironmentReader environmentReader;

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @Autowired
    private Logger logger;

    private CompanyProfileApi companyProfileApi;
    private SubscriptionDocument subscriptionDocument;

    @BeforeEach
    void setup() {
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyName(COMPANY_NAME);
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);

        subscriptionDocument = new SubscriptionDocument(USER_ID, COMPANY_NUMBER, COMPANY_NAME,
                QUERY, ACTIVE, NOW, NOW.minus(Period.ofDays(1)));
    }

    @Test
    void shouldReturnSinglePageOfSubscriptionDocuments() throws ServiceException {

        SubscriptionDocument subDoc = new SubscriptionDocument(USER_ID, COMPANY_NUMBER,
                COMPANY_NAME, QUERY, ACTIVE, NOW, NOW.minus(Period.ofDays(1)));

        Page<SubscriptionDocument> subscriptionDocumentPage = new PageImpl<>(List.of(subDoc));
        when(mongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(anyString(),
                any(Pageable.class))).thenReturn(subscriptionDocumentPage);
        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(companyProfileApi);

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions(USER_ID,
                PageRequest.of(0, 5));

        assertEquals(1, documentPage.getTotalPages());
        assertTrue(documentPage.stream().allMatch(this::correctType));
        logger.info(documentPage.toString());
    }

    @Test
    void shouldReturn2PagesOfSubscriptionDocuments() throws ServiceException {
        List<SubscriptionDocument> subscriptionDocumentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            subscriptionDocumentList.add(subscriptionDocument);
        }

        Pageable pageable = Pageable.unpaged();

        Page<SubscriptionDocument> subscriptionDocumentPage = new PageImpl<>(
                subscriptionDocumentList.subList(0, 5), pageable, subscriptionDocumentList.size());

        when(mongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(anyString(),
                any(Pageable.class))).thenReturn(subscriptionDocumentPage);

        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(companyProfileApi);

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions(USER_ID,
                PageRequest.of(0, 5));

        assertEquals(2, documentPage.getTotalPages());
        logger.info(documentPage.toString());
    }

    @Test
    void shouldReturnAPageWithAnEmptyOptional() throws ServiceException {

        when(mongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(anyString(),
                any(Pageable.class))).thenReturn(Page.empty());

        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(companyProfileApi);

        subscriptionService.getSubscriptions(USER_ID, PageRequest.of(0, 5));

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions(USER_ID,
                PageRequest.of(0, 5));

        assertEquals(1, documentPage.getTotalPages());
        assertTrue(documentPage.get().findFirst().isEmpty());
    }

    @Test
    void shouldReturnASubscription() throws ServiceException {
        when(mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(anyString(),
                anyString())).thenReturn(Optional.of(subscriptionDocument));
        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(companyProfileApi);

        SubscriptionDocument subDoc = subscriptionService.getSubscription(USER_ID, COMPANY_NUMBER);

        assertTrue(subDoc.isActive());
        assertEquals(COMPANY_NUMBER, subDoc.getCompanyNumber());
    }

    @Test
    void shouldReturnAnEmptySubscription() throws ServiceException {
        when(mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(anyString(),
                anyString())).thenReturn(Optional.empty());

        assertThrows(SubscriptionNotFound.class,
                () -> subscriptionService.getSubscription(USER_ID, COMPANY_NUMBER));
    }

    @Test
    void shouldCreateASubscription() throws ServiceException {
        when(mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(anyString(),
                anyString())).thenReturn(Optional.empty());
        when(mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsFalse(anyString(),
                anyString())).thenReturn(Optional.empty());
        subscriptionService.createSubscription(USER_ID, COMPANY_NUMBER);
        verify(mongoRepository, times(1)).save(any());
    }

    @Test
    void shouldDeleteASubscription() throws ServiceException {
        when(mongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(eq(USER_ID),
                eq(COMPANY_NUMBER))).thenReturn(Optional.of(subscriptionDocument));
        subscriptionService.deleteSubscription(USER_ID, COMPANY_NUMBER);
        verify(mongoRepository, times(1)).findAndSetActiveByUserIdAndCompanyNumber(anyString(),
                anyString(), eq(false));
    }

    private boolean correctType(Object obj) {
        return obj instanceof SubscriptionDocument;
    }
}

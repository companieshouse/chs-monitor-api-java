package uk.gov.companieshouse.chsmonitorapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.repository.MonitorMongoRepository;
import uk.gov.companieshouse.chsmonitorapi.service.impl.CompanyProfileServiceImpl;
import uk.gov.companieshouse.chsmonitorapi.service.impl.SubscriptionServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SubscriptionServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private MonitorMongoRepository mongoRepository;

    @MockBean
    private CompanyProfileServiceImpl companyProfileService;

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @Autowired
    private Logger logger;

    @Test
    void shouldReturnSinglePageOfSubscriptionDocuments() throws ServiceException {

        SubscriptionDocument subscriptionDocument = new SubscriptionDocument("userId",
                "companyNumber", "companyName", "query", true, NOW, NOW.minus(Period.ofDays(1)));

        Page<SubscriptionDocument> subscriptionDocumentPage = new PageImpl<>(
                List.of(subscriptionDocument));
        when(mongoRepository.findSubscriptionsByUserId(anyString(),
                any(Pageable.class))).thenReturn(subscriptionDocumentPage);
        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(
                new CompanyDetails("companyStatus", "companyName", "companyNumber"));

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions("userId", 0,
                1);

        assertEquals(1, documentPage.getTotalPages());
        assertTrue(documentPage.stream().allMatch(this::correctType));
        logger.info(documentPage.toString());
    }

    @Test
    void shouldReturn2PagesOfSubscriptionDocuments() throws ServiceException {
        List<SubscriptionDocument> subscriptionDocumentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            subscriptionDocumentList.add(
                    new SubscriptionDocument("userId", "companyNumber", "companyName", "query",
                            true, NOW, NOW.minus(Period.ofDays(1))));
        }

        Pageable pageable = Pageable.unpaged();

        Page<SubscriptionDocument> subscriptionDocumentPage = new PageImpl<>(
                subscriptionDocumentList.subList(0, 5), pageable, subscriptionDocumentList.size());

        when(mongoRepository.findSubscriptionsByUserId(anyString(),
                any(Pageable.class))).thenReturn(subscriptionDocumentPage);

        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(
                new CompanyDetails("companyStatus", "companyName", "companyNumber"));

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions("userId", 0,
                5);

        assertEquals(2, documentPage.getTotalPages());
        logger.info(documentPage.toString());
    }

    @Test
    void shouldThrowAnOutOfBoundsException() throws ServiceException {
        List<SubscriptionDocument> subscriptionDocumentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            subscriptionDocumentList.add(
                    new SubscriptionDocument("userId", "companyNumber", "companyName", "query",
                            true, NOW, NOW.minus(Period.ofDays(1))));
        }

        Pageable pageable = Pageable.unpaged();

        Page<SubscriptionDocument> subscriptionDocumentPage = new PageImpl<>(
                subscriptionDocumentList.subList(0, 5), pageable, subscriptionDocumentList.size());

        when(mongoRepository.findSubscriptionsByUserId(anyString(),
                any(Pageable.class))).thenReturn(subscriptionDocumentPage);

        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(
                new CompanyDetails("companyStatus", "companyName", "companyNumber"));

        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> subscriptionService.getSubscriptions("userId", 21, 5));
    }

    @Test
    void shouldReturnAPageWithAnEmptyOptional() throws ServiceException {

        when(mongoRepository.findSubscriptionsByUserId(anyString(),
                any(Pageable.class))).thenReturn(Page.empty());

        when(companyProfileService.getCompanyDetails(anyString())).thenReturn(
                new CompanyDetails("companyStatus", "companyName", "companyNumber"));

        subscriptionService.getSubscriptions("userId", 0, 5);

        Page<SubscriptionDocument> documentPage = subscriptionService.getSubscriptions("userId", 0,
                5);

        assertEquals(1, documentPage.getTotalPages());
        assertTrue(documentPage.get().findFirst().isEmpty());
    }

    private boolean correctType(Object obj) {
        return obj instanceof SubscriptionDocument;
    }
}

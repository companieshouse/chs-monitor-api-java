package uk.gov.companieshouse.chsmonitorapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.chsmonitorapi.config.LoggingConfig;
import uk.gov.companieshouse.chsmonitorapi.config.WebMvcConfig;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.exception.SubscriptionNotFound;
import uk.gov.companieshouse.chsmonitorapi.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.chsmonitorapi.model.InputSubscription;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.security.SecurityConfig;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

@WebMvcTest(ChsMonitorApiController.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@EnableSpringDataWebSupport
@Import({LoggingConfig.class, WebMvcConfig.class, AuthenticationInterceptor.class,
        SecurityConfig.class})
class ChsMonitorApiControllerTest {

    private final String testCompanyNumber = "1777777";
    private final String testId = "TEST_ID";
    private final String companyName = "12345678";
    private final String companyNumber = "TEST_COMPANY";
    private final String userId = "TEST_USER";
    private final String ericIdentity = "ERIC-Identity";
    private final String ericIdentityType = "ERIC-Identity-Type";
    private final String ericIdentityTypeValue = "key";
    private final LocalDateTime now = LocalDateTime.of(2025, 1, 22, 16, 28, 30)
            .truncatedTo(ChronoUnit.SECONDS);
    private final SubscriptionDocument activeSubscription = new SubscriptionDocument();
    private final LocalDateTime created = now.minus(Period.ofDays(1))
            .truncatedTo(ChronoUnit.SECONDS);
    private final boolean active = true;
    private SubscriptionDocument inactiveSubscription = new SubscriptionDocument();
    private Page<SubscriptionDocument> subscriptions;
    private String expectedResponsePaged;
    private String expectedResponse;

    @Autowired
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;


    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @BeforeEach
    void beforeEach() {
        activeSubscription.setId(testId);
        activeSubscription.setActive(active);
        activeSubscription.setCreated(now);
        activeSubscription.setCompanyName(companyName);
        activeSubscription.setCompanyNumber(companyNumber);
        activeSubscription.setUserId(userId);
        activeSubscription.setCreated(created);
        subscriptions = new PageImpl<>(
                List.of(activeSubscription, activeSubscription, activeSubscription));

        inactiveSubscription = new SubscriptionDocument(activeSubscription);
        inactiveSubscription.setActive(false);

        expectedResponse = """
                {"id":"%s",
                "userId":"%s",
                "companyNumber":"%s",
                "companyName":"%s",
                "query":null,
                "active":%s,
                "created":"%s",
                "updated":null}
                """.formatted(testId, userId, companyNumber, companyName, active, created);

        expectedResponsePaged = """
                {"_embedded":{"subscriptionDocumentList":
                [
                %s,
                %s,
                %s
                ]},
                "_links":{"self":{"href":"http://localhost/following?companyNumber=1777777&number=0&size=10"}},
                "page":{"size":3,"totalElements":3,"totalPages":1,"number":0}}
                """.formatted(expectedResponse, expectedResponse, expectedResponse);
    }

    @Test
    @WithAnonymousUser
    void shouldBlockUnauthorizedCalls() throws Exception {
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/" + testCompanyNumber)).andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/" + testCompanyNumber)).andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/" + testCompanyNumber)).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnListOfSubscriptions() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), any(Pageable.class))).thenReturn(
                subscriptions);
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("companyNumber", testCompanyNumber).queryParam("number", 0)
                .queryParam("size", 10).encode().toUriString();
        mockMvc.perform(get(template).header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue)).andDo(print())
                .andExpectAll(status().isOk(), content().json(expectedResponsePaged));
    }

    @Test
    void shouldReturnStatus416() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), any(Pageable.class))).thenThrow(
                new ArrayIndexOutOfBoundsException());

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("size", Integer.MAX_VALUE - 10).queryParam("number", 10).encode()
                .toUriString();
        mockMvc.perform(get(template).header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue)).andDo(print())
                .andExpect(status().isRequestedRangeNotSatisfiable());
    }

    @Test
    void shouldReturnStatus404() throws Exception {
        when(subscriptionService.getSubscription(anyString(), anyString())).thenThrow(
                new SubscriptionNotFound("companyName", "userId"));

        String template = UriComponentsBuilder.fromHttpUrl(
                "http://localhost/following/companyNumber").toUriString();
        mockMvc.perform(get(template).header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue)).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnSubscription() throws Exception {
        when(subscriptionService.getSubscription(anyString(), anyString())).thenReturn(
                activeSubscription);

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();
        mockMvc.perform(get(template).header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue)).andDo(print())
                .andExpect(status().isOk()).andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(
                        delete("http://localhost/following").with(csrf()).header(ericIdentity,
                                        ericIdentity)
                                .header(ericIdentityType, ericIdentityTypeValue)
                                .content(objectMapper.writeValueAsString(deletePayload))
                                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk());

        when(subscriptionService.getSubscription(anyString(), anyString())).thenThrow(
                new ServiceException("Not found"));

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();

        mockMvc.perform(get(template).header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue)).andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldFailToDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        doThrow(new ServiceException("Service exception")).when(subscriptionService)
                .deleteSubscription(anyString(), anyString());

        mockMvc.perform(
                        delete("http://localhost/following").with(csrf()).header(ericIdentity,
                                        ericIdentity)
                                .header(ericIdentityType, ericIdentityTypeValue)
                                .content(objectMapper.writeValueAsString(deletePayload))
                                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isInternalServerError());

    }

    @Test
    void shouldCreateSubscription() throws Exception {
        InputSubscription createPayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("http://localhost/following").header(ericIdentity, ericIdentity)
                        .header(ericIdentityType, ericIdentityTypeValue).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk());
    }
}

package uk.gov.companieshouse.chsmonitorapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.chsmonitorapi.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.chsmonitorapi.model.InputSubscription;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.security.SecurityConfig;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@WebMvcTest(ChsMonitorApiController.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@EnableSpringDataWebSupport
@Import({LoggingConfig.class, WebMvcConfig.class, AuthenticationInterceptor.class,
        SecurityConfig.class})
class ChsMonitorApiControllerTest {

    private final String TEST_COMPANY_NUMBER = "1777777";
    private final String TEST_ID = "TEST_ID";
    private final String COMPANY_NAME = "12345678";
    private final String COMPANY_NUMBER = "TEST_COMPANY";
    private final String USER_ID = "TEST_USER";
    private final String ERIC_PASSTHROUGH = "ERIC_PASSTHROUGH_TOKEN";
    private final String ERIC_IDENTITY = "ERIC-Identity";
    private final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private final String ERIC_IDENTITY_TYPE_VALUE = "key";
    // If you run this at exactly HH:MM:00 it cuts off the seconds and fails lol
    private final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final SubscriptionDocument ACTIVE_SUBSCRIPTION = new SubscriptionDocument();
    private final LocalDateTime CREATED = NOW.minus(Period.ofDays(1))
            .truncatedTo(ChronoUnit.SECONDS);
    private final boolean ACTIVE = true;
    private SubscriptionDocument INACTIVE_SUBSCRIPTION = new SubscriptionDocument();
    private Page<SubscriptionDocument> SUBSCRIPTIONS;
    private String EXPECTED_RESPONSE_PAGED;
    private String EXPECTED_RESPONSE;

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

    @Test
    @WithAnonymousUser
    void shouldBlockUnauthorizedCalls() throws Exception {
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @BeforeEach
    void beforeEach() {
        ACTIVE_SUBSCRIPTION.setId(TEST_ID);
        ACTIVE_SUBSCRIPTION.setActive(ACTIVE);
        ACTIVE_SUBSCRIPTION.setCreated(NOW);
        ACTIVE_SUBSCRIPTION.setCompanyName(COMPANY_NAME);
        ACTIVE_SUBSCRIPTION.setCompanyNumber(COMPANY_NUMBER);
        ACTIVE_SUBSCRIPTION.setUserId(USER_ID);
        ACTIVE_SUBSCRIPTION.setCreated(CREATED);
        SUBSCRIPTIONS = new PageImpl<>(
                List.of(ACTIVE_SUBSCRIPTION, ACTIVE_SUBSCRIPTION, ACTIVE_SUBSCRIPTION));

        INACTIVE_SUBSCRIPTION = new SubscriptionDocument(ACTIVE_SUBSCRIPTION);
        INACTIVE_SUBSCRIPTION.setActive(false);

        EXPECTED_RESPONSE = """
                {"id":"%s",
                "userId":"%s",
                "companyNumber":"%s",
                "companyName":"%s",
                "query":null,
                "active":%s,
                "created":"%s",
                "updated":null}
                """.formatted(TEST_ID, USER_ID, COMPANY_NUMBER, COMPANY_NAME, ACTIVE, CREATED);

        EXPECTED_RESPONSE_PAGED = """
                {"_embedded":{"subscriptionDocumentList":
                [
                %s,
                %s,
                %s
                ]},
                "_links":{"self":{"href":"http://localhost/following?companyNumber=1777777&number=0&size=10"}},
                "page":{"size":3,"totalElements":3,"totalPages":1,"number":0}}
                """.formatted(EXPECTED_RESPONSE, EXPECTED_RESPONSE, EXPECTED_RESPONSE);
    }

    @Test
    void shouldReturnListOfSubscriptions() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), eq(ERIC_PASSTHROUGH),
                any(Pageable.class))).thenReturn(SUBSCRIPTIONS);
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("companyNumber", TEST_COMPANY_NUMBER).queryParam("number", 0)
                .queryParam("size", 10).encode().toUriString();
        mockMvc.perform(get(template).header(ApiSdkManager.getEricPassthroughTokenHeader(),
                                ERIC_PASSTHROUGH).header(ERIC_IDENTITY, ERIC_IDENTITY)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)).andDo(print())
                .andExpectAll(status().isOk(), content().json(EXPECTED_RESPONSE_PAGED));
    }

    @Test
    void shouldReturnStatus416() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), eq(ERIC_PASSTHROUGH),
                any(Pageable.class))).thenThrow(new ArrayIndexOutOfBoundsException());

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("size", Integer.MAX_VALUE - 10).queryParam("number", 10).encode()
                .toUriString();
        mockMvc.perform(get(template).header(ApiSdkManager.getEricPassthroughTokenHeader(),
                                ERIC_PASSTHROUGH).header(ERIC_IDENTITY, ERIC_IDENTITY)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)).andDo(print())
                .andExpect(status().isRequestedRangeNotSatisfiable());
    }

    @Test
    void shouldReturnSubscription() throws Exception {
        when(subscriptionService.getSubscription(anyString(), anyString(),
                eq(ERIC_PASSTHROUGH))).thenReturn(ACTIVE_SUBSCRIPTION);

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();
        mockMvc.perform(get(template).header(ApiSdkManager.getEricPassthroughTokenHeader(),
                                ERIC_PASSTHROUGH).header(ERIC_IDENTITY, ERIC_IDENTITY)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)).andDo(print())
                .andExpect(status().isOk()).andExpect(content().json(EXPECTED_RESPONSE));
    }

    @Test
    void shouldDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(delete("http://localhost/following").with(csrf())
                .header(ERIC_IDENTITY, ERIC_IDENTITY)
                .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                .content(objectMapper.writeValueAsString(deletePayload))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());

        when(subscriptionService.getSubscription(anyString(), anyString(),
                eq(ERIC_PASSTHROUGH))).thenThrow(new ServiceException("Not found"));

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();

        mockMvc.perform(get(template).header(ERIC_IDENTITY, ERIC_IDENTITY)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE)
                        .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_PASSTHROUGH))
                .andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    void shouldFailToDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        doThrow(new ServiceException("Service exception")).when(subscriptionService)
                .deleteSubscription(anyString(), anyString());

        mockMvc.perform(delete("http://localhost/following").with(csrf())
                        .header(ERIC_IDENTITY, ERIC_IDENTITY).header(ERIC_IDENTITY_TYPE,
                                ERIC_IDENTITY_TYPE_VALUE)
                        .content(objectMapper.writeValueAsString(deletePayload))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isInternalServerError());

    }

    @Test
    void shouldCreateSubscription() throws Exception {
        InputSubscription createPayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("http://localhost/following").header(ERIC_IDENTITY, ERIC_IDENTITY)
                        .header(ERIC_IDENTITY_TYPE, ERIC_IDENTITY_TYPE_VALUE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk());
    }
}

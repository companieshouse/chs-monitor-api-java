package uk.gov.companieshouse.chsmonitorapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

@WebMvcTest(ChsMonitorApiController.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@EnableSpringDataWebSupport
class ChsMonitorApiControllerTest {

    private final String TEST_COMPANY_NUMBER = "1777777";
    private final String TEST_ID = "TEST_ID";
    private final String COMPANY_NAME = "12345678";
    private final String COMPANY_NUMBER = "TEST_COMPANY";
    private final String USER_ID = "TEST_USER";
    private final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final SubscriptionDocument SUBSCRIPTION = new SubscriptionDocument();
    private final LocalDateTime CREATED = NOW.minus(Period.ofDays(1))
            .truncatedTo(ChronoUnit.SECONDS);
    private final boolean ACTIVE = true;
    private Page<SubscriptionDocument> SUBSCRIPTIONS;
    private String EXPECTED_RESPONSE;
    private String RESPONSE_JSON_OBJECT;

    @MockBean
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;


    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    //    complaining about env vars now
    @Test
    @WithAnonymousUser
    void shouldBlockUnauthorizedCalls() throws Exception {
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/" + TEST_COMPANY_NUMBER)).andDo(print())
                .andExpect(status().isForbidden());
    }

    @BeforeEach
    void beforeEach() {
        SUBSCRIPTION.setId(TEST_ID);
        SUBSCRIPTION.setActive(ACTIVE);
        SUBSCRIPTION.setCreated(NOW);
        SUBSCRIPTION.setCompanyName(COMPANY_NAME);
        SUBSCRIPTION.setCompanyNumber(COMPANY_NUMBER);
        SUBSCRIPTION.setUserId(USER_ID);
        SUBSCRIPTION.setCreated(CREATED);
        SUBSCRIPTIONS = new PageImpl<>(List.of(SUBSCRIPTION, SUBSCRIPTION, SUBSCRIPTION));

        RESPONSE_JSON_OBJECT = """
                {"id":"%s",
                "userId":"%s",
                "companyNumber":"%s",
                "companyName":"%s",
                "query":null,
                "active":%s,
                "created":"%s",
                "updated":null}
                """.formatted(TEST_ID, USER_ID, COMPANY_NUMBER, COMPANY_NAME, ACTIVE, CREATED);

        EXPECTED_RESPONSE = """
                {"content":
                [
                %s,
                %s,
                %s
                ],
                "pageable":"INSTANCE",
                "totalElements":3,
                "last":true,
                "totalPages":1,
                "size":3,
                "number":0,
                "sort":{"empty":true,"unsorted":true,"sorted":false},
                "numberOfElements":3,
                "first":true,
                "empty":false}
                """.formatted(RESPONSE_JSON_OBJECT, RESPONSE_JSON_OBJECT, RESPONSE_JSON_OBJECT);
    }

    @Test
    @WithMockUser
    void shouldReturnListOfSubscriptions() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), any(Pageable.class))).thenReturn(
                SUBSCRIPTIONS);
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/")
                .queryParam("companyNumber", TEST_COMPANY_NUMBER).queryParam("startIndex", 0)
                .queryParam("itemsPerPage", 10).encode().toUriString();
        mockMvc.perform(get(template)).andDo(print())
                .andExpectAll(status().isOk(), content().json(EXPECTED_RESPONSE));
    }

    @Test
    @WithMockUser
    void shouldFailWithNullValues() throws Exception {
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/")
                .queryParam("startIndex", Optional.empty()).queryParam("itemsPerPage", 10).encode()
                .toUriString();
        mockMvc.perform(get(template)).andDo(print()).andExpectAll(status().isBadRequest(),
                status().reason("Required parameter 'startIndex' is not present."));

        template = UriComponentsBuilder.fromHttpUrl("http://localhost/")
                .queryParam("startIndex", 0).queryParam("itemsPerPage", Optional.empty()).encode()
                .toUriString();
        mockMvc.perform(get(template)).andDo(print()).andExpectAll(status().isBadRequest(),
                status().reason("Required parameter 'itemsPerPage' is not present."));
    }

    @Test
    @WithMockUser
    void shouldReturnStatus416() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), any(Pageable.class))).thenThrow(
                new ArrayIndexOutOfBoundsException());

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/")
                .queryParam("startIndex", Integer.MAX_VALUE - 10).queryParam("itemsPerPage", 10)
                .encode().toUriString();
        mockMvc.perform(get(template)).andDo(print())
                .andExpect(status().isRequestedRangeNotSatisfiable());
    }
}

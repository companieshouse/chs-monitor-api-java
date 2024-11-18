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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.InputSubscription;
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
                "_links":{"self":{"href":"http://localhost/following?companyNumber=1777777&startIndex=0&itemsPerPage=10"}},
                "page":{"size":3,"totalElements":3,"totalPages":1,"number":0}}
                """.formatted(EXPECTED_RESPONSE, EXPECTED_RESPONSE, EXPECTED_RESPONSE);
    }

    @Test
    @WithMockUser
    void shouldReturnListOfSubscriptions() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), any(Pageable.class))).thenReturn(
                SUBSCRIPTIONS);
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("companyNumber", TEST_COMPANY_NUMBER).queryParam("startIndex", 0)
                .queryParam("itemsPerPage", 10).encode().toUriString();
        System.out.println(EXPECTED_RESPONSE_PAGED);
        mockMvc.perform(get(template)).andDo(print())
                .andExpectAll(status().isOk(), content().json(EXPECTED_RESPONSE_PAGED));
    }

    @Test
    @WithMockUser
    void shouldFailWithNullValues() throws Exception {
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("startIndex", Optional.empty()).queryParam("itemsPerPage", 10).encode()
                .toUriString();
        mockMvc.perform(get(template)).andDo(print()).andExpectAll(status().isBadRequest(),
                status().reason("Required parameter 'startIndex' is not present."));

        template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
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

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("startIndex", Integer.MAX_VALUE - 10).queryParam("itemsPerPage", 10)
                .encode().toUriString();
        mockMvc.perform(get(template)).andDo(print())
                .andExpect(status().isRequestedRangeNotSatisfiable());
    }

    @Test
    @WithMockUser
    void shouldReturnSubscription() throws Exception {
        when(subscriptionService.getSubscription(anyString(), anyString())).thenReturn(
                ACTIVE_SUBSCRIPTION);

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();
        mockMvc.perform(get(template)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(EXPECTED_RESPONSE));
    }

    @Test
    @WithMockUser
    void shouldDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(delete("http://localhost/following").with(csrf())
                .content(objectMapper.writeValueAsString(deletePayload))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());

        when(subscriptionService.getSubscription(anyString(), anyString())).thenThrow(
                new ServiceException("Not found"));

        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following/1777777")
                .encode().toUriString();

        mockMvc.perform(get(template)).andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void shouldFailToDeleteSubscription() throws Exception {
        InputSubscription deletePayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();

        doThrow(new ServiceException("Service exception")).when(subscriptionService)
                .deleteSubscription(anyString(), anyString());

        mockMvc.perform(delete("http://localhost/following").with(csrf())
                        .content(objectMapper.writeValueAsString(deletePayload))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isInternalServerError());

    }

    @Test
    @WithMockUser
    void shouldCreateSubscription() throws Exception {
        InputSubscription createPayload = new InputSubscription("1777777");
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("http://localhost/following").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isOk());
    }
}

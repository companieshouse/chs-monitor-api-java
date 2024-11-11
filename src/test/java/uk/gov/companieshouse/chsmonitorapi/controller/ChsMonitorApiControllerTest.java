package uk.gov.companieshouse.chsmonitorapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.chsmonitorapi.model.Subscription;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;

@SpringBootTest
@AutoConfigureMockMvc
class ChsMonitorApiControllerTest {

    private final String TEST_COMPANY_NUMBER = "1777777";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SubscriptionService subscriptionService;

    private final String TEST_ID = "TEST_ID";
    private final String COMPANY_NAME = "12345678";
    private final String COMPANY_NUMBER = "TEST_COMPANY";
    private final String USER_ID = "TEST_USER";
    private final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final List<Subscription> SUBSCRIPTIONS = new ArrayList<>();
    private final Subscription SUBSCRIPTION = new Subscription();
    private final LocalDateTime CREATED = NOW.minus(Period.ofDays(1)).truncatedTo(ChronoUnit.SECONDS);
    private final boolean ACTIVE = true;
    private String EXPECTED_RESPONSE;


    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
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
        SUBSCRIPTIONS.addFirst(SUBSCRIPTION);

        EXPECTED_RESPONSE = """
                [{"id":"%s",
                "userId":"%s",
                "companyNumber":"%s",
                "companyName":"%s",
                "query":null,
                "active":%s,
                "created":"%s",
                "updated":null}]
                """.formatted(TEST_ID, USER_ID, COMPANY_NUMBER, COMPANY_NAME, ACTIVE, CREATED);
    }

    @Test
    @WithMockUser
    void shouldReturnListOfSubscriptions() throws Exception {
        when(subscriptionService.getSubscriptions(anyString(), anyInt(), anyInt())).thenReturn(
                SUBSCRIPTIONS);
        String template = UriComponentsBuilder.fromHttpUrl("http://localhost/following")
                .queryParam("companyNumber", TEST_COMPANY_NUMBER).queryParam("startIndex", 0)
                .queryParam("itemsPerPage", 10).encode().toUriString();
        mockMvc.perform(get(template)).andDo(print())
                .andExpectAll(status().isOk(), content().json(EXPECTED_RESPONSE));
    }
}

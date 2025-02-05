package uk.gov.companieshouse.chsmonitorapi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

@DataMongoTest
@Testcontainers
class MonitorMongoRepositoryTest {

    public static final String USER_ID = "userId";
    public static final String VALID_COMPANY_NUMBER = "12345678";
    public static final String INVALID_COMPANY_NUMBER = "1234";
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    private static final int PAGE_REQUEST_NUMBER = 0;
    private static final int PAGE_REQUEST_SIZE = 2;
    private static final int EXPECTED_TOTAL_PAGES = 6;
    private static final int EXPECTED_TOTAL_ELEMENTS = 11;
    private static final int EXPECTED_PAGE_COUNT = 2;
    @Autowired
    private MonitorMongoRepository monitorMongoRepository;
    private SubscriptionDocument document;

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        document = getSubscriptionDocument();
        monitorMongoRepository.insert(document);
    }

    @AfterEach
    void tearDown() {
        monitorMongoRepository.deleteAll();
    }

    @Test
    void testFindSubscriptionByCompanyNumber() {
        Optional<SubscriptionDocument> retrievedDocument =
                monitorMongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(
                USER_ID, VALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isPresent());
    }

    @Test
    void testFindSubscriptionByCompanyNumber_IsEmpty() {
        Optional<SubscriptionDocument> retrievedDocument =
                monitorMongoRepository.findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(
                USER_ID, INVALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isEmpty());
    }

    @Test
    void testPaginationQuery() {
        List<SubscriptionDocument> docs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            docs.add(new SubscriptionDocument(USER_ID,
                    String.valueOf(Integer.parseInt(VALID_COMPANY_NUMBER + i + 1)),
                    "CompanyName" + i, "", true, LocalDateTime.now(), LocalDateTime.now()));
        }
        monitorMongoRepository.insert(docs);
        PageRequest pageRequest = PageRequest.of(PAGE_REQUEST_NUMBER, PAGE_REQUEST_SIZE);
        Page<SubscriptionDocument> documentPage =
                monitorMongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(
                USER_ID, pageRequest);

        assertEquals(EXPECTED_TOTAL_PAGES, documentPage.getTotalPages());
        assertEquals(EXPECTED_TOTAL_ELEMENTS, documentPage.getTotalElements());
        assertEquals(EXPECTED_PAGE_COUNT, documentPage.stream().count());

        List<String> firstCompanyNumbers = List.of(
                documentPage.toList().getFirst().getCompanyNumber(),
                documentPage.toList().getLast().getCompanyNumber());
        List<String> secondCompanyNumbers = List.of(
                documentPage.toList().getFirst().getCompanyNumber(),
                documentPage.toList().getLast().getCompanyNumber());
        assertNotEquals(documentPage.toList().getFirst().getCompanyNumber(),
                documentPage.toList().getLast().getCompanyNumber());

        documentPage = monitorMongoRepository.findSubscriptionsByUserIdAndActiveIsTrue(USER_ID,
                pageRequest.next());

        List<String> thirdCompanyNumbers = List.of(
                documentPage.toList().getFirst().getCompanyNumber(),
                documentPage.toList().getLast().getCompanyNumber());

        assertTrue(firstCompanyNumbers.containsAll(secondCompanyNumbers));
        assertFalse(firstCompanyNumbers.containsAll(thirdCompanyNumbers));
        assertFalse(documentPage.isFirst());
        assertEquals(EXPECTED_PAGE_COUNT, documentPage.stream().count());
    }

    private @NotNull SubscriptionDocument getSubscriptionDocument() {
        document = new SubscriptionDocument();
        document.setCompanyNumber(VALID_COMPANY_NUMBER);
        document.setUserId(USER_ID);
        document.setActive(true);
        return document;
    }
}

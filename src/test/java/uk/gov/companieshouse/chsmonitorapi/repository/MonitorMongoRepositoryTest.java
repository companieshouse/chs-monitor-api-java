package uk.gov.companieshouse.chsmonitorapi.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
    @Autowired
    private MonitorMongoRepository monitorMongoRepository;

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        SubscriptionDocument document = getSubscriptionDocument();
        monitorMongoRepository.insert(document);
    }

    @AfterEach
    void tearDown(){
        monitorMongoRepository.deleteById("testId");
    }

    @Test
    void testFindSubscriptionByCompanyNumber() {
        Optional<SubscriptionDocument> retrievedDocument =
                monitorMongoRepository.findSubscriptionByUserIdAndCompanyNumber(
                USER_ID, VALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isPresent());
    }

    @Test
    void testFindSubscriptionByCompanyNumber_IsEmpty() {
        Optional<SubscriptionDocument> retrievedDocument =
                monitorMongoRepository.findSubscriptionByUserIdAndCompanyNumber(
                USER_ID, INVALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isEmpty());
    }

    private @NotNull SubscriptionDocument getSubscriptionDocument() {
        SubscriptionDocument document = new SubscriptionDocument();
        document.setCompanyNumber(VALID_COMPANY_NUMBER);
        document.setId("testId");
        return document;
    }
}

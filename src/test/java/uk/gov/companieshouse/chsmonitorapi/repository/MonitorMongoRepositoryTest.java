package uk.gov.companieshouse.chsmonitorapi.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
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

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    public static final String VALID_COMPANY_NUMBER = "12345678";
    public static final String INVALID_COMPANY_NUMBER = "1234";

    @Autowired
    private MonitorMongoRepository monitorMongoRepository;

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void setUp(@Autowired MonitorMongoRepository mongoRepository) {
        SubscriptionDocument document = new SubscriptionDocument();
        document.setCompanyNumber(VALID_COMPANY_NUMBER);
        mongoRepository.save(document);
    }

    @Test
    void testFindSubscriptionByCompanyNumber() {
        Optional<SubscriptionDocument> retrievedDocument = monitorMongoRepository.findSubscriptionByCompanyNumber(
                VALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isPresent());
    }

    @Test
    void testFindSubscriptionByCompanyNumber_IsEmpty() {
        Optional<SubscriptionDocument> retrievedDocument = monitorMongoRepository.findSubscriptionByCompanyNumber(
                INVALID_COMPANY_NUMBER);

        assertTrue(retrievedDocument.isEmpty());
    }

}

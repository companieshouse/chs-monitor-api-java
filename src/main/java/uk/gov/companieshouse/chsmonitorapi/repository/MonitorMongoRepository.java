package uk.gov.companieshouse.chsmonitorapi.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

@Repository
public interface MonitorMongoRepository extends MongoRepository<SubscriptionDocument, String> {

    Optional<SubscriptionDocument> findSubscriptionByCompanyNumber(String companyNumber);
}

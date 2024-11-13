package uk.gov.companieshouse.chsmonitorapi.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

@Repository
public interface MonitorMongoRepository extends MongoRepository<SubscriptionDocument, String> {

    Optional<SubscriptionDocument> findSubscriptionByUserIdAndCompanyNumber(String userId,
            String companyNumber);

    Page<SubscriptionDocument> findSubscriptionsByUserId(String userId, Pageable pageable);

    void deleteAllByUserIdAndCompanyNumber(String userId, String companyNumber);
}

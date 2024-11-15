package uk.gov.companieshouse.chsmonitorapi.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

@Repository
public interface MonitorMongoRepository extends MongoRepository<SubscriptionDocument, String> {

    Optional<SubscriptionDocument> findSubscriptionByUserIdAndCompanyNumberAndActiveIsTrue(
            String userId, String companyNumber);

    Optional<SubscriptionDocument> findSubscriptionByUserIdAndCompanyNumberAndActiveIsFalse(
            String userId, String companyNumber);

    Page<SubscriptionDocument> findSubscriptionsByUserIdAndActiveIsTrue(String userId,
            Pageable pageable);

    void deleteAllByUserIdAndCompanyNumber(String userId, String companyNumber);

    @Update("{ '$set' : { 'active' : ?2 } }")
    void findAndSetActiveByUserIdAndCompanyNumber(String userId, String companyNumber,
            boolean active);
}

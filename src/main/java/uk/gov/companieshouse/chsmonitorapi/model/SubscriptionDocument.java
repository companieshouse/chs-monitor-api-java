package uk.gov.companieshouse.chsmonitorapi.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Subscription represents a monitor subscription.
 */
@Document(collection = "query")
public class SubscriptionDocument {

    @Id
    private String id;
    private String userId;
    private String companyNumber;
    private String companyName;
    private String query;
    private boolean active;
    private LocalDateTime created;
    private LocalDateTime updated;

    public SubscriptionDocument() {
    }

    public SubscriptionDocument(String userId, String companyNumber, String companyName,
            String query, boolean active, LocalDateTime created, LocalDateTime updated) {
        this.userId = userId;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.query = query;
        this.active = active;
        this.created = created;
        this.updated = updated;
    }

    public SubscriptionDocument(SubscriptionDocument subscriptionDocument) {
        this.userId = subscriptionDocument.getUserId();
        this.companyNumber = subscriptionDocument.getCompanyNumber();
        this.companyName = subscriptionDocument.getCompanyName();
        this.query = subscriptionDocument.getQuery();
        this.active = subscriptionDocument.isActive();
        this.created = subscriptionDocument.getCreated();
        this.updated = subscriptionDocument.getUpdated();
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets company number.
     *
     * @return the company number
     */
    public String getCompanyNumber() {
        return companyNumber;
    }

    /**
     * Sets company number.
     *
     * @param companyNumber the company number
     */
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    /**
     * Gets company name.
     *
     * @return the company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets company name.
     *
     * @param companyName the company name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Gets query.
     *
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets query.
     *
     * @param query the query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets active.
     *
     * @param active the active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets created.
     *
     * @return the created
     */
    public LocalDateTime getCreated() {
        return created;
    }

    /**
     * Sets created.
     *
     * @param created the created
     */
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    /**
     * Gets updated.
     *
     * @return the updated
     */
    public LocalDateTime getUpdated() {
        return updated;
    }

    /**
     * Sets updated.
     *
     * @param updated the updated
     */
    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }
}

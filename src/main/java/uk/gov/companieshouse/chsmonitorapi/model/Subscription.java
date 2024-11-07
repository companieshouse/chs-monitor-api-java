package uk.gov.companieshouse.chsmonitorapi.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;

/**
 * Subscription represents a monitor subscription.
 */
public class Subscription {

    @Id
    private String id;
    private String userId;
    private String companyNumber;
    private String companyName;
    private String query;
    private boolean active;
    private LocalDateTime created;
    private LocalDateTime updated;

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

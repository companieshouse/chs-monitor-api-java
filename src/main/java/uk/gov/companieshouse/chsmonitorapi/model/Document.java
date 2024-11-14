package uk.gov.companieshouse.chsmonitorapi.model;

import java.util.List;

/**
 * Document contains subscriptions metadata.
 */
public class Document {

    private int itemsPerPage;
    private int startIndex;
    private int totalCount;
    private String kind;
    private List<SubscriptionDocument> items;

    /**
     * Gets items per page.
     *
     * @return the items per page
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * Sets items per page.
     *
     * @param itemsPerPage the items per page
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Gets start index.
     *
     * @return the start index
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets start index.
     *
     * @param startIndex the start index
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Gets total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets total count.
     *
     * @param totalCount the total count
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Gets kind.
     *
     * @return the kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets kind.
     *
     * @param kind the kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<SubscriptionDocument> getItems() {
        return items;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems(List<SubscriptionDocument> items) {
        this.items = items;
    }
}
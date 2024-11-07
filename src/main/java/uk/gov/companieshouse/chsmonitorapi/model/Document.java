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
    private List<Subscription> items;

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<Subscription> getItems() {
        return items;
    }

    public void setItems(List<Subscription> items) {
        this.items = items;
    }
}
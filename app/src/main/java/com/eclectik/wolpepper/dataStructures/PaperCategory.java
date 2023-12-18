package com.eclectik.wolpepper.dataStructures;

/**
 * Created by mj on 1/7/17.
 */

public class PaperCategory {
    private String categoryTitle;
    private String categoryImageUrl;

    public PaperCategory() {
    }

    public PaperCategory(String categoryTitle, String categoryImageUrl) {
        this.categoryTitle = categoryTitle;
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    public void setCategoryImageUrl(String categoryImageUrl) {
        this.categoryImageUrl = categoryImageUrl;
    }


}

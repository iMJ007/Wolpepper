package com.eclectik.wolpepper.dataStructures;

/**
 *
 * Created by mj on 12/6/17.
 *
 * Collection Data Structure
 *
 */

public class PaperCollections {

    private String collectionId, collectionTitle, collectionDescrip, collectionDate, collectionShareKey, collectionUserId, collectionUserName, collectionNameOfUser, collectionUserProfilePicUrl, collectionAllImageDownloadUrl;
    private String coverPhotoId, coverPhotoDisplayUrl, coverImageColor;
    private boolean isCurated, isFeatured;
    private int totalPhotos;

    public PaperCollections() {
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public String getCollectionDescrip() {
        return collectionDescrip;
    }

    public void setCollectionDescrip(String collectionDescrip) {
        this.collectionDescrip = collectionDescrip;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getCollectionShareKey() {
        return collectionShareKey;
    }

    public void setCollectionShareKey(String collectionShareKey) {
        this.collectionShareKey = collectionShareKey;
    }

    public String getCollectionUserId() {
        return collectionUserId;
    }

    public void setCollectionUserId(String collectionUserId) {
        this.collectionUserId = collectionUserId;
    }

    public String getCollectionUserName() {
        return collectionUserName;
    }

    public void setCollectionUserName(String collectionUserName) {
        this.collectionUserName = collectionUserName;
    }

    public String getCollectionNameOfUser() {
        return collectionNameOfUser;
    }

    public void setCollectionNameOfUser(String collectionNameOfUser) {
        this.collectionNameOfUser = collectionNameOfUser;
    }

    public String getCoverPhotoId() {
        return coverPhotoId;
    }

    public void setCoverPhotoId(String coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    public String getCoverPhotoDisplayUrl() {
        return coverPhotoDisplayUrl;
    }

    public void setCoverPhotoDisplayUrl(String coverPhotoDisplayUrl) {
        this.coverPhotoDisplayUrl = coverPhotoDisplayUrl;
    }

    public int getTotalPhotos() {
        return totalPhotos;
    }

    public void setTotalPhotos(int totalPhotos) {
        this.totalPhotos = totalPhotos;
    }

    public String getCollectionUserProfilePicUrl() {
        return collectionUserProfilePicUrl;
    }

    public void setCollectionUserProfilePicUrl(String collectionUserProfilePicUrl) {
        this.collectionUserProfilePicUrl = collectionUserProfilePicUrl;
    }

    public boolean isCurated() {
        return isCurated;
    }

    public void setCurated(boolean curated) {
        isCurated = curated;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public String getCoverImageColor() {
        return coverImageColor;
    }

    public void setCoverImageColor(String coverImageColor) {
        this.coverImageColor = coverImageColor;
    }

    public String getCollectionAllImageDownloadUrl() {
        return collectionAllImageDownloadUrl;
    }

    public void setCollectionAllImageDownloadUrl(String collectionAllImageDownloadUrl) {
        this.collectionAllImageDownloadUrl = collectionAllImageDownloadUrl;
    }
}

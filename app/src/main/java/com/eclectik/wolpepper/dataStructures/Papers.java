package com.eclectik.wolpepper.dataStructures;

import android.os.Parcel;
import android.os.Parcelable;

import com.eclectik.wolpepper.utils.ConstantValues;

/**
 * Created by mj on 5/6/17.
 */

public class Papers implements Parcelable{

    private String imageId,  date,  displayImageUrl, imageHtmlLink, fullImageUrl, rawImageUrl, camera, location, focalLength, exposureTime, aperture, color, iso, storyDesc, storyTitle, totalDownloads, totalViews, totalLikes;
    // Author variables
    private String authorName, authorImageUrl, authorId, authorUserName, profileUrl, authorBio;
    private int authorTotalLikes, authorTotalCollections, authorTotalPhotos, height, width;
    private boolean isLiked, isInMuzeiList, isPortraitResolution;
    private String crasher;
    private String downloadImageApiCallUrl, downloadImageUrl;

    public Papers() {
    }

    public Papers(String imageId, String authorName, String date, String authorImageUrl, String displayImageUrl, String imageHtmlLink){
        this.imageId = imageId;
        this.authorName = authorName;
        this.date = date;
        this.authorImageUrl = authorImageUrl;
        this.displayImageUrl = displayImageUrl;
        this.imageHtmlLink = imageHtmlLink;
    }

    public String getAuthorUserName() {
        return authorUserName;
    }

    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFullImageUrl() {
        return fullImageUrl;
    }

    public void setFullImageUrl(String fullImageUrl) {
        this.fullImageUrl = fullImageUrl;
    }

    public String getRawImageUrl() {
        return rawImageUrl;
    }

    public void setRawImageUrl(String rawImageUrl) {
        this.rawImageUrl = rawImageUrl;
    }

    public String getImageHtmlLink() {
        return imageHtmlLink;
    }

    public void setImageHtmlLink(String imageHtmlLink) {
        this.imageHtmlLink = imageHtmlLink;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthorImageUrl() {
        return authorImageUrl;
    }

    public void setAuthorImageUrl(String authorImageUrl) {
        this.authorImageUrl = authorImageUrl;
    }

    public String getDisplayImageUrl() {
        return displayImageUrl;
    }

    public void setDisplayImageUrl(String displayImageUrl) {
        this.displayImageUrl = displayImageUrl;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    public String getAperture() {
        return aperture;
    }

    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getStoryDesc() {
        return storyDesc;
    }

    public void setStoryDesc(String storyDesc) {
        this.storyDesc = storyDesc;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(String totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public String getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(String totalViews) {
        this.totalViews = totalViews;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(String totalLikes) {
        this.totalLikes = totalLikes;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public int getAuthorTotalLikes() {
        return authorTotalLikes;
    }

    public void setAuthorTotalLikes(int authorTotalLikes) {
        this.authorTotalLikes = authorTotalLikes;
    }

    public int getAuthorTotalCollections() {
        return authorTotalCollections;
    }

    public void setAuthorTotalCollections(int authorTotalCollections) {
        this.authorTotalCollections = authorTotalCollections;
    }

    public int getAuthorTotalPhotos() {
        return authorTotalPhotos;
    }

    public void setAuthorTotalPhotos(int authorTotalPhotos) {
        this.authorTotalPhotos = authorTotalPhotos;
    }

    public String getAuthorBio() {
        return authorBio;
    }

    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }

    public boolean isInMuzeiList() {
        return isInMuzeiList;
    }

    public void setInMuzeiList(boolean inMuzeiList) {
        isInMuzeiList = inMuzeiList;
    }

    public boolean isPortraitResolution() {
        return isPortraitResolution;
    }

    public String getCrasher() {
        if (crasher == null){
            throw new RuntimeException();
        } else {
            return crasher;
        }
    }

    public void setPortraitResolution(boolean portraitResolution) {
        isPortraitResolution = portraitResolution;
    }



    /*   ******************************** PARCELABLE METHODS ************************************ */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageId);
        dest.writeString(fullImageUrl);
        dest.writeString(authorName);
        dest.writeString(rawImageUrl);
        dest.writeString(imageHtmlLink);
        dest.writeString(displayImageUrl);
        dest.writeString(date);
        dest.writeString(authorImageUrl);
        dest.writeString(authorUserName);
        dest.writeInt(authorTotalCollections);
        dest.writeInt(authorTotalPhotos);
        dest.writeInt(authorTotalLikes);
        dest.writeString(profileUrl);
        dest.writeString(authorBio);
        dest.writeString(location);
        dest.writeByte((byte) (isLiked ? 1 : 0));     //if myBoolean == true, byte == 1
        dest.writeString(downloadImageApiCallUrl);
    }

    public void setCrasher(String crasher) {
        this.crasher = crasher;
    }

    public String getDownloadImageApiCallUrl() {
        return downloadImageApiCallUrl;
    }

    public void setDownloadImageApiCallUrl(String downloadImageApiCallUrl) {
        this.downloadImageApiCallUrl = downloadImageApiCallUrl;
    }

    public String getDownloadImageUrl() {
        return downloadImageUrl;
    }

    public void setDownloadImageUrl(String downloadImageUrl) {
        this.downloadImageUrl = downloadImageUrl;
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Papers> CREATOR = new Parcelable.Creator<Papers>() {
        public Papers createFromParcel(Parcel in) {
            return new Papers(in);
        }

        public Papers[] newArray(int size) {
            return new Papers[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values ****** :::: NOTE :: THE SEQUENCE IN THIS CONSTRUCTOR SHOULD BE SAME AS IN writeToParcel() METHOD
    private Papers(Parcel in) {
        imageId = in.readString();
        fullImageUrl = in.readString();
        authorName = in.readString();
        rawImageUrl = in.readString();
        imageHtmlLink = in.readString();
        displayImageUrl = in.readString();
        date = in.readString();
        authorImageUrl = in.readString();
        authorUserName = in.readString();
        authorTotalCollections = in.readInt();
        authorTotalPhotos = in.readInt();
        authorTotalLikes = in.readInt();
        profileUrl = in.readString();
        authorBio = in.readString();
        location = in.readString();
        isLiked = in.readByte() != 0;
        downloadImageApiCallUrl = in.readString();
    }
}

package com.eclectik.wolpepper.utils;


import android.os.Environment;

/**
 * Created by MJ on 5/22/2017.
 * CONSTANT STRINGS
 */

public class ConstantValues {

    // UNSPLASH BASE LINKS STRINGS
    public static final String BASE_UNSPLASH_API_STRING = "https://api.unsplash.com/";
    public static final String BASE_UNSPLASH_URL_STRING = "https://unsplash.com/";
    public static final String UNSPLASH_JOIN_URL_STRING = "https://unsplash.com/join";
    public static final String UNSPLASH_SUBMIT_URL_STRING = "https://unsplash.com/submit";
    public static final String UNSPLASH_IMAGE_EXTRA_INFO_NAPI_STRING = "https://unsplash.com/napi/photos/ENTER_IMAGE_ID_HERE/info";
    public static final String UNSPLASH_LOGIN_CALLBACK_STRING = "unsplash-auth-callback";
    public static final String UNSPLASH_PHOTO_PATH_STRING = "photos";
    public static final String UNSPLASH_COLLECTIONS_PATH_STRING = "collections";
    public static final String UNSPLASH_CURATED_PATH_STRING = "curated";
    public static final String UNSPLASH_FEATURED_PATH_STRING = "featured";
    public static final String UNSPLASH_DOWNLOAD_PATH_STRING = "download";
    public static final String UNSPLASH_SEARCH_PATH_STRING = "search";
    public static final String UNSPLASH_USER_LIKES_PATH_STRING = "likes";
    public static final String UNSPLASH_USERS_PATH_STRING = "users";
    public static final String UNSPLASH_OAUTH_PATH_STRING = "oauth";

    // STORAGE FOLDER PATH
    public static final String IMAGE_LOCAL_STORAGE_FOLDER_NAME = "/WolPeppers";
    public static final String SOLID_COLORS_STORAGE_FOLDER_NAME = "SolidColorPeppers";
    public static final String IMAGE_CURRENT_PAPER_CACHE = "WolCache";
    public static final String IMAGE_LOCAL_STORAGE_FULL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/";
    public static final String SOLID_COLORS_STORAGE_FULL_PATH = IMAGE_LOCAL_STORAGE_FULL_PATH + SOLID_COLORS_STORAGE_FOLDER_NAME + "/";
    public static final String IMAGE_LOCAL_RAW_STORAGE_FULL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/RAW/";

    // HTML LINKS
    public static final String UNSPLASH_HTML_JSON_SELECTED_PHOTO = "asyncPropsSelectedPhoto";
    public static final String UNSPLASH_HTML_JSON_PROPS_PHOTOS = "asyncPropsPhotos";
    public static final String UNSPLASH_HTML_JSON_PROPS_USERS = "asyncPropsUsers";
    public static final String UNSPLASH_HTML_JSON_SELECTED_USER = "asyncPropsSelectedUser";

    //JSON ID's
    public static final String IMAGE_ID_KEY = "id";
    public static final String IMAGE_CREATED_DATE_KEY = "created_at";
    public static final String IMAGE_WIDTH_KEY = "width";
    public static final String IMAGE_HEIGHT_KEY = "height";
    public static final String IMAGE_COLOR_KEY = "color";
    public static final String IMAGE_TOTAL_DOWNLOADS_KEY = "downloads";
    public static final String IMAGE_TOTAL_VIEWS_KEY = "views";
    public static final String IMAGE_TOTAL_LIKES_KEY = "likes";
    public static final String IMAGE_LIKED_BY_USER_KEY = "liked_by_user";
    public static final String IMAGE_EXIF_KEY = "exif";
    public static final String IMAGE_CAMERA_MAKE_KEY = "make";
    public static final String IMAGE_CAMERA_MODEL_KEY = "model";
    public static final String IMAGE_EXPOSURE_TIME_KEY = "exposure_time";
    public static final String IMAGE_APERTURE_KEY = "aperture";
    public static final String IMAGE_FOCAL_LENGTH_KEY = "focal_length";
    public static final String IMAGE_ISO_KEY = "iso";
    public static final String IMAGE_LOCATION_KEY = "location";
    public static final String IMAGE_LOCATION_TITLE_KEY = "title";
    public static final String IMAGE_URLS_KEY = "urls";
    public static final String IMAGE_RAW_IMAGE_URL_KEY = "raw";
    public static final String IMAGE_FULL_IMAGE_URL_KEY = "full";
    public static final String IMAGE_REGULAR_IMAGE_URL_KEY = "regular";
    public static final String IMAGE_CATEGORY_KEY = "categories";
    public static final String IMAGE_LINKS_KEY = "links";
    public static final String IMAGE_HTML_LINK_KEY = "html";
    public static final String IMAGE_DOWNLOAD_LINK_KEY = "download";
    public static final String IMAGE_STORY_KEY = "story";
    public static final String IMAGE_STORY_TITLE_KEY = "title";
    public static final String IMAGE_STORY_DESCRIPTION_KEY = "description";
    public static final String IMAGE_RELATED_TAGS_KEY = "tags";
    public static final String IMAGE_RELATED_TAGS_TITLE_KEY = "title";
    public static final String IMAGE_RELATED_TAGS_URL_KEY = "url";
    public static final String IMAGE_RELATED_COLLECTIONS = "related_collections";
    public static final String IMAGE_TOTAL_RELATED_COLLECTIONS_KEY = "total";
    public static final String IMAGE_RELATED_COLLECTION_TYPE_KEY = "type";
    public static final String IMAGE_RELATED_COLLECTIONS_IDS_KEY = "result_ids";
    public static final String IMAGE_DOWNLOAD_API_CALL_LINK_KEY = "download_location";
    public static final String URL_KEY = "url";

    // USERS
    public static final String IMAGE_USER_DETAILS_OBJECT_KEY = "user";
    public static final String IMAGE_USER_ID_KEY = "id";
    public static final String IMAGE_EXTRA_DETAILS_USER_ID_KEY = "userId";
    public static final String IMAGE_USERNAME_KEY = "username";
    public static final String IMAGE_NAME_OF_USER_KEY = "name";
    public static final String IMAGE_USER_BIO_KEY = "bio";
    public static final String IMAGE_USER_TOTAL_LIKES = "total_likes";
    public static final String IMAGE_USER_TOTAL_PHOTOS = "total_photos";
    public static final String IMAGE_USER_TOTAL_COLLECTIONS = "total_collections";
    public static final String IMAGE_USER_FOLLOWERS_COUNT_KEY = "followers_count";
    public static final String IMAGE_USER_FOLLOWING_COUNT_KEY = "following_count";
    public static final String IMAGE_USER_PROFILE_LINK_KEY = "profile";
    public static final String IMAGE_USER_PROFILE_PIC_KEY = "profile_image";
    public static final String IMAGE_USER_PROFILE_PIC_SMALL_KEY = "small";
    public static final String IMAGE_USER_PROFILE_PIC_MEDIUM_KEY = "medium";
    public static final String IMAGE_USER_PROFILE_PIC_LARGE_KEY = "large";
    public static final String IMAGE_USER_PROFILE_PIC_CUSTOM_KEY = "custom";

    // JSON EXTRACTION BEGIN AND END POINTS
    public static final String IMAGE_JSON_BEGIN_TEXT = "<script>__ASYNC_PROPS__ = [";
    public static final String IMAGE_JSON_END_TEXT = "]</script>";
    public static final String PROFILE_JSON_BEGIN_TEXT = "<script>__ASYNC_PROPS__ = ";
    public static final String PROFILE_JSON_END_TEXT = "</script>";

    public static final String CALLING_ACTIVITY_NAME = "activity_name";
    public static final String HAVE_PARENT_ACTIVITY = "does_have_parent_activity";

    // PLACEHOLDER TEXT
    public static final String PREMIUM_UPGRADE_REQUIRED_WIDGET_TOAST = "Upgrade to premium to enjoy all widget features.";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    public static final String SKU_PREMIUM = "premium";

    public static class PreferencesKeys{
        public static final String IMAGE_EXTRA_DETAILS_PREFERENCES_BASE_KEY = "image_extra_details";
        public static final String IMAGE_DOWNLOAD_EXTRA_WORK_PREFERENCES_BASE_KEY = "extra_image";
        public static final String IMAGE_DOWNLOAD_IS_GREY_ENABLED_KEY = "is_grey_enabled";
        public static final String IMAGE_DOWNLOAD_SET_WALLPAPER_ENABLED_KEY = "is_set_wallpaper";
        public static final String IMAGE_DOWNLOAD_IS_PORTRAIT_KEY = "is_portrait";
        public static final String IMAGE_CACHE_PREF_DATE_BASE_KEY = "base_date_prefs";
        public static final String IMAGE_CACHE_PREF_DATE_KEY = "cache_date";
        public static final String IMAGE_SHARE_ENABLED_KEY = "share_image";
        public static final String IMAGE_SET_AS_INTENT_KEY = "set_as_intent";
        public static final String FULL_WIDGET_UNLOCKED_PREFENCES_BASE_KEY = "perform_all_buttons_base_pref";
        public static final String FULL_WIDGET_UNLOCKED_KEY = "perform_all_buttons";
    }

    public static class CollectionKeys{
        public static final String COLLECTION_ID_KEY = "id";
        public static final String COLLECTION_DATE_KEY = "published_at";
        public static final String COLLECTION_TITLE_KEY = "title";
        public static final String COLLECTION_DESCRIPTION_KEY = "description";
        public static final String COLLECTION_TOTAL_PHOTOS_KEY = "total_photos";
        public static final String COLLECTION_SHARE_KEY_KEY = "share_key";
        public static final String COLLECTION_COVER_PHOTO_KEY = "cover_photo";
        public static final String COLLECTION_IS_CURATED_KEY = "curated";
        public static final String COLLECTIN_IS_FEATURED_KEY = "featured";
    }

    public static class HeaderKeys{
        public static final String TOTAL_RATE_LIMIT_HEADER_KEY = "X-Ratelimit-Limit";
        public static final String RATE_LIMIT_REMAINING_HEADER_KEY = "X-Ratelimit-Remaining";
    }

    public static class OrderByKeys{
        public static final String ORDER_BY_LATEST = "latest";
        public static final String ORDER_BY_OLDEST = "oldest";
        public static final String ORDER_BY_POPULAR = "popular";
        public static final String ORDER_BY_RANDOM = "random";
    }

    public static class FilterbyKeys{
        public static final String FILTER_BY_FEATURED = "featured";
        public static final String FILTER_BY_CURATED = "curated";
    }

    public static class RequestMethodKeys{
        public static final String POST_METHOD = "POST";
        public static final String PUT_METHOD = "PUT";
        public static final String DELETE_METHOD = "DELETE";
    }

    public static class ErrorKeys{
        // RESPONSE ERRORS
        public static final String RESPONSE_TIMEOUT_ERROR = "connection_error";

        public static final String RESPONSE_SOMETHING_WENT_WRONG_ERROR = "something_went_wrong";
    }

    public static class ProfileFragTags{
        public static final String LIKE_FRAG_TAG = "like_frag";
        public static final String PHOTOS_FRAG_TAG = "photos_frag";
        public static final String COLLECTION_FRAG_TAG = "collection_frag";
    }

    public static class MuzeiReturnValues{
        public static final int IMAGE_ALREADY_EXIST_IN_LIST = -1;
        public static final int MAXIMUM_IMAGE_IN_A_LIST_REACHED = -2;
        public static final int MAXIMUM_LIST_ALLOWED_REACHED = -3;
        public static final int LIST_NAME_ALREADY_EXIST = -4;
        public static final int MUZEI_LIST_OPERATION_SUCCESSFUL = -5;
    }

    public static class IntentKeys{
        public static final String LIST_NAME = "list_name";
        public static final String ADAPTER_POSITION = "position";
        public static final int ACTIVITY_RESULT = 0;
    }

    public static class Base64Strings{
        public static final String PUBLIC_BASE_64_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyIzDtTPE1CcgJ5A8vzTyMLQM2H7CXGFeoT/";
    }

    public static class TestDevicesStrings {
        public static final String ADS_TEST_DEVICE_ID = "E23B650AE794D09851DE3B12CE7487A9";
    }

}

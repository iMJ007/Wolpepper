package com.eclectik.wolpepper.utils;


import static com.eclectik.wolpepper.utils.ConstantValues.BASE_UNSPLASH_API_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.BASE_UNSPLASH_URL_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.OrderByKeys.ORDER_BY_RANDOM;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_COLLECTIONS_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_CURATED_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_OAUTH_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_PHOTO_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_SEARCH_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_USERS_PATH_STRING;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * These utilities will be used to communicate with the network.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class NetworkUtils {

    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_PAGE_NUMBER = "page";
    private static final String PARAM_PER_PAGE_ITEM_COUNT = "per_page";
    private static final String PARAM_RANDOM_ITEM_COUNT = "count";
    private static final String PARAM_ORDER_BY = "order_by";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_REDIRECT_URI = "redirect_uri";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_UTM_SOURCE = "utm_source";
    private static final String PARAM_UTM_MEDIUM = "utm_medium";
    private static final String PARAM_UTM_CAMPAIGN = "utm_campaign";


    /**
     * Builds the URL used to get List of New Photos of single Page.
     *
     * @return The URL to use to query the Unsplash.
     */
    public static URL buildNewPhotosUrl(String clientId, String path, String pageNumber, String perPageItemCount, String orderBy) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(path)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                .appendQueryParameter(PARAM_ORDER_BY, orderBy)
                .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to get List of New Photos of single Page.
     *
     * @return The URL to use to query the Unsplash.
     */
    public static URL buildCuratedPhotosListUrl(String clientId, String pageNumber, String perPageItemCount) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                .appendPath(UNSPLASH_CURATED_PATH_STRING)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Build URL for random list of photos
     *
     * @param clientId  -
     * @param itemCount -
     * @return -
     */
    public static URL buildRandomPhotosListUrl(String clientId, String pageNumber, String itemCount) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                .appendPath(ORDER_BY_RANDOM)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_RANDOM_ITEM_COUNT, itemCount)
                .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to query Collections.
     *
     * @return The URL to use to query the UNSPLASH API FOR COLLECTIONS.
     */
    public static URL buildCollectionsUrl(String clientId, String path, String pageNumber, String perPageItemCount) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(path)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to query Collections Filtered by featured/curated/all.
     *
     * @param clientId         client ID
     * @param filter           Featured/Curated
     * @param pageNumber       The current page number to be retrieved
     * @param perPageItemCount item count per page
     * @return The URL to use to query the UNSPLASH API FOR COLLECTIONS.
     */
    public static URL buildCollectionsFilterUrl(String clientId, String filter, String pageNumber, String perPageItemCount) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_COLLECTIONS_PATH_STRING)
                .appendPath(filter)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildCollectionPhotosUrl(String clientId, String collectionId, String pageNumber, String perPageItemCount, boolean isCurated) {
        Uri builtUri;
        if (isCurated) {
            builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                    .appendPath(UNSPLASH_COLLECTIONS_PATH_STRING)
                    .appendPath(UNSPLASH_CURATED_PATH_STRING)
                    .appendPath(collectionId)
                    .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                    .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                    .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                    .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                    .build();
        } else {
            builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                    .appendPath(UNSPLASH_COLLECTIONS_PATH_STRING)
                    .appendPath(collectionId)
                    .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                    .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                    .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPageItemCount)
                    .appendQueryParameter(PARAM_CLIENT_ID, clientId)
                    .build();
        }
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    /**
     * Build and returns Authorisation URL
     *
     * @param appId       -
     * @param appSecret   -
     * @param redirectUri -
     * @param code        -
     * @return -
     */
    public static URL buildAuthUrl(String appId, String appSecret, String redirectUri, String code) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_URL_STRING).buildUpon()
                .appendPath(UNSPLASH_OAUTH_PATH_STRING)
                .appendPath("token")
                .appendQueryParameter(PARAM_CLIENT_ID, appId)
                .appendQueryParameter(PARAM_CLIENT_SECRET, appSecret)
                .appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
                .appendQueryParameter(PARAM_CODE, code)
                .appendQueryParameter(PARAM_GRANT_TYPE, "authorization_code")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildSelfProfileUrl(String path, String appId) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(path)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Build a specific users profile url
     *
     * @param userId-
     * @param appId-
     * @return -
     */
    public static URL buildUserProfileUrl(String userId, String appId) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_USERS_PATH_STRING)
                .appendPath(userId)
                .appendQueryParameter("w", "220")
                .appendQueryParameter(PARAM_CLIENT_ID, appId)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Build a specific users images/likes/collections url
     *
     * @param userId-
     * @param path    - The feature to get e.g likes / collections / images
     * @param appId-
     * @return -
     */
    public static URL buildUserContentUrl(String userId, String path, String pageNumber, String perPage, String appId) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_USERS_PATH_STRING)
                .appendPath(userId)
                .appendPath(path)
                .appendQueryParameter(PARAM_CLIENT_ID, appId)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, perPage)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    public static URL buildLikeUnlikeUrl(String imageId, String APP_ID) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                .appendPath(imageId)
                .appendPath("like")
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildSearchUrl(String query, String pageNumber, String APP_ID) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_SEARCH_PATH_STRING)
                .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                .appendQueryParameter(PARAM_QUERY, query)
                .appendQueryParameter(PARAM_PAGE_NUMBER, pageNumber)
                .appendQueryParameter(PARAM_PER_PAGE_ITEM_COUNT, "30")
                .appendQueryParameter(PARAM_CLIENT_ID, APP_ID)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Uri buildLinkWithUtmParameters(Context context, String link) {
        return Uri.parse(link).buildUpon()
                .appendQueryParameter(PARAM_UTM_SOURCE, context.getString(R.string.app_name))
                .appendQueryParameter(PARAM_UTM_MEDIUM, "referral")
                .appendQueryParameter(PARAM_UTM_CAMPAIGN, "api-credit")
                .build();
    }

    public static URL buildPhotoDetailUrl(String imageId) {
        Uri builtUri = Uri.parse(BASE_UNSPLASH_API_STRING).buildUpon()
                .appendPath(UNSPLASH_PHOTO_PATH_STRING)
                .appendPath(imageId)
                .appendQueryParameter(PARAM_CLIENT_ID, BuildConfig.UNSPLASH_APP_ID)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Loads profile JSON
     *
     * @param url-
     * @return -
     * @throws IOException-
     */
    public static String getProfileDetailHtml(String url) throws IOException {
        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .addHeader("Accept-Version", "v1")
                .url(url)
                .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Loads Image JSON
     *
     * @param url-
     * @return -
     * @throws IOException-
     */
    public static String getPhotoDetailHtml(String url) throws IOException {
        OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .addHeader("Accept-Version", "v1")
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * EXTRACTS JSON FOM RETRIEVED
     *
     * @param url-
     * @param imageId-
     * @param isProfile-
     * @return -
     * @throws IOException-
     */
    public static String getExtractedJson(Context context, String url, String imageId, boolean isProfile) throws IOException {
        String unExtractedText;

        if (isProfile) {
            unExtractedText = getProfileDetailHtml(url);
        } else {
            unExtractedText = getPhotoDetailHtml(url);
            return unExtractedText;
        }

        int beginIndex, endIndex;

        try {
            if (isProfile) {
                beginIndex = unExtractedText.indexOf(ConstantValues.PROFILE_JSON_BEGIN_TEXT);
                endIndex = unExtractedText.indexOf(ConstantValues.PROFILE_JSON_END_TEXT, beginIndex);
                return unExtractedText.substring(beginIndex, endIndex).replace(ConstantValues.PROFILE_JSON_BEGIN_TEXT, "").trim();
            } else {
//                beginIndex = unExtractedText.indexOf(ConstantValues.IMAGE_JSON_BEGIN_TEXT);
//                endIndex = unExtractedText.indexOf(ConstantValues.IMAGE_JSON_END_TEXT, beginIndex);
                return unExtractedText;
            }
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url, Context context) throws IOException, NullPointerException {
        Response response = null;
        try {

//            Log.e("CONTEXT", url.toString());

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(true).followSslRedirects(true);

            OkHttpClient client = enableTls12OnPreLollipop(clientBuilder).build();

            Request.Builder builder = new Request.Builder().addHeader("Accept-Version", "v1").url(url);

            if (UtilityMethods.isUserLoggedIn(context)) {

                builder.header("Authorization", "Bearer " + UtilityMethods.getAccessToken(context));
            }

            Request request = builder.build();

            response = client.newCall(request).execute();

            if (response.header(RATE_LIMIT_REMAINING_HEADER_KEY) != null && response.header(RATE_LIMIT_REMAINING_HEADER_KEY).equals("0")) {
                return RATE_LIMIT_REMAINING_HEADER_KEY;
            }

            return response.body().string();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


    /**
     * This method returns the entire result from the HTTP response. (Access Token)
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getAuthorisationResponse(URL url) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection = enableTls12Support(urlConnection);
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getCurrentUserProfile(URL url, String token) throws IOException {
        Response response = null;
        try {
            OkHttpClient client = enableTls12OnPreLollipop(new OkHttpClient.Builder()).build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Version", "v1")
                    .header("Authorization", "Bearer " + token)
                    .build();

            response = client.newCall(request).execute();
            return response.body().string();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * This method returns the entire result from the HTTP response after the action is completed. e.g Like/Unlike action
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response after performing action e.g Like, Unlike etc.
     * @throws IOException Related to network and stream reading ERRORS
     */
    public static String getActionResponse(Context context, URL url, String method) throws IOException, NullPointerException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection = enableTls12Support(urlConnection);
        try {
            if (UtilityMethods.isUserLoggedIn(context)) {
                urlConnection.addRequestProperty("AUTHORIZATION", "Bearer " + UtilityMethods.getAccessToken(context));
            }

            urlConnection.setRequestMethod(method);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(25000);
            urlConnection.connect();

            if (urlConnection.getHeaderField(RATE_LIMIT_REMAINING_HEADER_KEY) != null && urlConnection.getHeaderField(RATE_LIMIT_REMAINING_HEADER_KEY).equals("0")) {
                return RATE_LIMIT_REMAINING_HEADER_KEY;
            }

            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                String some = scanner.next();
                return some;
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private static HttpsURLConnection enableTls12Support(HttpsURLConnection urlConnection) {
        if (Build.VERSION.SDK_INT < 28) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
//                    throw new IllegalStateException("Unexpected default trust managers:"
//                            + Arrays.toString(trustManagers));
                    return urlConnection;
                }
                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, new TrustManager[]{trustManager}, null);
                urlConnection.setSSLSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return urlConnection;
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < 22) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
//                    throw new IllegalStateException("Unexpected default trust managers:"
//                            + Arrays.toString(trustManagers));
                    return client;
                }

                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, new TrustManager[]{trustManager}, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
//                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }

}

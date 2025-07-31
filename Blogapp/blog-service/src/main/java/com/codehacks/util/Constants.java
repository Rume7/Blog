package com.codehacks.util;

/**
 * Constants class for API endpoints and other application constants.
 * This class centralizes all endpoint paths to ensure consistency and ease of maintenance.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // API Base Paths
    public static final String API_V1 = "/api/v1";
    
    // Main Resource Paths
    public static final String POSTS_PATH = API_V1 + "/posts";
    public static final String AUTH_PATH = API_V1 + "/auth";
    public static final String USERS_PATH = API_V1 + "/users";
    public static final String EMAIL_PATH = API_V1 + "/email";
    public static final String SUBSCRIPTIONS_PATH = API_V1 + "/subscriptions";
    public static final String COMMENTS_PATH = API_V1 + "/comments";
    public static final String IMAGES_PATH = API_V1 + "/images";
    
    // Auth-specific endpoints
    public static final String LOGIN_ENDPOINT = AUTH_PATH + "/login";
    public static final String REGISTER_ENDPOINT = AUTH_PATH + "/register";
    public static final String VERIFY_MAGIC_LINK_ENDPOINT = AUTH_PATH + "/verify-magic-link";
    
    // User-specific endpoints
    public static final String USER_BY_ID_ENDPOINT = USERS_PATH + "/{id}";
    public static final String ALL_USERS_ENDPOINT = USERS_PATH;
    
    // Post-specific endpoints
    public static final String POST_BY_ID_ENDPOINT = POSTS_PATH + "/{id}";
    public static final String ALL_POSTS_ENDPOINT = POSTS_PATH;
    public static final String CREATE_POST_ENDPOINT = POSTS_PATH;
    public static final String UPDATE_POST_ENDPOINT = POSTS_PATH + "/{id}";
    public static final String DELETE_POST_ENDPOINT = POSTS_PATH + "/{id}";
    public static final String CLAP_POST_ENDPOINT = POSTS_PATH + "/{id}/clap";
    
    // Email-specific endpoints
    public static final String SEND_MAGIC_LINK_ENDPOINT = EMAIL_PATH + "/magic-link";
    public static final String VALIDATE_MAGIC_LINK_ENDPOINT = EMAIL_PATH + "/magic-link/validate/{token}";
    public static final String GET_EMAIL_FROM_TOKEN_ENDPOINT = EMAIL_PATH + "/magic-link/email/{token}";
    
    // Subscription-specific endpoints
    public static final String SUBSCRIBE_ENDPOINT = SUBSCRIPTIONS_PATH;
    public static final String VERIFY_SUBSCRIPTION_ENDPOINT = SUBSCRIPTIONS_PATH + "/verify/{token}";
    public static final String UNSUBSCRIBE_ENDPOINT = SUBSCRIPTIONS_PATH + "/{token}";
    public static final String UPDATE_PREFERENCES_ENDPOINT = SUBSCRIPTIONS_PATH + "/{token}/preferences";
    public static final String GET_SUBSCRIPTION_BY_EMAIL_ENDPOINT = SUBSCRIPTIONS_PATH + "/email/{email}";
    public static final String GET_SUBSCRIPTION_BY_TOKEN_ENDPOINT = SUBSCRIPTIONS_PATH + "/{token}";
    public static final String GET_ALL_SUBSCRIPTIONS_ENDPOINT = SUBSCRIPTIONS_PATH;
    public static final String GET_SUBSCRIPTION_STATISTICS_ENDPOINT = SUBSCRIPTIONS_PATH + "/statistics";
    
    // Comment-specific endpoints
    public static final String CREATE_COMMENT_ENDPOINT = COMMENTS_PATH;
    public static final String GET_COMMENT_BY_ID_ENDPOINT = COMMENTS_PATH + "/{id}";
    public static final String GET_COMMENTS_BY_POST_ENDPOINT = COMMENTS_PATH + "/post/{postId}";
    public static final String GET_ALL_COMMENTS_BY_POST_ENDPOINT = COMMENTS_PATH + "/post/{postId}/all";
    public static final String GET_COMMENTS_BY_POST_PAGE_ENDPOINT = COMMENTS_PATH + "/post/{postId}/page";
    public static final String GET_COMMENT_REPLIES_ENDPOINT = COMMENTS_PATH + "/{id}/replies";
    public static final String GET_COMMENTS_BY_USER_ENDPOINT = COMMENTS_PATH + "/user/{userId}";
    public static final String GET_COMMENTS_BY_STATUS_ENDPOINT = COMMENTS_PATH + "/status/{status}";
    public static final String GET_PENDING_COMMENTS_ENDPOINT = COMMENTS_PATH + "/pending";
    public static final String UPDATE_COMMENT_ENDPOINT = COMMENTS_PATH + "/{id}";
    public static final String DELETE_COMMENT_ENDPOINT = COMMENTS_PATH + "/{id}";
    public static final String MODERATE_COMMENT_ENDPOINT = COMMENTS_PATH + "/{id}/moderate";
    public static final String GET_COMMENT_STATISTICS_ENDPOINT = COMMENTS_PATH + "/statistics";
    public static final String GET_RECENT_COMMENTS_ENDPOINT = COMMENTS_PATH + "/recent";
    public static final String SEARCH_COMMENTS_ENDPOINT = COMMENTS_PATH + "/search";
    public static final String CHECK_USER_COMMENTED_ENDPOINT = COMMENTS_PATH + "/check/{postId}";
    public static final String GET_USER_RECENT_COMMENT_ENDPOINT = COMMENTS_PATH + "/user/{userId}/recent";
    public static final String COUNT_COMMENTS_BY_STATUS_ENDPOINT = COMMENTS_PATH + "/count/status/{status}";
    public static final String COUNT_COMMENTS_BY_POST_ENDPOINT = COMMENTS_PATH + "/count/post/{postId}";
    
    // Image-specific endpoints
    public static final String UPLOAD_IMAGE_ENDPOINT = IMAGES_PATH + "/upload";
    public static final String UPLOAD_PROFILE_PICTURE_ENDPOINT = IMAGES_PATH + "/profile-picture";
    public static final String GET_IMAGE_BY_ID_ENDPOINT = IMAGES_PATH + "/{id}"; // Public for featured images only
    public static final String GET_IMAGE_FILE_ENDPOINT = IMAGES_PATH + "/{id}/file"; // Public for featured images only
    public static final String GET_USER_PROFILE_PICTURE_ENDPOINT = IMAGES_PATH + "/profile/{userId}"; // Authenticated users only
    public static final String GET_IMAGES_BY_TYPE_ENDPOINT = IMAGES_PATH + "/type/{imageType}";
    public static final String GET_ALL_IMAGES_ENDPOINT = IMAGES_PATH;
    public static final String GET_USER_IMAGE_STATS_ENDPOINT = IMAGES_PATH + "/user/{uploaderId}/stats";
    public static final String GET_IMAGE_TYPE_STATS_ENDPOINT = IMAGES_PATH + "/type/{imageType}/stats";
    
    // HTTP Methods (for reference)
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String PATCH = "PATCH";
    
    // Common HTTP Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_HEADER = "Accept";
    
    // Content Types
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    
    // Pagination Constants
    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "size";
    public static final String SORT_PARAM = "sort";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Security Constants
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLE_PREFIX = "ROLE_";
    
    // Cache Names
    public static final String USERS_CACHE = "users";
    public static final String POSTS_CACHE = "posts";
    public static final String CLAPS_CACHE = "claps";
    public static final String AUTH_CACHE = "auth";
    public static final String COMMENTS_CACHE = "comments";
    public static final String IMAGES_CACHE = "images";
} 
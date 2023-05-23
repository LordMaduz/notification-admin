package com.admin.notification.constant;

public final class Constant {

    public static final String EVENT_END_POINT = "/events";
    public static final String PAYLOAD_END_POINT = "/{eventId}/channel/{channelId}/payload";
    public static final String PAYLOAD_BY_TEMPLATE_ID_VERSION_ID = "/{eventId}/channel/{channelId}/payload/{templateId}/{versionId}";
    public static final String TEMPLATE_RENDER_END_POINT = "/{eventId}/channel/{channelId}/payload/render/{templateId}/{versionId}";
    public static final String TEMPLATE_ACTIVATE_END_POINT = "/{eventId}/channel/{channelId}/payload/activate/{templateId}/{versionId}";


    public static final String USER_END_POINT = "/user";
    public static final String USER_SUBSCRIPTION_END_POINT = "/{id}/subscription";
    public static final String EVENT_EXIST_ERROR = "No Event with Given ID Exist";
    public static final String CHANNEL_EXIST_ERROR = "No Channel with Given ID Exist";
    public static final String PAYLOAD_EXIST_ERROR = "No Payload with Given TemplateId and VersionId  Exist";
    public static final String CLOUD_EVENTS_JSON = "application/cloudevents+json";
}

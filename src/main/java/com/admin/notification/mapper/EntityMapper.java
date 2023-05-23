package com.admin.notification.mapper;

import com.admin.notification.model.*;
import com.admin.notification.model.document.Event;
import com.admin.notification.model.document.Payload;
import com.admin.notification.model.document.User;
import com.admin.notification.model.enums.Channel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EntityMapper {

    public Version toVersion(final String name, final String id, final boolean status) {
        return Version.builder()
                .id(id)
                .versionName(name)
                .active(status)
                .build();
    }

    public Template toTemplate(final String templateName, final String templateId, final List<Version> version) {
        return Template.builder().
                templateName(templateName).
                templateId(templateId).
                versions(version).
                build();
    }

    public Payload toPayload(final String payload, final String eventId, final String templateId, final String versionId, final String type) {
        return Payload.builder()
                .body(payload)
                .eventId(eventId)
                .templateId(templateId)
                .versionId(versionId)
                .type(type)
                .build();
    }

    public Event toEvent(final String id, final String eventName, final List<ChannelMetaData> metaDataList) {
        return Event.builder()
                .id(id)
                .eventName(eventName)
                .channelMetaData(metaDataList)
                .build();
    }

    public ChannelMetaData toChannelMetaData(final String channelId, final String channelName, final Map<String, Object> configuration) {
        return ChannelMetaData.builder()
                .channelId(channelId)
                .channelType(Channel.valueOf(channelName))
                .configuration(configuration)
                .build();
    }

    public User toUser(final String name, final List<Subscription> subscription) {

        return User.builder()
                .name(name)
                .subscriptions(subscription)
                .build();
    }

    public Subscription toSubscription(final String eventId, final List<Channel> channelList) {
        return Subscription.builder()
                .eventId(eventId)
                .channels(channelList)
                .build();
    }
}

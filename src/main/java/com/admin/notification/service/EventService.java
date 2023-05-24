package com.admin.notification.service;

import com.admin.notification.exception.NotFoundException;
import com.admin.notification.exception.ProcessException;
import com.admin.notification.mapper.EntityMapper;
import com.admin.notification.mapper.EventMapper;
import com.admin.notification.mjml.MJMLRenderService;
import com.admin.notification.model.*;
import com.admin.notification.model.document.Event;
import com.admin.notification.model.document.Payload;
import com.admin.notification.repo.EventRepository;
import com.admin.notification.util.JSONUtil;
import com.admin.notification.util.Util;
import com.admin.notification.vo.EventVo;
import com.admin.notification.vo.TemplateVO;
import com.admin.notification.repo.PayloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.admin.notification.constant.Constant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final PayloadRepository payloadRepository;
    private final EntityMapper entityMapper;
    private final EventMapper eventMapper;
    private final MJMLRenderService mjmlRenderService;
    private final JSONUtil<TemplateVO> jsonUtil;

    public Mono<Event> createEvent(final EventVo eventVo) {

        try {
            final List<ChannelMetaData> channelMetaDataList = new ArrayList<>();

            eventVo.getChannelMetaData().forEach(channel -> {
                final ChannelMetaData channelMetaData = entityMapper.toChannelMetaData(Util.getUUID(), channel.getChannelType(), channel.getConfiguration());

                channelMetaDataList.add(channelMetaData);
            });

            final Event event = entityMapper.toEvent(eventVo.getEventName().replaceAll(" ", "_"),
                    eventVo.getEventName(),
                    channelMetaDataList);

            return eventRepository.save(event);

        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }


    public Mono<Payload> createTemplatePayloads(final String templateString, final String eventId, final String channelId, final FilePart file) {

        final List<Mono<?>> monoList = new ArrayList<>();
        final AtomicReference<Payload> payloadReference = new AtomicReference<>();
        final TemplateVO templateVO = jsonUtil.convertTo(templateString, TemplateVO.class);
        try {
            return eventRepository.findById(eventId).flatMap(event -> {
                final String templateId = Util.getUUID();
                final String versionId = Util.getUUID();
                final Supplier<Stream<ChannelMetaData>> supplier = () -> event.getChannelMetaData().stream().filter(channel -> channel.getChannelId().equals(channelId));
                if (supplier.get().findAny().isEmpty()) {
                    return Mono.error(
                            new NotFoundException(CHANNEL_EXIST_ERROR));
                } else {
                    supplier.get()
                            .forEach(channel -> {
                                final Version version = entityMapper.toVersion(templateVO.getVersion(), versionId, true);
                                final Template template = entityMapper.toTemplate(templateVO.getTemplateName(), templateId, List.of(version));
                                channel.setTemplate(template);
                            });
                    if (file != null) {
                        final Flux<String> fileContent = getFileContent(file);
                        fileContent.subscribe(body -> {
                            payloadReference.set(entityMapper.toPayload(body, eventId, templateId, versionId, templateVO.getPayloadType()));
                            monoList.add(eventRepository.save(event).then(payloadRepository.save(payloadReference.get())));
                        });
                    } else {
                        payloadReference.set(entityMapper.toPayload(templateVO.getPayload(), eventId, templateId, versionId, templateVO.getPayloadType()));
                        monoList.add(eventRepository.save(event).then(payloadRepository.save(payloadReference.get())));

                    }
                    return Mono.zip(monoList, c -> payloadReference.get());
                }
            });
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Event> updateEventData(final EventVo eventVo, final String id) {
        try {
            return eventRepository.findById(eventVo.getId()).flatMap(event -> {

                eventVo.getChannelMetaData().stream().filter(channelVo -> StringUtils.isNotEmpty(channelVo.getChannelId())).forEach(channelVo -> {
                    event.getChannelMetaData().stream().filter(channel -> channel.getChannelId().equals(channelVo.getChannelId())).findAny().ifPresent(channel -> {
                        BeanUtils.copyProperties(eventMapper.map(channelVo), channel, "template");
                    });
                });
                eventVo.getChannelMetaData().stream().filter(channelVo -> StringUtils.isEmpty(channelVo.getChannelId())).forEach(chanelVo -> {
                    ChannelMetaData channelMetaData = entityMapper.toChannelMetaData(Util.getUUID(), chanelVo.getChannelType(), chanelVo.getConfiguration());

                    event.getChannelMetaData().add(channelMetaData);
                });
                return eventRepository.save(event);
            });

        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Event> updateTemplatePayload(final FilePart filePart, final String templateString, final String eventId, final String channelId) {

        try {
            final TemplateVO templateVO = jsonUtil.convertTo(templateString, TemplateVO.class);
            return eventRepository.findById(eventId).flatMap(event ->
                    Mono.justOrEmpty(event.getChannelMetaData().stream().filter(channel ->
                            channel.getChannelId().equals(channelId)
                    ).findFirst()).flatMap(channel -> {
                        final List<Mono<?>> publishers = new ArrayList<>();
                        if (StringUtils.isNotEmpty(templateVO.getVersionId())) {
                            publishers.add(Mono.justOrEmpty(channel.getTemplate().getVersions().stream().filter(version ->
                                    version.getId().equals(templateVO.getVersionId())
                            ).findAny()).flatMap(version -> payloadRepository.findByEventIdAndTemplateIdAndVersionId(
                                    eventId, channel.getTemplate().getTemplateId(), version.getId()).flatMap(payload -> {

                                final List<Mono<?>> innerPublisher = new ArrayList<>();
                                if (filePart != null) {
                                    final Flux<String> fileContent = getFileContent(filePart);
                                    fileContent.subscribe(body -> {
                                        payload.setBody(body);
                                        innerPublisher.add(payloadRepository.save(payload));
                                    });
                                } else {
                                    payload.setBody(templateVO.getPayload());
                                    innerPublisher.add(payloadRepository.save(payload));
                                }
                                return Mono.zip(innerPublisher, p -> payload);
                            })));
                        } else {
                            String versionId = Util.getUUID();
                            Version version = entityMapper.toVersion(templateVO.getVersion(), versionId, false);
                            channel.getTemplate().getVersions().add(version);

                            Payload payload = entityMapper.toPayload(templateVO.getPayload(), eventId, channel.getTemplate().getTemplateId(), versionId, templateVO.getPayloadType());
                            publishers.add(payloadRepository.save(payload));
                        }
                        publishers.add(eventRepository.save(event));
                        return Mono.zip(publishers, e -> event);
                    }));
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Object> deleteEvent(final String id) {

        try {
            return eventRepository.findById(id).switchIfEmpty(Mono.error(
                    new NotFoundException(EVENT_EXIST_ERROR))).flatMap(event ->

                    payloadRepository.deleteByEventId(event.getEventName()).then(eventRepository.delete(event))
            );
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Payload> getPayloadByEventIdAndTemplateIdAndVersionId(final String eventId, final String templateId, final String versionId) {
        try {
            return payloadRepository.findByEventIdAndTemplateIdAndVersionId(eventId, templateId, versionId);
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Event> getEventByEventId(final String id) {
        return eventRepository.findById(id);
    }

    public Mono<Object> renderTemplate(final String eventId, final String templateId, final String versionId, final Map<String, Object> map) {

        try {
            return payloadRepository.findByEventIdAndTemplateIdAndVersionId(eventId, templateId, versionId).switchIfEmpty(Mono.error(
                    new NotFoundException(PAYLOAD_EXIST_ERROR))).flatMap(payload ->
                    Mono.just(mjmlRenderService.process(payload.getBody(), map))
            );
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    public Mono<Event> activateEventTemplate(final String eventId, final String templateId, final String versionId) {
        try {
            return eventRepository.findById(eventId).flatMap(event -> {
                        event.getChannelMetaData().stream().filter(channel -> channel.getTemplate().getTemplateId().equals(templateId)).forEach(channel -> {
                            channel.getTemplate().getVersions().stream().filter(Version::isActive).forEach(v -> v.setActive(false));
                            channel.getTemplate().getVersions().stream().filter(version -> version.getId().equals(versionId)).forEach(v -> v.setActive(true));
                        });
                        return eventRepository.save(event);
                    }
            );

        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }

    }

    public Flux<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    private Flux<String> getFileContent(final FilePart filePart) {
        return filePart.content().map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return new String(bytes, StandardCharsets.UTF_8);
        });

    }

}

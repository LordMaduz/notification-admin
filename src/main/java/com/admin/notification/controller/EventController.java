package com.admin.notification.controller;

import com.admin.notification.model.document.Event;
import com.admin.notification.model.document.Payload;
import com.admin.notification.service.EventService;
import com.admin.notification.vo.EventVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.admin.notification.constant.Constant.*;

@RestController
@RequestMapping(EVENT_END_POINT)
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping()
    public Mono<Event> createEvent(@RequestBody final Mono<EventVo> eventVo) {
        return eventVo.flatMap(this.eventService::createEvent);
    }

    @PostMapping(value = PAYLOAD_END_POINT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Payload> createTemplatePayload(@RequestPart(required = false) final FilePart file, @RequestPart final String templateString, @PathVariable final String eventId, @PathVariable final String channelId) {
        return this.eventService.createTemplatePayloads(templateString, eventId, channelId, file);
    }

    @PostMapping("/{id}")
    public Mono<Event> updateEventData(@RequestBody final Mono<EventVo> eventVo, @PathVariable final String id) {
        return eventVo.flatMap(event -> this.eventService.updateEventData(event, id));
    }

    @PutMapping(value = PAYLOAD_END_POINT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Event> updateTemplatePayload(@RequestPart(required = false) final FilePart file, @RequestPart final String templateString, @PathVariable final String eventId, @PathVariable final String channelId) {
        return this.eventService.updateTemplatePayload(file, templateString, eventId, channelId);
    }

    @GetMapping()
    public Flux<Event> getAllEvents() {
        return this.eventService.getAllEvents();
    }

    @DeleteMapping("/{id}")
    public Mono<Object> deleteEvent(@PathVariable final String id) {
        return this.eventService.deleteEvent(id);
    }

    @PostMapping(PAYLOAD_BY_TEMPLATE_ID_VERSION_ID)
    public Mono<Payload> getPayloadByEventIdAndTemplateIdAndVersionId(@PathVariable final String eventId, @PathVariable final String templateId, @PathVariable final String versionId) {
        return this.eventService.getPayloadByEventIdAndTemplateIdAndVersionId(eventId, templateId, versionId);
    }

    @GetMapping("/{id}")
    public Mono<Event> getEventById(@PathVariable final String id) {
        return this.eventService.getEventByEventId(id);
    }


    @PostMapping(TEMPLATE_RENDER_END_POINT)
    public Mono<Object> renderPayload(@RequestBody final Mono<Map<String, Object>> map, @PathVariable final String eventId, @PathVariable final String templateId, @PathVariable final String versionId) {
        return map.flatMap(m -> this.eventService.renderTemplate(eventId, templateId, versionId, m));
    }

    @PostMapping(TEMPLATE_ACTIVATE_END_POINT)
    public Mono<Event> activatePayload(@PathVariable final String eventId, @PathVariable final String templateId, @PathVariable final String versionId) {
        return this.eventService.activateEventTemplate(eventId, templateId, versionId);
    }
}

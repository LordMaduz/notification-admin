package com.admin.notification.exception.handler;

import com.admin.notification.exception.NotFoundException;
import com.admin.notification.exception.ProcessException;
import com.admin.notification.vo.ErrorResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
@Slf4j
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final Map<Class<? extends Exception>, HttpStatus> exceptionToStatusCode = Map.of(
            NotFoundException.class, HttpStatus.NOT_FOUND,
            ProcessException.class, HttpStatus.INTERNAL_SERVER_ERROR
    );
    private final HttpStatus defaultStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public GlobalErrorWebExceptionHandler(final ErrorAttributes errorAttributes,
                                          final ApplicationContext applicationContext,
                                          final ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());

    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(final ServerRequest serverRequest) {

        Throwable error = getError(serverRequest);
        HttpStatus httpStatus;
        if (error instanceof RuntimeException exception) {
            httpStatus = exceptionToStatusCode.getOrDefault(exception.getClass(), defaultStatus);
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }


        return ServerResponse
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ErrorResponseVO
                        .builder()
                        .code(httpStatus.value())
                        .message(error.getMessage())
                        .build())
                );
    }

}
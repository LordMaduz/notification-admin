package com.admin.notification.controller;

import com.admin.notification.model.document.User;
import com.admin.notification.service.UserService;
import com.admin.notification.vo.SubscriptionVo;
import com.admin.notification.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.admin.notification.constant.Constant.USER_END_POINT;
import static com.admin.notification.constant.Constant.USER_SUBSCRIPTION_END_POINT;

@RestController
@RequestMapping(USER_END_POINT)
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @PostMapping()
    public Mono<User> createUser(@RequestBody Mono<UserVo> userVo) {
        return userVo.flatMap(this.userService::createUser);
    }

    @PostMapping(USER_SUBSCRIPTION_END_POINT)
    public Mono<User> addSubscription(@RequestBody final Mono<SubscriptionVo> subscriptionVo, @PathVariable final String id) {
        return subscriptionVo.flatMap(subscription -> this.userService.addSubscription(subscription, id));
    }

    @GetMapping()
    public Flux<User> getUsers() {
        return this.userService.getUsers();
    }
}

package com.admin.notification.service;

import com.admin.notification.mapper.EntityMapper;
import com.admin.notification.model.Subscription;
import com.admin.notification.model.document.User;
import com.admin.notification.model.enums.Channel;
import com.admin.notification.repo.UserRepository;
import com.admin.notification.vo.SubscriptionVo;
import com.admin.notification.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public Mono<User> createUser(final UserVo userVo) {
        final List<Subscription> subscriptions = new ArrayList<>();
        if (userVo.getSubscription() != null) {
            List<String> channels = userVo.getSubscription().getChannels();
            List<Channel> channelList = new ArrayList<>();
            channels.forEach(channel -> {
                channelList.add(Channel.valueOf(channel));
            });
            subscriptions.add(entityMapper.toSubscription(userVo.getSubscription().getEventId(), channelList));
        }


        return userRepository.save(entityMapper.toUser(userVo.getName(), subscriptions));
    }

    public Mono<User> addSubscription(final SubscriptionVo subscriptionVo, final String id) {
        return userRepository.findById(id).flatMap(user -> {
            List<String> channels = subscriptionVo.getChannels();
            List<Channel> channelList = new ArrayList<>();
            channels.forEach(channel -> {
                channelList.add(Channel.valueOf(channel));
            });
            user.getSubscriptions().add(entityMapper.toSubscription(subscriptionVo.getEventId(), channelList));
            return userRepository.save(user);
        });
    }

    public Flux<User> getUsers() {
        return userRepository.findAll();
    }

}

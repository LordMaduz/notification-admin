package com.admin.notification.mapper;

import com.admin.notification.model.ChannelMetaData;
import com.admin.notification.vo.ChannelMetaDataVo;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = JsonNullableMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface EventMapper {


    ChannelMetaData map(ChannelMetaDataVo vo);

    List<ChannelMetaData> map(List<ChannelMetaDataVo> list);


    @InheritConfiguration
    void update(List<ChannelMetaDataVo> vo, @MappingTarget List<ChannelMetaData> channel);

    @InheritConfiguration
    void update(ChannelMetaDataVo vo, @MappingTarget ChannelMetaData channel);
}

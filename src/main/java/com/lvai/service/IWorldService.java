package com.lvai.service;

import com.lvai.entity.TravelInspiration;
import com.lvai.entity.SystemBroadcast;
import com.lvai.entity.Destination;
import com.lvai.entity.Topic;
import com.lvai.vo.DestinationVO;

import java.util.List;

public interface IWorldService {
    List<TravelInspiration> getInspirations(Integer month);
    List<TravelInspiration> getHotSelfdriveInspirations();
    com.lvai.vo.InspirationVO getInspirationDetail(Long id);
    List<SystemBroadcast> getBroadcasts();
    List<Destination> getHotDestinations();
    DestinationVO getDestinationDetail(Long id);
    List<Topic> getHotTopics();
}

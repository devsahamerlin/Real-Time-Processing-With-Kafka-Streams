package com.merlin.clickconsumer.service;

import com.merlin.clickconsumer.entity.ClickCount;

import java.util.List;
import java.util.Optional;

public interface ClickCountService {

    void updateClickCount(String key, Long count);
    Optional<ClickCount> getClickCount(String key);
    Optional<Long> getGlobalClickCount();
    List<ClickCount> getAllUserClickCounts();
    List<ClickCount> getTopUsers(int limit);
    Long getUserClickCount(String userId);
}

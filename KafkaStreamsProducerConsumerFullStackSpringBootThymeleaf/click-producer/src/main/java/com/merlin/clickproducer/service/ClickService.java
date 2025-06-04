package com.merlin.clickproducer.service;

import com.merlin.clickproducer.entity.UserClick;

import java.util.List;

public interface ClickService {

    void processClick(String userId, String sessionId);
    List<UserClick> getUserClickHistory(String userId);
    long getUserClickCount(String userId);
    long getTotalClickCount();
}
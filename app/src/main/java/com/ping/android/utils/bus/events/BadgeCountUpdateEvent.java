package com.ping.android.utils.bus.events;

import java.util.Map;

public class BadgeCountUpdateEvent {
    public Map<String, Integer> conversationBadgeMap;
    public final int missedCallCount;
    public final int messageCount;

    public BadgeCountUpdateEvent(Map<String, Integer> conversationBadgeMap, int messageCount, int missedCallCount) {
        this.conversationBadgeMap = conversationBadgeMap;
        this.messageCount = messageCount;
        this.missedCallCount = missedCallCount;
    }

    public int totalBadgeCount() {
        return missedCallCount + messageCount;
    }
}

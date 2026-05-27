package com.lvai.event;

import org.springframework.context.ApplicationEvent;

public class UserBehaviorEvent extends ApplicationEvent {
    private final String locationName;
    private final String type; // "FOOTPRINT" or "NOTE"

    public UserBehaviorEvent(Object source, String locationName, String type) {
        super(source);
        this.locationName = locationName;
        this.type = type;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getType() {
        return type;
    }
}

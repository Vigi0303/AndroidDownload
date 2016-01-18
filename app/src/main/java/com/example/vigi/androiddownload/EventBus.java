package com.example.vigi.androiddownload;

import com.squareup.otto.Bus;

/**
 * Created by Vigi on 2016/1/18.
 */
public class EventBus {
    private static EventBus ourInstance = new EventBus();

    public static EventBus getInstance() {
        return ourInstance;
    }

    private EventBus() {
        mBus = new Bus();
    }

    private Bus mBus;

    public void postEvent(Object event) {
        mBus.post(event);
    }

    public void register(Object o) {
        mBus.register(o);
    }

    public void unregister(Object o) {
        mBus.unregister(o);
    }
}

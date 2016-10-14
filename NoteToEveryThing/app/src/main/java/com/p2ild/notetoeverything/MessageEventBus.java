package com.p2ild.notetoeverything;

import com.p2ild.notetoeverything.service.AppService;

/**
 * Created by duypi on 2016-10-08.
 */
public class MessageEventBus {
    private final int type;

    public MessageEventBus(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}

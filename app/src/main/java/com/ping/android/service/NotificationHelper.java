package com.ping.android.service;

/**
 * Created by bzzz on 11/29/17.
 */

public class NotificationHelper {

    private NotificationHelper instance = new NotificationHelper();

    private NotificationHelper(){

    }

    public NotificationHelper getInstance(){
        return  instance;
    }


}

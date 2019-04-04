package com.poproject.app;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

public class EventHandlers {
    static EventHandler<Event> getScrollLock(){
        return new EventHandler<Event>(){
            @Override
            public void handle(Event event) {
                System.out.println("scroll captutred " + event.toString());
                //event.consume();
            }
        };


    }

}

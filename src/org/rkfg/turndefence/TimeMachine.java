package org.rkfg.turndefence;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

class TimeMachine { // time manager
    private static HashMap<Integer, Array<GameEvent>> Events = new HashMap<Integer, Array<GameEvent>>();

    public static void storeEvent(int time, GameEvent event, boolean insert) {
        if (!Events.containsKey(time))
            Events.put(time, new Array<GameEvent>(10));
        if (insert)
            Events.get(time).insert(0, event);
        else
            Events.get(time).add(event);
    }

    public static void storeEvent(int time, GameEvent event) {
        storeEvent(time, event, false);
    }

    public static Array<GameEvent> getEvents(int time) {
        return Events.get(time);
    }

    public static Actor respawn(int time, Class<? extends Actor> classFilter,
            int playerNumber) throws NotRespawnable {
        for (GameEvent event : getEvents(time)) {
            if (!event.bound && event.eventType == EventType.SPAWN
                    && event.unit.getClass() == classFilter
                    && event.unit.playerNumber == playerNumber) {
                event.bound = true;
                try {
                    return (Actor) event.unit.clone();
                } catch (CloneNotSupportedException e) {
                    throw new NotRespawnable();
                }
            }
        }
        throw new NotRespawnable();
    }
}

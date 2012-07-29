package org.rkfg.turndefence;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

class TimeMachine { // time manager
    private static HashMap<Integer, Array<GameEvent>> Events = new HashMap<Integer, Array<GameEvent>>();

    public static void storeEvent(int time, GameEvent event) {
        System.out.println("Storing event: " + event);
        if (!Events.containsKey(time))
            Events.put(time, new Array<GameEvent>());
        Array<GameEvent> moment = Events.get(time);
        if (event.eventType == EventType.TURN || moment.size == 0)
            moment.add(event);
        else
            moment.insert(moment.size - 1, event);
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
                    Gdx.app.debug("Respawning", String.format(
                            "Life: %d, speed: %d, playerNumber: %d",
                            event.unit.life, event.unit.speed,
                            event.unit.playerNumber));
                    return (Actor) event.unit.clone();
                } catch (CloneNotSupportedException e) {
                    throw new NotRespawnable();
                }
            }
        }
        throw new NotRespawnable();
    }
}

package org.rkfg.turndefence;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

class TimeMachine { // time manager
    private static HashMap<Integer, Array<GameEvent>> Events = new HashMap<Integer, Array<GameEvent>>();

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

    public static void timeTravel(int time) {
        if (time > TurnDefence.PresentPlayTime) // travelling to the future
                                                // isn't supported
            // yet :[
            return;

        int tempTurn = TurnDefence.Turn;
        Gdx.app.debug("Travel", String.format("Going to time: %d", time));
        TurnDefence.init();
        TurnDefence.TurnProcess = 1;
        Array<GameEvent> events;
        while (TurnDefence.PlayTime <= time) {
            events = TimeMachine.getEvents(TurnDefence.PlayTime);
            if (events != null) { // something to replay
                for (GameEvent event : events) {
                    event.bound = false;
                    switch (event.eventType) {
                    case BUILD:
                        try {
                            Gdx.app.log("Travel", "Score before: "
                                    + TurnDefence.Score[TurnDefence.Turn]);
                            Gdx.app.debug("Travel", String.format(
                                    "Creating the building @ %f, %f",
                                    event.building.x, event.building.y));
                            Building replayBuilding = (Building) event.building
                                    .clone(false);
                            if (replayBuilding != null) {
                                TurnDefence.BuildingsGroup
                                        .addActor(replayBuilding);
                                replayBuilding.mActive = true;
                            }
                            Gdx.app.log("Travel", "Score now: "
                                    + TurnDefence.Score[TurnDefence.Turn]);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SELL:
                        Gdx.app.debug("Travel", String.format(
                                "Selling the building @ %f, %f for %d",
                                event.building.x, event.building.y,
                                event.number));
                        for (Actor actor : TurnDefence.BuildingsGroup
                                .getActors()) {
                            if (event.building.x == actor.x
                                    && event.building.y == actor.y
                                    && event.building.getClass() == actor
                                            .getClass()) {
                                Gdx.app.debug("Travel",
                                        "Found the building to sell");
                                TurnDefence.Stage.removeActor(actor);
                                TurnDefence.changeScore(event.number);
                                break;
                            }
                        }
                        break;
                    case TURN:
                        TurnDefence.Turn = event.number;
                        TurnDefence.PlayerUpkeep = 0;
                        break;
                    }
                }
            }
            if (TurnDefence.PlayTime < time) {
                TurnDefence.Stage.act(TurnDefence.GAMESTEP);
                TurnDefence.PlayTime += 1;
            } else {
                break;
            }
            Gdx.app.debug(
                    "Travel",
                    String.format(
                            "Runtime: %f, TurnProcess: %d, PlayTime: %d, PresentPlayTime: %d",
                            TurnDefence.Runtime, TurnDefence.TurnProcess,
                            TurnDefence.PlayTime, TurnDefence.PresentPlayTime));
        }
        TurnDefence.TurnProcess = 0;
        TurnDefence.Turn = tempTurn;
        TurnDefence.changeScore(0);
        TurnDefence.changeUpkeep(0);
    }

}

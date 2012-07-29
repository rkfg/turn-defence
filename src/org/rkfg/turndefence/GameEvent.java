package org.rkfg.turndefence;

public class GameEvent {
    public EventType eventType;
    public Building building;
    public Unit unit;
    public int number;
    public boolean bound; // is the event bound to a cause

    private GameEvent(EventType eventType) {
        this.eventType = eventType;
    }

    GameEvent(EventType eventType, int number) {
        this(eventType);
        this.number = number;
    }

    public GameEvent(EventType eventType, Building building) {
        this(eventType);
        this.building = building;
    }

    public GameEvent(EventType eventType, Unit unit) {
        this(eventType);
        this.unit = unit;
    }

    @Override
    public String toString() {
        return String
                .format("{eventType: %s, building: %s, unit: %s, number: %s, bound: %s}",
                        eventType, building, unit, number, bound);
    }
}

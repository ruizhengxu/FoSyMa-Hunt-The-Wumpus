package eu.su.mas.dedaleEtu.mas.knowledge.smart;

import eu.su.mas.dedale.env.Observation;

import java.io.Serializable;
import java.util.Objects;

public class Treasure implements Serializable {

    private int value;
    private Observation type;
    private int lastModifiedDate;
    private TreasureState state;

    public Treasure(Observation type, int value, int foundedDate, TreasureState state) {
        this.type = type;
        this.value = value;
        this.lastModifiedDate = foundedDate;
        this.state = state;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Observation getType() {
        return this.type;
    }

    public void setType(Observation type) {
        this.type = type;
    }

    public int getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(int lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return "Treasure{" +
                "value=" + value +
                ", type=" + type +
                ", lastModifiedDate=" + lastModifiedDate +
                ", state=" + state +
                '}';
    }

    public TreasureState getState() {
        return state;
    }

    public void setState(TreasureState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Treasure treasure = (Treasure) o;
        return value == treasure.value && type == treasure.type && state == treasure.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type, state);
    }
    //    @Override public boolean equals(Object other) {
//        boolean result = false;
//        if (other instanceof Treasure) {
//            Treasure o = (Treasure) other;
//            result = (this.getLocation() == o.getLocation() && this.getType().equals(o.getType()));
//        }
//        return result;
//    }

}

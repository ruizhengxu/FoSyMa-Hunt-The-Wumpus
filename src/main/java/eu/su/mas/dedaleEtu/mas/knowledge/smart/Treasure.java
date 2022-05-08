package eu.su.mas.dedaleEtu.mas.knowledge.smart;

import eu.su.mas.dedale.env.Observation;

import java.io.Serializable;
import java.util.Objects;

public class Treasure implements Serializable, Comparable<Treasure> {

    private String location;
    private Integer value;
    private Observation type;
    private Integer strength;
    private int lastModifiedDate;
    private TreasureState state;

    public Treasure(String location, Observation type, int value, int foundedDate, TreasureState state) {
        this.location = location;
        this.type = type;
        this.value = value;
        this.lastModifiedDate = foundedDate;
        this.state = state;
    }

    public Treasure(Integer strength) {
        this.strength = strength;
    }

    public String getLocation() {return this.location;}

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Observation getType() {
        return this.type;
    }

    public void setType(Observation type) {
        this.type = type;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public int getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(int lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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
        return location == treasure.location && value == treasure.value && type == treasure.type && state == treasure.state;
    }

    @Override
    public String toString() {
        return "Treasure{" +
                "location='" + location + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", strength=" + strength +
                ", lastModifiedDate=" + lastModifiedDate +
                ", state=" + state +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, value, type, state);
    }

    @Override
    public int compareTo(Treasure t) {
        return -(this.getValue().compareTo(t.getValue())); // Order by decreasing
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

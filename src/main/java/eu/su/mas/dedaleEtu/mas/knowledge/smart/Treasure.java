package eu.su.mas.dedaleEtu.mas.knowledge.smart;

import eu.su.mas.dedale.env.Observation;

import java.io.Serializable;

public class Treasure implements Serializable {

    private int value;
    private Observation type;
    private int foundedDate;

    public Treasure(Observation type, int value, int foundedDate) {
        this.type = type;
        this.value = value;
        this.foundedDate = foundedDate;
    }

    public int getValue() {
        return this.value;
    }

    public Observation getType() {
        return this.type;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setType(Observation type) {
        this.type = type;
    }

    public int getFoundedDate() { return this.foundedDate; }

    @Override
    public String toString() {
        return "Treasure{" +
                "value=" + value +
                ", type=" + type +
                ", foundedDate=" + foundedDate +
                '}';
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

package eu.su.mas.dedaleEtu.mas.knowledge.smart;

import java.io.Serializable;
import java.util.Objects;

public class AgentInfo implements Serializable {

    private String name;
    private int id;
    private Integer goldValue;
    private Integer diamondValue;
    private Integer goldCapacity;
    private Integer diamondCapacity;
    private int lastModifiedDate;

    public AgentInfo(String name, int id, int goldValue, int diamondValue, int goldCapacity, int diamondCapacity, int lastModifiedDate) {
        this.name = name;
        this.id = id;
        this.goldValue = goldValue;
        this.diamondValue = diamondValue;
        this.goldCapacity = goldCapacity;
        this.diamondCapacity = diamondCapacity;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getGoldValue() {
        return goldValue;
    }

    public void setGoldValue(Integer goldValue) {
        this.goldValue = goldValue;
    }

    public Integer getDiamondValue() {
        return diamondValue;
    }

    public void setDiamondValue(Integer diamondValue) {
        this.diamondValue = diamondValue;
    }

    public Integer getGoldCapacity() {
        return goldCapacity;
    }

    public void setGoldCapacity(Integer goldCapacity) {
        this.goldCapacity = goldCapacity;
    }

    public Integer getDiamondCapacity() {
        return diamondCapacity;
    }

    public void setDiamondCapacity(Integer diamondCapacity) {
        this.diamondCapacity = diamondCapacity;
    }

    public int getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(int lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentInfo agentInfo = (AgentInfo) o;
        return id == agentInfo.id && goldValue == agentInfo.goldValue && diamondValue == agentInfo.diamondValue && goldCapacity == agentInfo.goldCapacity && diamondCapacity == agentInfo.diamondCapacity && Objects.equals(name, agentInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, goldValue, diamondValue, goldCapacity, diamondCapacity);
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", goldValue=" + goldValue +
                ", diamondValue=" + diamondValue +
                ", goldCapacity=" + goldCapacity +
                ", diamondCapacity=" + diamondCapacity +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}

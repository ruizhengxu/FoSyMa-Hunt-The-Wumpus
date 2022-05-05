package eu.su.mas.dedaleEtu.mas.agents.smart;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.smart.*;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class ExploreFSMAgent extends AbstractDedaleAgent {

    private static final long serialVersionUID = -7969469610244668140L;
    public MapRepresentation myMap;
    private Observation treasureType;
    private AgentState agentState;
    private boolean treasureMapUpdated;
    private boolean agentInfoUpdated;
    private HashMap<String, Treasure> treasuresMap = new HashMap<>();
    private HashMap<String, AgentInfo> agentInfo = new HashMap<>();
    private int totalStep;

    // State names
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";

    private List<String> past_position = new ArrayList<String>();

    private Set<ArrayList<String>> busyNeighbors = new HashSet<ArrayList<String>>();

    // useless ?
    private ArrayList<String> toForget = new ArrayList<String>();

    private String nextMove = null;

    private Integer time = 100;
    private Integer nbAgent;

    protected void setup() {
        super.setup();

        final Object[] args = getArguments();
        List<String> receivers = this.getReceivers(args);
        this.nbAgent = receivers.size();
        this.treasureType = Observation.ANY_TREASURE;
        this.agentState = AgentState.EXPLORE;
        this.totalStep = 0;
        List<Couple<Observation, Integer>> backpackCapacity = this.getBackPackFreeSpace(); // [<Gold, x>, <Diamond, y>]
        AgentInfo info = new AgentInfo(this.getLocalName(), receivers.indexOf(this.getLocalName()), 0, 0, backpackCapacity.get(0).getRight(), backpackCapacity.get(1).getRight(), this.totalStep);
        this.agentInfo.put(this.getLocalName(), info);
        this.treasureMapUpdated = true;
        this.agentInfoUpdated = true;

        FSMBehaviour fsm = new FSMBehaviour(this);
        fsm.registerFirstState(new PingNShareBehaviour(this, receivers), A);
        fsm.registerState(new MoveBehaviour(this), B);
        fsm.registerState(new InterBlockedBehaviour(this, receivers), C);
        fsm.registerDefaultTransition(A, B);
        fsm.registerDefaultTransition(B, A);
        // after blocking we transit to the Move Behaviour
        fsm.registerDefaultTransition(C, B);

        fsm.registerTransition(B, C, 1);

        List<Behaviour> lb = new ArrayList<Behaviour>();
        lb.add(fsm);
        addBehaviour(new startMyBehaviours(this, lb));

        System.out.println("the  agent " + this.getLocalName() + " is started");
    }

    private List<String> getReceivers(Object[] args) {

        List<String> list_agentNames = new ArrayList<String>();

        if (args.length == 0) {
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        } else {
            int i = 2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next
            // release.
            while (i < args.length) {
                list_agentNames.add((String) args[i]);
                i++;
            }
        }
        return list_agentNames;
    }

    public Observation getTreasureType() {
        return this.treasureType;
    }

    public void setTreasureType(Observation treasureType) {
        this.treasureType = treasureType;
    }

    public List<String> getPastPosition() {
        return this.past_position;
    }

    public void addPastPosition(String pastPos) {
        this.past_position.add(pastPos);
    }

    public Set<ArrayList<String>> getBusyNeighbors() {
        return this.busyNeighbors;
    }

    /* 0 name of the agent, 1 his position */
    public void addBusyNeighbors(ACLMessage cur) {
//        System.out.println(cur.getSender().getName());
        ArrayList<String> l = new ArrayList<String>();
        l.add(cur.getSender().getName());
        l.add(cur.getContent());
        this.busyNeighbors.add(l);
    }

    public void clearBusyNeighbors() {
        this.busyNeighbors.clear();
    }

    public String getNextMove() {
        return this.nextMove;
    }

    public void setNextMove(String s) {
        this.nextMove = s;
    }

    public ArrayList<String> getToForget() {
        return this.toForget;
    }

    public void addToForget(String s) {
        this.toForget.add(s);
    }

    public Integer getTime() {
        return this.time;
    }

    public Integer getNbAgent() {
        return this.nbAgent;
    }

    public boolean isBlocked() {
        if (past_position.size() >= 5) {
            for (int i = past_position.size() - 5; i < past_position.size() - 1; ++i) {
                if (past_position.get(i) == past_position.get(i + 1)) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void increaseStep() {
        this.totalStep += 1;
    }

    public int getCurrentStep() {
        return this.totalStep;
    }

    public AgentState getCurrentAgentState() {
        return this.agentState;
    }

    public void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }

    public boolean isTreasureMapUpdated() {
        return this.treasureMapUpdated;
    }

    public HashMap<String, Treasure> getTreasuresMap() {
        return this.treasuresMap;
    }

    public void addTreasure(String location, Treasure treasure) {
        if (this.treasuresMap.containsKey(location)) {
            Treasure t = this.treasuresMap.get(location);
            if (treasure.getLastModifiedDate() > t.getLastModifiedDate()) { // If new treasure is founded later
                this.treasuresMap.put(location, treasure);
                this.treasureMapUpdated = true;
            }
        } else {
            this.treasuresMap.put(location, treasure);
            this.treasureMapUpdated = true;
        }
    }

    public void pickedTreasure(String location, int pickedValue) {
        Treasure t = this.treasuresMap.get(location);
        if (pickedValue == t.getValue()) {
            t.setValue(0);
            t.setState(TreasureState.PICKED);
        } else {
            t.setValue(t.getValue()-pickedValue);
        }
        t.setLastModifiedDate(this.totalStep);
        this.treasuresMap.put(location, t);
        this.treasureMapUpdated = true;
        this.updateMyInfo(t.getType(), pickedValue);
    }

    public void missTreasure(String location) {
        Treasure t = this.treasuresMap.get(location);
        t.setState(TreasureState.MISSING);
        t.setLastModifiedDate(this.totalStep);
        this.treasuresMap.put(location, t);
        this.treasureMapUpdated = true;
    }

    public void mergeTreasuresMap(HashMap<String, Treasure> tm) {
        if (! this.treasuresMap.equals(tm)) {
            HashMap<String, Treasure> tmp = (HashMap<String, Treasure>) this.treasuresMap.clone();
            System.out.println("before merge :" + this.treasuresMap + " U " + tm);
            for (Map.Entry<String, Treasure> entry : tm.entrySet()) {
                // If it is a new treasure
                if (!this.treasuresMap.containsKey(entry.getKey())) {
                    this.treasuresMap.put(entry.getKey(), entry.getValue());
                } else {
                    Treasure t = this.treasuresMap.get(entry.getKey());
                    if (! t.getState().equals(entry.getValue().getState())) {
                        if (entry.getValue().getState().equals(TreasureState.PICKED)) {
                            this.treasuresMap.put(entry.getKey(), entry.getValue());
                        }
                    } else if (entry.getValue().getLastModifiedDate() > t.getLastModifiedDate()) {
                        this.treasuresMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            System.out.println("after merge :" + this.treasuresMap);
            this.treasureMapUpdated = this.treasuresMap.equals(tmp);
        }
    }

    public HashMap<String, AgentInfo> getAgentInfo() {
        return this.agentInfo;
    }

    private void updateMyInfo(Observation type, int value) {
        AgentInfo me = this.agentInfo.get(this.getLocalName());
        if (type.equals(Observation.GOLD)) {
            me.setGoldValue(me.getGoldValue()+value);
        } else if (type.equals(Observation.DIAMOND)) {
            me.setDiamondValue(me.getDiamondValue()+value);
        }
        this.agentInfo.put(this.getLocalName(), me);
        this.agentInfoUpdated = true;
    }

    public void mergeAgentInfo(HashMap<String, AgentInfo> infos) {
        if (! this.agentInfo.equals(infos)) {
            HashMap<String, AgentInfo> tmp = (HashMap<String, AgentInfo>) this.agentInfo.clone();
            System.out.println("before merge :" + this.agentInfo + " U " + infos);
            for (Map.Entry<String, AgentInfo> entry : infos.entrySet()) {
                if (!this.agentInfo.containsKey(entry.getKey())) {
                    this.agentInfo.put(entry.getKey(), entry.getValue());
                } else {
                    if (!entry.getKey().equals(this.getLocalName())) {
                        AgentInfo info = this.agentInfo.get(entry.getKey());
                        if (entry.getValue().getLastModifiedDate() > info.getLastModifiedDate()) {
                            this.agentInfo.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            System.out.println("after merge :" + this.agentInfo);
            this.agentInfoUpdated = this.agentInfo.equals(tmp);
        }
    }

    public boolean isAgentInfoUpdated() {
        return this.agentInfoUpdated;
    }
}
package eu.su.mas.dedaleEtu.mas.agents.smart;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.smart.*;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.AgentState;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.Treasure;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class ExploreFSMAgent extends AbstractDedaleAgent {

    private static final long serialVersionUID = -7969469610244668140L;
    public MapRepresentation myMap;
    private Observation treasureType;
    private AgentState agentState;
    private HashMap<String, Treasure> treasuresMap = new HashMap<>();
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

        this.treasureType = Observation.ANY_TREASURE;
        this.agentState = AgentState.EXPLORE;
        this.totalStep = 0;

        final Object[] args = getArguments();
        List<String> receivers = this.getReceivers(args);
        this.nbAgent = receivers.size();

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

    public HashMap<String, Treasure> getTreasuresMap() {
        return this.treasuresMap;
    }

    public void addTreasure(String location, Treasure treasure) {
        if (this.treasuresMap.containsKey(location)) {
            Treasure t = this.treasuresMap.get(location);
            if (treasure.getFoundedDate() > t.getFoundedDate()) { // If new treasure is founded later
                this.treasuresMap.put(location, treasure);
            }
        } else {
            this.treasuresMap.put(location, treasure);
        }
    }

    public void mergeTreasuresMap(HashMap<String, Treasure> tm) {
        System.out.println("before merge :" + this.treasuresMap + "\n" + tm);
        for (Map.Entry<String, Treasure> entry: tm.entrySet()) {
            if (! this.treasuresMap.containsKey(entry.getKey())) {
                this.treasuresMap.put(entry.getKey(), entry.getValue());
            } else {
                Treasure t = this.treasuresMap.get(entry.getKey());
                if (entry.getValue().getFoundedDate() > t.getFoundedDate()) {
                    this.treasuresMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        System.out.println("after merge :" + this.treasuresMap);
    }
}
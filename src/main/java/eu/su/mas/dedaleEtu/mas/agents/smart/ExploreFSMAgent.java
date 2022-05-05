package eu.su.mas.dedaleEtu.mas.agents.smart;

import dataStructures.serializableGraph.SerializableSimpleGraph;
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
    private boolean mapRepresentationUpdated;
    private boolean treasureMapUpdated;
    private boolean agentInfoUpdated;
    private int lastMapUpdateDate;
    private int lastTreasureUpdateDate;
    private int lastAgentInfoUpdateDate;
    private List<String> receivers;
    private HashMap<String, Treasure> treasuresMap = new HashMap<>();
    private HashMap<String, AgentInfo> agentInfo = new HashMap<>();
    private int totalStep;
    private int messageSend;

    // State names
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";

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
        this.receivers = this.getReceivers(args);
        this.nbAgent = receivers.size();
        this.treasureType = Observation.ANY_TREASURE;
        this.agentState = AgentState.EXPLORE;
        this.totalStep = 0;
        this.messageSend = 0;
        List<Couple<Observation, Integer>> backpackCapacity = this.getBackPackFreeSpace(); // [<Gold, x>, <Diamond, y>]
        AgentInfo info = new AgentInfo(this.getLocalName(), receivers.indexOf(this.getLocalName()), 0, 0, backpackCapacity.get(0).getRight(), backpackCapacity.get(1).getRight(), this.totalStep);
        this.agentInfo.put(this.getLocalName(), info);
        this.treasureMapUpdated = true;
        this.agentInfoUpdated = true;
        this.mapRepresentationUpdated = true;
        this.lastMapUpdateDate = 0;
        this.lastTreasureUpdateDate = 0;
        this.lastAgentInfoUpdateDate = 0;

        FSMBehaviour fsm = new FSMBehaviour(this);
        fsm.registerFirstState(new PingNShareBehaviour(this, receivers), A);
        fsm.registerState(new MoveBehaviour(this), B);
        fsm.registerState(new InterBlockedBehaviour(this, receivers), C);
        fsm.registerLastState(new CollectBehaviour(this), D);
        // TRANSITION
        fsm.registerDefaultTransition(A, B);
        fsm.registerDefaultTransition(B, A);
        // after blocking we transit to the Move Behaviour
        fsm.registerTransition(B, C, 1);
        fsm.registerDefaultTransition(C, B);
        // collect behaviour
//        fsm.registerTransition(B, D, 2);
//        fsm.registerDefaultTransition(D, A);
//        fsm.registerTransition(A, D, 1);

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

    public int getMessageSend() {
        return messageSend;
    }

    public void increaseMessageCount() {
        this.messageSend += 1;
    }

    public AgentState getCurrentAgentState() {
        return this.agentState;
    }

    public void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }

    public boolean switchToCollect() {
        System.out.printf("%s - AgentSize : %d, TotalStep : %d, lastMap : %d, lastTre : %d, lastAg : %d, openNodes : %d\n", this.getLocalName(), this.agentInfo.size(), this.totalStep, this.lastMapUpdateDate, this.lastTreasureUpdateDate, this.lastAgentInfoUpdateDate, this.myMap.getOpenNodes().size());
//        System.out.println((this.agentInfo.size() == this.nbAgent));
//        System.out.println((this.totalStep - this.lastMapUpdateDate > 20));
//        System.out.println((this.totalStep - this.lastTreasureUpdateDate > 25));
        if ((this.agentInfo.size() == this.nbAgent) && (this.totalStep - this.lastMapUpdateDate > 20) &&
                (this.totalStep - this.lastTreasureUpdateDate > 25)) {
            this.setAgentState(AgentState.COLLECT);
            System.out.println("############################################################");
            System.out.println(this.getLocalName() + " passes to COLLECT");
            System.out.println(this.getLocalName() + this.treasuresMap);
            System.out.println(this.getLocalName() + this.agentInfo);
            System.out.println("############################################################");
            this.generateStrategy();
            return true;
        }
        return false;
    }

    public void mergeMap(SerializableSimpleGraph<String, MapRepresentation.MapAttribute> map) {
        List<String> openNodesBefore = this.myMap.getOpenNodes();
        List<String> closedNodesBefore = this.myMap.getClosedNodes();
        this.myMap.mergeMap(map);
        List<String> openNodesAfter = this.myMap.getOpenNodes();
        List<String> closedNodesAfter = this.myMap.getClosedNodes();

        this.mapRepresentationUpdated = !openNodesBefore.equals(openNodesAfter) || !closedNodesBefore.equals(closedNodesAfter);
        this.lastMapUpdateDate = this.mapRepresentationUpdated?this.totalStep:this.lastMapUpdateDate;
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
                this.lastTreasureUpdateDate = this.totalStep;
            }
        } else {
            this.treasuresMap.put(location, treasure);
            this.treasureMapUpdated = true;
            this.lastTreasureUpdateDate = this.totalStep;
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
        this.lastTreasureUpdateDate = this.totalStep;
        this.updateMyInfo(t.getType(), pickedValue);
    }

    public void missTreasure(String location) {
        Treasure t = this.treasuresMap.get(location);
        t.setState(TreasureState.MISSING);
        t.setLastModifiedDate(this.totalStep);
        this.treasuresMap.put(location, t);
        this.treasureMapUpdated = true;
        this.lastTreasureUpdateDate = this.totalStep;
    }

    public void mergeTreasuresMap(HashMap<String, Treasure> tm) {
        if (! this.treasuresMap.equals(tm)) {
            HashMap<String, Treasure> tmp = (HashMap<String, Treasure>) this.treasuresMap.clone();
//            System.out.println(this.getLocalName());
//            System.out.println("before merge :" + this.treasuresMap);
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
//            System.out.println("after merge :" + this.treasuresMap);
            this.treasureMapUpdated = !this.treasuresMap.equals(tmp);
            this.lastTreasureUpdateDate = this.treasureMapUpdated?this.totalStep:this.lastTreasureUpdateDate;
        }
    }

    public HashMap<String, AgentInfo> getAgentInfo() {
        return this.agentInfo;
    }

    private void updateMyInfo(Observation type, int value) {
        AgentInfo me = this.agentInfo.get(this.getLocalName());
        me.setLastModifiedDate(this.totalStep);
        if (type.equals(Observation.GOLD)) {
            me.setGoldValue(me.getGoldValue()+value);
        } else if (type.equals(Observation.DIAMOND)) {
            me.setDiamondValue(me.getDiamondValue()+value);
        }
        this.agentInfo.put(this.getLocalName(), me);
        this.agentInfoUpdated = true;
        this.lastAgentInfoUpdateDate = this.totalStep;
    }

    public void mergeAgentInfo(HashMap<String, AgentInfo> infos) {
        if (! this.agentInfo.equals(infos)) {
            HashMap<String, AgentInfo> tmp = (HashMap<String, AgentInfo>) this.agentInfo.clone();
            System.out.println(this.getLocalName());
            System.out.println("before merge :" + this.agentInfo);
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
            this.agentInfoUpdated = !this.agentInfo.equals(tmp);
            this.lastAgentInfoUpdateDate = this.agentInfoUpdated?this.totalStep:this.lastAgentInfoUpdateDate;
        }
    }

    public boolean isAgentInfoUpdated() {
        return this.agentInfoUpdated;
    }

    private HashMap<Integer, List<Treasure>> kMeans(List<Treasure> treasureList, List<String> agentList, int nbClass) {
        HashMap<Integer, List<Treasure>> target = new HashMap<>();
        System.out.println(treasureList);
        for (String name: agentList) {
            System.out.println(this.agentInfo.get(name));
        }
        return target;
    }

    private void generateStrategy() {
        int sumOfGold = 0;
        int sumOfDiamond = 0;
        List<String> goldAgentList = new ArrayList<>();
        List<Treasure> goldList = new ArrayList<>();
        List<String> diamondAgentList = new ArrayList<>();
        List<Treasure> diamondList = new ArrayList<>();

        for (Treasure t: this.treasuresMap.values()) {
            if (t.getState().equals(TreasureState.FOUND)) {
                if (t.getType().equals(Observation.GOLD)) {
                    goldList.add(t);
                    sumOfGold += t.getValue();
                } else {
                    diamondList.add(t);
                    sumOfDiamond += t.getValue();
                }
            }
        }
        Collections.sort(goldList);
        Collections.sort(diamondList);
        int objValue = (sumOfGold+sumOfDiamond)/this.nbAgent; // Objective value of each agent
        int nbRequiredForDiamond = Math.min((int)Math.ceil((double)sumOfDiamond/objValue), diamondList.size()); // Number of agent to collect diamond
        int nbRequiredForGold = Math.min(this.nbAgent-nbRequiredForDiamond, goldList.size()); // Number of agent to collect gold

        List<AgentInfo> agentList = new ArrayList<>();
        for (AgentInfo info: this.agentInfo.values()) {
            agentList.add(info);
        }

        while (nbRequiredForDiamond > 0 && nbRequiredForGold > 0) {
            AgentInfo ag;
            if (sumOfGold > sumOfDiamond) { // If sum of gold is higher
                // Find the agent who has the highest capacity of gold
                ag = getAgentWithHighestCapOfGold(agentList);
                goldAgentList.add(ag.getName());
                sumOfGold -= ag.getGoldCapacity();
                nbRequiredForGold -= 1;
            } else {
                ag = getAgentWithHighestCapOfDiamond(agentList);
                diamondAgentList.add(ag.getName());
                sumOfGold -= ag.getDiamondCapacity();
                nbRequiredForDiamond -= 1;
            }
            agentList.remove(ag);
        }

        HashMap<Integer, List<Treasure>> goldStrategy = this.kMeans(goldList, goldAgentList, nbRequiredForGold);
        HashMap<Integer, List<Treasure>> diamondStrategy = this.kMeans(diamondList, diamondAgentList, nbRequiredForGold);
        System.out.printf("%d %d %d %d %d\n", sumOfGold, sumOfDiamond, objValue, nbRequiredForGold, nbRequiredForDiamond);
    }

    private AgentInfo getAgentWithHighestCapOfGold(List<AgentInfo> agentList) {
        AgentInfo ag = null;
        for (AgentInfo ag_ : agentList) {
            if (ag == null || ag.getGoldCapacity() < ag_.getGoldCapacity()) {
                ag = ag_;
            }
        }
        return ag;
    }

    private AgentInfo getAgentWithHighestCapOfDiamond(List<AgentInfo> agentList) {
        AgentInfo ag = null;
        for (AgentInfo ag_ : agentList) {
            if (ag == null || ag.getDiamondCapacity() < ag_.getDiamondCapacity()) {
                ag = ag_;
            }
        }
        return ag;
    }
}
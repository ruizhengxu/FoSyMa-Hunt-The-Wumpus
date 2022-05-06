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
    private List<String> treasureToPick = new ArrayList<>();
    private int totalStep;
    private int messageSend;
    private Integer objValue;

    // State names
    private static final String PINGNSHARE = "A";
    private static final String EXPLO = "B";
    private static final String INTERBLOCK = "C";
    private static final String COLLECT = "D";
    private static final String FINISH = "E";

    private List<String> past_position = new ArrayList<String>();

    private Set<ArrayList<String>> busyNeighbors = new HashSet<ArrayList<String>>();

    // useless ?
    private ArrayList<String> toForget = new ArrayList<String>();

    private String nextMove = null;

    private Integer time = 100;
    private Integer nbAgent;
    private ArrayList<Integer> findedOnLastPass = new ArrayList<Integer>();

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
        fsm.registerFirstState(new PingNShareBehaviour(this, receivers), PINGNSHARE);
        fsm.registerState(new ExploreBehaviour(this), EXPLO);
        fsm.registerState(new InterBlockedBehaviour(this, receivers), INTERBLOCK);
        fsm.registerState(new CollectBehaviour(this), COLLECT);
        fsm.registerLastState(new FinishBehaviour(this), FINISH);
        // TRANSITION
        fsm.registerDefaultTransition(PINGNSHARE, EXPLO);
        fsm.registerTransition(PINGNSHARE, COLLECT, 1);
        fsm.registerDefaultTransition(EXPLO, PINGNSHARE);
        fsm.registerTransition(EXPLO, INTERBLOCK, 1);
        fsm.registerTransition(EXPLO, COLLECT, 2);
        fsm.registerDefaultTransition(INTERBLOCK, EXPLO);
        fsm.registerTransition(INTERBLOCK, COLLECT, 1);
        fsm.registerDefaultTransition(COLLECT, PINGNSHARE);
        fsm.registerTransition(COLLECT, INTERBLOCK, 1);
        fsm.registerTransition(COLLECT, FINISH, 2);

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
        if ( (this.agentInfo.size() == this.nbAgent) && (this.totalStep - this.lastTreasureUpdateDate > 20) && (this.myMap.getOpenNodes().size()<5) && ((this.totalStep - this.lastMapUpdateDate > 15) || (! this.myMap.hasOpenNode())) ) {
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
            if (!treasure.equals(t)) { // If new treasure is founded later
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
        System.out.println(this.treasuresMap);
        Treasure t = this.treasuresMap.get(location);
        if (pickedValue == t.getValue()) {
            t.setValue(0);
            t.setState(TreasureState.PICKED);
        } else {
            t.setValue(t.getValue()-pickedValue);
        }
        this.treasureToPick.remove(location);
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
//            System.out.println(this.getLocalName());
//            System.out.println("before merge :" + this.agentInfo);
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
//            System.out.println("after merge :" + this.agentInfo);
            this.agentInfoUpdated = !this.agentInfo.equals(tmp);
            this.lastAgentInfoUpdateDate = this.agentInfoUpdated?this.totalStep:this.lastAgentInfoUpdateDate;
        }
    }

    public boolean isAgentInfoUpdated() {
        return this.agentInfoUpdated;
    }

    private HashMap<String, List<Treasure>> kMeans(List<Treasure> treasureList, List<String> agentList) {
        HashMap<String, List<Treasure>> distribution = new HashMap<>();
        // Initialize each agent by distributing one treasure
        for (String name: agentList) {
            List<Treasure> l = new ArrayList<>();
            l.add(treasureList.remove(0));
            distribution.put(name, l);
        }

        while (treasureList.size() > 0) {
            // Find the agent who currently have the minimum value of treasure
            String name = this.getAgentWithMinValueOfTreasure(distribution);
            Treasure t = distribution.get(name).get(0);
            // Get the nearest treasure of the initial treasure t
            Treasure nearestTreasure = null;
            Integer distances = Integer.MAX_VALUE;
            for (Treasure t_: treasureList) {
                Integer currentDist = this.myMap.getShortestPath(t.getLocation(), t_.getLocation()).size();
                if (distances > currentDist) {
                    nearestTreasure = t_;
                    distances = currentDist;
                }
            }
            // Add the nearest treasure to this agent
            distribution.get(name).add(nearestTreasure);
            treasureList.remove(nearestTreasure);
        }

        return distribution;
    }

    private String getAgentWithMinValueOfTreasure(HashMap<String, List<Treasure>> distribution) {
        String name = null;
        Integer minSum = Integer.MAX_VALUE;
        for (Map.Entry<String, List<Treasure>> entry: distribution.entrySet()) {
            Integer currentSum = this.getSumOfValue(entry.getValue());
            if (minSum > currentSum) {
                name = entry.getKey();
                minSum = currentSum;
            }
        }
        return name;
    }

    private Integer getSumOfValue(List<Treasure> treasureList) {
        Integer sum = 0;
        for (Treasure t: treasureList) {
            sum += t.getValue();
        }
        return sum;
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
        this.objValue = (sumOfGold+sumOfDiamond)/this.nbAgent; // Objective value of each agent

        List<AgentInfo> agentList = new ArrayList<>();
        for (AgentInfo info: this.agentInfo.values()) {
            agentList.add(info);
        }

        System.out.printf("%d %d %d\n", sumOfGold, sumOfDiamond, this.objValue);

        while (agentList.size() > 0) {
            AgentInfo agG = getAgentWithHighestCapOfGold(agentList);
            AgentInfo agD = getAgentWithHighestCapOfDiamond(agentList);
            if (agG.getGoldCapacity() > agD.getDiamondCapacity()) {
                if (sumOfGold > 0 && Math.abs(sumOfGold-agG.getGoldCapacity())<Math.abs(sumOfDiamond-agG.getDiamondCapacity())) {
                    goldAgentList.add(agG.getName());
                    sumOfGold -= agG.getGoldCapacity();
                } else {
                    diamondAgentList.add(agG.getName());
                    sumOfGold -= agG.getDiamondCapacity();
                }
                agentList.remove(agG);
            } else {
                if (sumOfDiamond > 0 && Math.abs(sumOfDiamond-agD.getDiamondCapacity())<Math.abs(sumOfGold-agD.getGoldCapacity())) {
                    diamondAgentList.add(agD.getName());
                    sumOfGold -= agD.getDiamondCapacity();
                } else {
                    goldAgentList.add(agD.getName());
                    sumOfGold -= agD.getGoldCapacity();
                }
                agentList.remove(agD);
            }
        }

        HashMap<String, List<Treasure>> strategy = null;
        if (goldAgentList.contains(this.getLocalName())) {
            strategy = this.kMeans(goldList, goldAgentList);
        } else {
            strategy = this.kMeans(diamondList, diamondAgentList);
        }
        System.out.println(this.getLocalName() + " - My strategy is : " + strategy.get(this.getLocalName()));

        for (Treasure t: strategy.get(this.getLocalName())) {
            this.treasureToPick.add(t.getLocation());
        }
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

    public List<String> getTreasureToPick() {
        return this.treasureToPick;
    }

    public String findNearestTreasure(String myPosition) {
        if (this.treasureToPick.size() > 0) {
            String nearestTreasure = null;
            Integer distances = Integer.MAX_VALUE;
            for (String node : this.treasureToPick) {
                Integer currentDist = this.myMap.getShortestPath(myPosition, node).size();
                if (distances > currentDist) {
                    nearestTreasure = node;
                    distances = currentDist;
                }
            }
            return nearestTreasure;
        } else {
            return null;
        }
    }

    public void moveAfterBlocking() {
        try {
            this.doWait(((ExploreFSMAgent)this).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent) this).moveTo(((ExploreFSMAgent)this).getNextMove());
        try {
            this.doWait(((ExploreFSMAgent)this).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* Move randomly */
        List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this).observe();
        int index = new Random().nextInt(obs.size() - 1);
        String pos = obs.get(index).getLeft();
        ((AbstractDedaleAgent) this).moveTo(pos);

        ((ExploreFSMAgent)this).setNextMove(null);
    }

    public ArrayList<Integer> getFindedOnLastPass() {
        return this.findedOnLastPass;
    }

    public void addFindedOnLastPass(int i) {
        this.findedOnLastPass.add(i);
    }
}
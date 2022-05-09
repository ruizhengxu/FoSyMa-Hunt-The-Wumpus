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
    private Integer strength;
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
    private List<String> notMyTreasure = new ArrayList<>();
    private int totalStep;
    private int messageSend;
    private Integer objValue;
    private Integer passNb = 0;

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
    private List<Treasure> strategy;

    protected void setup() {
        super.setup();

        final Object[] args = getArguments();
        this.receivers = this.getReceivers(args);
        this.nbAgent = receivers.size();
        this.treasureType = Observation.ANY_TREASURE;
        this.agentState = AgentState.EXPLORE;
        for (Couple<Observation, Integer> c : this.getMyExpertise()) {
            if (c.getLeft().equals(Observation.STRENGH)) {
                this.strength = c.getRight();
            }
        }
        this.totalStep = 0;
        this.messageSend = 0;
        List<Couple<Observation, Integer>> backpackCapacity = this.getBackPackFreeSpace(); // [<Gold, x>, <Diamond, y>]
        AgentInfo info = new AgentInfo(this.getLocalName(), receivers.indexOf(this.getLocalName()), 0, 0, backpackCapacity.get(0).getRight(), backpackCapacity.get(1).getRight(), this.strength, this.totalStep);
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
        fsm.registerState(new GarbageCollectorBehaviour(this), FINISH);
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

        fsm.registerDefaultTransition(FINISH, FINISH);
        fsm.registerTransition(FINISH, INTERBLOCK, 1);
        fsm.registerTransition(FINISH, EXPLO, 2);
        fsm.registerTransition(INTERBLOCK, FINISH, 2);

        List<Behaviour> lb = new ArrayList<Behaviour>();
        lb.add(fsm);
        addBehaviour(new startMyBehaviours(this, lb));

        System.out.println("the agent " + this.getLocalName() + " is started with a strength of " + this.strength);
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
        if (((this.agentInfo.size() == this.nbAgent) && (this.totalStep - this.lastTreasureUpdateDate > 12) && (this.totalStep - this.lastMapUpdateDate > 12) && (this.myMap.getOpenNodes().size()<4)) || (! this.myMap.hasOpenNode())) {
//             (this.agentInfo.size() == this.nbAgent) && (this.totalStep - this.lastTreasureUpdateDate > 20) && (this.myMap.getOpenNodes().size()<5) && ((this.totalStep - this.lastMapUpdateDate > 15) || (! this.myMap.hasOpenNode()))
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

    public boolean isThereFoundedTreasure() {
        boolean res = false;
        for (Treasure t : this.treasuresMap.values()) {
            if (t.getState().equals(TreasureState.OPENED)) {
                res = true;
                break;
            }
        }
        return res;
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
        Treasure t = this.treasuresMap.get(location);
        t.setLastModifiedDate(this.totalStep);
        t.setValue(t.getValue()-pickedValue);
        if (t.getValue().equals(0)) {
            t.setState(TreasureState.PICKED);
            this.treasureToPick.remove(location);
        }
        this.treasuresMap.put(location, t);
        this.treasureMapUpdated = true;
        this.lastTreasureUpdateDate = this.totalStep;
        this.updateMyInfo(t.getType(), pickedValue);
    }

    public void missTreasure(String location) {
        Treasure t = this.treasuresMap.get(location);
        t.setState(TreasureState.MISSING);
        t.setLastModifiedDate(this.totalStep);
        if (this.treasureToPick.contains(location)) {this.treasureToPick.remove(location);}
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
                        if (!entry.getValue().getState().equals(TreasureState.OPENED)) {
                            this.treasuresMap.put(entry.getKey(), entry.getValue());
                        }
                    } else if (entry.getValue().getLastModifiedDate() > t.getLastModifiedDate()) {
                        this.treasuresMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
//            System.out.println("after merge (" + !this.treasuresMap.equals(tmp) + ") :" + this.treasuresMap);
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
//        this.agentInfoUpdated = true;
//        this.lastAgentInfoUpdateDate = this.totalStep;
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
        System.out.println("kmeans : " + treasureList + "\n" + agentList);
        for (String name: agentList) {
            List<Treasure> l = new ArrayList<>();
            l.add(treasureList.remove(0));
            distribution.put(name, l);
        }

        System.out.println(this.getLocalName() + " - Current distrib : " + distribution);
        System.out.println(this.getLocalName() + " - Current treasure list : " + treasureList);
//        while (this.existsCompatibleTreasure(treasureList, agentList)) {
        while(treasureList.size() > 0) {
            // Find the agent who currently have the minimum value of treasure
            String name = this.getAgentWithMinValueOfTreasure(distribution);
            Treasure t = distribution.get(name).get(0);
            // Get the nearest treasure of the initial treasure t
            Treasure nearestTreasure = null;
            Integer distances = Integer.MAX_VALUE;
//            System.out.println(this.getLocalName() + " - current min agent : " + name);
            for (Treasure t_: treasureList) {
                Integer currentDist = this.myMap.getShortestPath(t.getLocation(), t_.getLocation()).size();
                if (distances > currentDist) {
                    nearestTreasure = t_;
                    distances = currentDist;
                }
            }
            // Add the nearest treasure to this agent
            if (nearestTreasure != null) {
                distribution.get(name).add(nearestTreasure);
                treasureList.remove(nearestTreasure);
            }
        }

        return distribution;
    }

    private boolean existsCompatibleTreasure(List<Treasure> treasureList, List<String> agentList) {
        boolean exists = false;
//        System.out.println("calculate compatibility \n" + treasureList + "\n" + agentList);
        for (String agent: agentList) {
            for (Treasure t: treasureList) {
                if (t.getStrength() <= this.agentInfo.get(agent).getStrength())
                    exists = true;
                    break;
            }
            if (exists) {break;}
        }
        return exists;
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

    public void generateStrategy() {
        if (this.treasureType.equals(Observation.ANY_TREASURE)) {
            int sumOfGold = 0;
            int sumOfDiamond = 0;
            List<String> goldAgentList = new ArrayList<>();
            List<Treasure> goldList = new ArrayList<>();
            List<String> diamondAgentList = new ArrayList<>();
            List<Treasure> diamondList = new ArrayList<>();
            System.out.println(this.treasuresMap);
            for (Treasure t: this.treasuresMap.values()) {
                if (t.getState().equals(TreasureState.OPENED)) {
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
            int nbRequiredForDiamond = Math.min((int)Math.ceil((double)sumOfDiamond/this.objValue), diamondList.size()); // Number of agent to collect diamond
            int nbRequiredForGold = Math.min(this.nbAgent-nbRequiredForDiamond, goldList.size()); // Number of agent to collect gold

            List<AgentInfo> agentList = new ArrayList<>();
            for (AgentInfo info: this.agentInfo.values()) {
                agentList.add(info);
            }

            System.out.printf("%d %d %d %d %d\n", sumOfGold, sumOfDiamond, this.objValue, nbRequiredForGold, nbRequiredForDiamond);

//            for (AgentInfo agent: agentList) {
//                double ratio = agent.getGoldCapacity()/agent.getDiamondCapacity();
//                if (ratio < 0.25 && nbRequiredForDiamond > 0) {
//                    agentList.remove(agent);
//                    diamondAgentList.add(agent.getName());
//                    nbRequiredForDiamond -= 1;
//                }
//                if (ratio > 2 && nbRequiredForGold > 0) {
//                    agentList.remove(agent);
//                    goldAgentList.add(agent.getName());
//                    nbRequiredForGold -= 1;
//                }
//            }

            double stdG = calculateStdG(agentList);
            double stdD = calculateStdD(agentList);
            double confidenceGAt95 = (stdG/Math.sqrt(agentList.size()))*1.96;
            double confidenceDAt95 = (stdD/Math.sqrt(agentList.size()))*1.96;
            while (agentList.size() > 0) {
                AgentInfo ag = agentList.get(0);
                double ratioGD = ag.getGoldCapacity()/ag.getDiamondCapacity();
                if (diamondList.size() > diamondAgentList.size() && (ag.getDiamondCapacity() > stdD + confidenceDAt95 || ratioGD < 0.33)) {
                    agentList.remove(ag);
                    diamondAgentList.add(ag.getName());
                } else if (ag.getGoldCapacity() > stdG + confidenceGAt95 || ratioGD > 0.33) {
                    agentList.remove(ag);
                    goldAgentList.add(ag.getName());
                } else {
//                    Random rand = new Random();
//                    if (rand.nextInt(2) == 0) {
//                        agentList.remove(ag);
//                        diamondAgentList.add(ag.getName());
//                    } else {
                    agentList.remove(ag);
                    goldAgentList.add(ag.getName());
//                    }
                }
//                if (nbRequiredForGold >= nbRequiredForDiamond) {
//                    if (agentList.size() > 1) {
//                        AgentInfo ag1 = getAgentWithHighestCapOfGold(agentList);
//                        agentList.remove(ag1);
//                        AgentInfo ag2 = getAgentWithHighestCapOfGold(agentList);
//                        agentList.remove(ag2);
//                        if (ag1.getGoldCapacity()/ag1.getDiamondCapacity() < ag2.getGoldCapacity()/ag2.getDiamondCapacity()) {
//                            agentList.add(ag2);
//                            goldAgentList.add(ag1.getName());
//                            nbRequiredForGold -= 1;
//                        } else {
//                            agentList.add(ag1);
//                            goldAgentList.add(ag2.getName());
//                            nbRequiredForGold -= 1;
//                        }
//                    } else {
//                        AgentInfo ag = getAgentWithHighestCapOfGold(agentList);
//                        goldAgentList.add(ag.getName());
//                        agentList.remove(ag);
//                        nbRequiredForGold -= 1;
//                    }
//                } else {
//                    if (agentList.size() > 1) {
//                        AgentInfo ag1 = getAgentWithHighestCapOfDiamond(agentList);
//                        agentList.remove(ag1);
//                        AgentInfo ag2 = getAgentWithHighestCapOfDiamond(agentList);
//                        agentList.remove(ag2);
//                        if (ag1.getGoldCapacity()/ag1.getDiamondCapacity() < ag2.getGoldCapacity()/ag2.getDiamondCapacity()) {
//                            agentList.add(ag2);
//                            diamondAgentList.add(ag1.getName());
//                            nbRequiredForDiamond -= 1;
//                        } else {
//                            agentList.add(ag1);
//                            diamondAgentList.add(ag2.getName());
//                            nbRequiredForDiamond -= 1;
//                        }
//                    } else {
//                        AgentInfo ag = getAgentWithHighestCapOfDiamond(agentList);
//                        diamondAgentList.add(ag.getName());
//                        agentList.remove(ag);
//                        nbRequiredForDiamond -= 1;
//                    }
//                }
            }

            System.out.println(this.getLocalName() + " - gold list : "+ goldAgentList);
            System.out.println(this.getLocalName() + " - diamond list : "+ diamondAgentList);
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

            this.strategy = strategy.get(this.getLocalName());
        } else {
            for (Map.Entry<String, Treasure> t: this.treasuresMap.entrySet()) {
                if (t.getValue().getState().equals(TreasureState.OPENED) && t.getValue().getType().equals(this.treasureType))
                    this.treasureToPick.add(t.getKey());
            }
        }
    }

    private double calculateStdG(List<AgentInfo> agentList) {
        double sum = 0.0;
        double std = 0.0;
        for (AgentInfo ag: agentList) {
            sum += ag.getGoldCapacity();
        }
        double mean = sum/(agentList.size());
        for (AgentInfo ag: agentList) {
            std += Math.pow((ag.getGoldCapacity() - mean), 2);
        }
        return Math.sqrt(std/(agentList.size()));
    }

    private double calculateStdD(List<AgentInfo> agentList) {
        double sum = 0.0;
        double std = 0.0;
        for (AgentInfo ag: agentList) {
            sum += ag.getDiamondCapacity();

        }
        double mean = sum/(agentList.size());
        for (AgentInfo ag: agentList) {
            std += Math.pow((ag.getDiamondCapacity() - mean), 2);
        }
        return Math.sqrt(std/(agentList.size()));
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

    public void removeTreasureToPick(String location) {
        this.treasureToPick.remove(location);
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

    public String findNearestLockedTreasure(String myPosition) {
        if (this.treasuresMap.size() > 0) {
            String nearestTreasure = null;
            Integer distances = Integer.MAX_VALUE;
            for (Map.Entry<String, Treasure> node : this.treasuresMap.entrySet()) {
                if (node.getValue().getState().equals(TreasureState.LOCKED) && node.getValue().getStrength() < this.strength) {
                    Integer currentDist = this.myMap.getShortestPath(myPosition, node.getKey()).size();
                    if (distances > currentDist) {
                        nearestTreasure = node.getKey();
                        distances = currentDist;
                    }
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

        int t = 1 + new Random().nextInt(this.nbAgent);
        for(int i = 0; i < t; ++i){
            try {
                this.doWait(((ExploreFSMAgent)this).getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
            /* Move randomly */
            
            List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this).observe();
            int index = 1 + new Random().nextInt(obs.size() - 1);
            String pos = obs.get(index).getLeft();
            ((AbstractDedaleAgent) this).moveTo(pos);
        }

        ((ExploreFSMAgent)this).setNextMove(null);
    }

    public ArrayList<Integer> getFindedOnLastPass() {
        return this.findedOnLastPass;
    }

    public void addFindedOnLastPass(int i) {
        this.findedOnLastPass.add(i);
    }

    public int getIdealValueToTarget() {
        return this.objValue;
    }

    public Integer getCurrentValue(Observation o) {
        AgentInfo me = this.agentInfo.get(this.getLocalName());
        return o ==  Observation.GOLD ? me.getGoldValue() : me.getDiamondValue();
    }

    public Integer getCurrentCapacity() {

        AgentInfo me = this.agentInfo.get(this.getLocalName());
        return this.strategy.get(0).getType() == Observation.GOLD ? me.getGoldCapacity() : me.getDiamondCapacity();
    }

    public List<Treasure> getStrategy() {
        return this.strategy;
    }

    public Integer getPassNb() {
        return this.passNb;
    }

    public void increasePassNb() {
        ++this.passNb;
    }
}
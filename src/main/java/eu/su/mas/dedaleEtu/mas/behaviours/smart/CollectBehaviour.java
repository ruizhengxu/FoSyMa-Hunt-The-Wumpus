package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.AgentState;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.Treasure;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.TreasureState;
import jade.core.behaviours.OneShotBehaviour;

import java.util.List;
import java.util.Random;

public class CollectBehaviour extends OneShotBehaviour {

    private int exitValue;

    public CollectBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {

        this.exitValue = 0;

        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

        if (myPosition != null) {

            //Adding past position into agent list
            ((ExploreFSMAgent) this.myAgent).addPastPosition(myPosition);

            if (((ExploreFSMAgent) this.myAgent).isBlocked()) {
//                    System.out.println("BLOCKED IN MOVE");
                if (((ExploreFSMAgent) this.myAgent).getNextMove() != null) {
                    try {
                        this.myAgent.doWait(((ExploreFSMAgent) this.myAgent).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ((AbstractDedaleAgent) this.myAgent).moveTo(((ExploreFSMAgent) this.myAgent).getNextMove());
                    try {
                        this.myAgent.doWait(((ExploreFSMAgent) this.myAgent).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /* Move randomly */
                    List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();
                    int index = new Random().nextInt(obs.size() - 1);
                    String pos = obs.get(index).getLeft();
                    ((AbstractDedaleAgent) this.myAgent).moveTo(pos);
                    ((ExploreFSMAgent) this.myAgent).increaseStep();
                    ((ExploreFSMAgent) this.myAgent).setNextMove(null);
                    return;
                }

                this.exitValue = 1;

            } else {

                //List of observable from the agent's current position
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

                try {
                    this.myAgent.doWait(((ExploreFSMAgent) this.myAgent).getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ------------------------------------------------------------------------------------------------
                // Pick treasure
                //list of observations associated to the currentPosition
                List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();

                //example related to the use of the backpack for the treasure hunt
//                Treasure t = null;
                for (Couple<Observation, Integer> o : lObservations) {
//                    System.out.println(o);
                    switch (o.getLeft()) {
                        case LOCKPICKING:
                        case LOCKSTATUS:
                        case STENCH:
                        case AGENTNAME:
                        case STRENGH:
                            break;
                        case DIAMOND:
                        case GOLD:
                            if (((ExploreFSMAgent) this.myAgent).getTreasureToPick().contains(myPosition)) {
                                System.out.println(this.myAgent.getLocalName() + " - My current state is : " + (((ExploreFSMAgent) this.myAgent).getCurrentAgentState()));
                                System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + (((ExploreFSMAgent) this.myAgent).getTreasureType()));
                                System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                                System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                                if (((ExploreFSMAgent) this.myAgent).getTreasureType().equals(Observation.ANY_TREASURE)) {
                                    ((ExploreFSMAgent) this.myAgent).setTreasureType(o.getLeft());
                                    boolean open = ((AbstractDedaleAgent) this.myAgent).openLock(Observation.ANY_TREASURE);
                                    if (open) {
                                        int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
                                        System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
                                        ((ExploreFSMAgent) this.myAgent).pickedTreasure(myPosition, pickedQuantity);
                                    } else {
                                        ((ExploreFSMAgent)this.myAgent).removeTreasureToPick(myPosition);
                                    }
                                } else if (((ExploreFSMAgent) this.myAgent).getTreasureType() == o.getLeft()) {
                                    boolean open = ((AbstractDedaleAgent) this.myAgent).openLock(((ExploreFSMAgent) this.myAgent).getTreasureType());
                                    if (open) {
                                        int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
                                        System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
                                        ((ExploreFSMAgent) this.myAgent).pickedTreasure(myPosition, pickedQuantity);
                                    } else {
                                        ((ExploreFSMAgent)this.myAgent).removeTreasureToPick(myPosition);
                                    }
                                } else {
                                    System.out.println(this.myAgent.getLocalName() + " - I can't pick up this treasure");
                                }
                                System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                            } else {
                                Treasure t = new Treasure(myPosition, o.getLeft(), o.getRight(), ((ExploreFSMAgent)this.myAgent).getCurrentStep(), TreasureState.OPENED);
                                ((ExploreFSMAgent)this.myAgent).addTreasure(myPosition, t);
//                                if (t == null) {
//                                    t = new Treasure(myPosition, o.getLeft(), o.getRight(), ((ExploreFSMAgent)this.myAgent).getCurrentStep(), TreasureState.OPENED);
//                                } else {
//                                    t.setLocation(myPosition);
//                                    t.setType(o.getLeft());
//                                    t.setValue(o.getRight());
//                                    t.setLastModifiedDate(((ExploreFSMAgent)this.myAgent).getCurrentStep());
//                                    t.setState(TreasureState.OPENED);
//                                }
                            }
                            break;
//                        case STRENGH:
//                            if (t == null) {
//                                t = new Treasure(o.getRight());
//                            } else {
//                                t.setStrength(o.getRight());
//                            }
//                            break;
                        default: // If there is nothing in my position
//                            System.out.println(this.myAgent.getLocalName() + " - " + " my position is " + myPosition);
//                            System.out.println(this.myAgent.getLocalName() + " - " + " there is nothing in my position");
//                            System.out.println(((ExploreFSMAgent) this.myAgent).getTreasureToPick());
                            if (((ExploreFSMAgent) this.myAgent).getTreasuresMap().containsKey(myPosition)) {// If the current position is in my treasure map so I need to remove it
                                Treasure t_ = ((ExploreFSMAgent) this.myAgent).getTreasuresMap().get(myPosition);
                                if (t_.getState().equals(TreasureState.OPENED)) {
                                    ((ExploreFSMAgent) this.myAgent).missTreasure(myPosition);
                                }
                            }
                            if (((ExploreFSMAgent) this.myAgent).getTreasureToPick().contains(myPosition)) {
                                ((ExploreFSMAgent) this.myAgent).removeTreasureToPick(myPosition);
                            }
                            break;
                    }
                }
//                if (t != null && t.getStrength() != null && t.getValue() != null) {
//                    ((ExploreFSMAgent)this.myAgent).addTreasure(myPosition, t);
//                }


                if (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.GOLD) && ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight() == 0) {
                    this.exitValue = 3;
                    ((ExploreFSMAgent) this.myAgent).setAgentState(AgentState.FINISH);
                } else if (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.DIAMOND) && ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(1).getRight() == 0) {
                    this.exitValue = 3;
                    ((ExploreFSMAgent) this.myAgent).setAgentState(AgentState.FINISH);
                } else {
                    String nearestTreasure = ((ExploreFSMAgent) this.myAgent).findNearestTreasure(myPosition);
                    if (nearestTreasure != null) {
                        System.out.println(this.myAgent.getLocalName() + " - " + " my position is " + myPosition);
                        System.out.println(this.myAgent.getLocalName() + " - " + " nearest treasure is " + nearestTreasure);
                        List<String> path = ((ExploreFSMAgent) this.myAgent).myMap.getShortestPath(myPosition, nearestTreasure);
                        System.out.println("Shortest path : " + path);
                        if (path.size() > 0) {
                            String nextNode = ((ExploreFSMAgent) this.myAgent).myMap.getShortestPath(myPosition, nearestTreasure).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                            System.out.println(this.myAgent.getLocalName() + " - " + "next node is " + nextNode);
                            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                            ((ExploreFSMAgent) this.myAgent).increaseStep();
                        } else {
                            ((ExploreFSMAgent) this.myAgent).removeTreasureToPick(nearestTreasure);
                            ((ExploreFSMAgent) this.myAgent).increaseStep();
                        }
                    } else {
                        if ((((ExploreFSMAgent)this.myAgent).isThereFoundedTreasure()) || (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.GOLD) && ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight() > 0)
                                || (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.DIAMOND) && ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(1).getRight() > 0)) {
                            this.exitValue = 2;
                        } else {
                            this.exitValue = 3;
                            ((ExploreFSMAgent) this.myAgent).setAgentState(AgentState.FINISH);
                        }
                    }
                }
            }
        }

    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}

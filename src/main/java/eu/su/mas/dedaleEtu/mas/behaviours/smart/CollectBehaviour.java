package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.AgentState;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.Treasure;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.TreasureState;
import jade.core.behaviours.OneShotBehaviour;

import java.util.Iterator;
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
        System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

        if (myPosition != null) {

            //Adding past position into agent list
            ((ExploreFSMAgent)this.myAgent).addPastPosition(myPosition);

            if (((ExploreFSMAgent)this.myAgent).isBlocked()) {
                //System.out.println("BLOCKED IN MOVE");
                if (((ExploreFSMAgent)this.myAgent).getNextMove() != null) {
                    try {
                        this.myAgent.doWait(((ExploreFSMAgent)this.myAgent).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ((AbstractDedaleAgent) this.myAgent).moveTo(((ExploreFSMAgent)this.myAgent).getNextMove());
                    try {
                        this.myAgent.doWait(((ExploreFSMAgent)this.myAgent).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /* Move randomly */
                    List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();
                    int index = new Random().nextInt(obs.size() - 1);
                    String pos = obs.get(index).getLeft();
                    ((AbstractDedaleAgent) this.myAgent).moveTo(pos);
                    ((ExploreFSMAgent)this.myAgent).increaseStep();
                    ((ExploreFSMAgent)this.myAgent).setNextMove(null);
                    return;
                }

                this.exitValue = 1;

            } else if (((ExploreFSMAgent)this.myAgent).getTreasureToPick().contains(myPosition)){

                //List of observable from the agent's current position
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

                try {
                    this.myAgent.doWait(((ExploreFSMAgent)this.myAgent).getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ------------------------------------------------------------------------------------------------
                // Pick treasure
                //list of observations associated to the currentPosition
                List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();

                //example related to the use of the backpack for the treasure hunt
                for(Couple<Observation,Integer> o:lObservations){
                    switch (o.getLeft()) {
                        case DIAMOND:case GOLD:
                            System.out.println(this.myAgent.getLocalName()+" - My current state is : "+(((ExploreFSMAgent)this.myAgent).getCurrentAgentState()));
	                        System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+(((ExploreFSMAgent)this.myAgent).getTreasureType()));
	                        System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
	                        System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
                            if (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.ANY_TREASURE)) {
                                ((ExploreFSMAgent)this.myAgent).setTreasureType(o.getLeft());
                                System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.ANY_TREASURE));
                                int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
                                System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
                                ((ExploreFSMAgent)this.myAgent).pickedTreasure(myPosition, pickedQuantity);
                            } else if (((ExploreFSMAgent)this.myAgent).getTreasureType() == o.getLeft()) {
                                System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(((ExploreFSMAgent)this.myAgent).getTreasureType()));
                                int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
                                System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
                                ((ExploreFSMAgent)this.myAgent).pickedTreasure(myPosition, pickedQuantity);
                            } else {
                                System.out.println(this.myAgent.getLocalName() + " - I can't pick up this treasure");
                            }
                            System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                            break;
                        default: // If there is nothing in my position
                            if (((ExploreFSMAgent)this.myAgent).getTreasuresMap().containsKey(myPosition)) {// If the current position is in my treasure map so I need to remove it
                                Treasure t = ((ExploreFSMAgent)this.myAgent).getTreasuresMap().get(myPosition);
                                if (t.getState().equals(TreasureState.FOUND)) {
                                    ((ExploreFSMAgent) this.myAgent).missTreasure(myPosition);
                                }
                            }
                            break;
                    }
                }

                String nearstTreasure = ((ExploreFSMAgent)this.myAgent).findNearestTreasure(myPosition);
                String nextNode = ((ExploreFSMAgent)this.myAgent).myMap.getShortestPath(myPosition, nearstTreasure).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                ((ExploreFSMAgent)this.myAgent).increaseStep();

            }
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}

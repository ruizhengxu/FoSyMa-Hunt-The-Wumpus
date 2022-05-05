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
import net.sourceforge.plantuml.sequencediagram.teoz.ElseTile;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MoveBehaviour extends OneShotBehaviour {

    private int exitValue;

    public MoveBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {

		System.out.println(((ExploreFSMAgent)this.myAgent).getPastPosition());
        if (((ExploreFSMAgent)this.myAgent).myMap == null)
            ((ExploreFSMAgent)this.myAgent).myMap = new MapRepresentation();

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

					((ExploreFSMAgent)this.myAgent).setNextMove(null);
					return;
				}

        		this.exitValue = 1;
        		
        	} else {
        		
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
	            Boolean b=false;
	            for(Couple<Observation,Integer> o:lObservations){
	                switch (o.getLeft()) {
	                    case DIAMOND:case GOLD:
//							System.out.println(this.myAgent.getLocalName()+" - My current state is : "+(((ExploreFSMAgent)this.myAgent).getCurrentAgentState()));
//	                        System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+(((ExploreFSMAgent)this.myAgent).getTreasureType()));
//	                        System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//	                        System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
							///////////////////
							// IN STATE EXPLORE
							///////////////////
							if (((ExploreFSMAgent)this.myAgent).getCurrentAgentState().equals(AgentState.EXPLORE)) {
								Treasure t = new Treasure(o.getLeft(), o.getRight(), ((ExploreFSMAgent)this.myAgent).getCurrentStep(), TreasureState.FOUND);
								((ExploreFSMAgent)this.myAgent).addTreasure(myPosition, t);
							///////////////////
							// IN STATE COLLECT
							///////////////////
							} else if (((ExploreFSMAgent)this.myAgent).getCurrentAgentState().equals(AgentState.COLLECT)) {
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
								b=true;
							}
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
	
	            //If the agent picked (part of) the treasure
	            if (b){
	                List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	                System.out.println("State of the observations after picking "+lobs2);
	            }
	
	            //Trying to store everything in the tanker
	//            System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
	//            System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
	//            System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
	
	            // ------------------------------------------------------------------------------------------------
	            // Move and update map
	
	            //1) remove the current node from openlist and add it to closedNodes.
	            ((ExploreFSMAgent)this.myAgent).myMap.addNode(myPosition, MapRepresentation.MapAttribute.closed);
	
	            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
	            String nextNode = null;
	            Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
	            while (iter.hasNext()) {
	                String nodeId = iter.next().getLeft();
	                boolean isNewNode = ((ExploreFSMAgent)this.myAgent).myMap.addNewNode(nodeId);
	                //the node may exist, but not necessarily the edge
	                if (myPosition != nodeId) {
	                    ((ExploreFSMAgent)this.myAgent).myMap.addEdge(myPosition, nodeId);
	                    if (nextNode == null && isNewNode) nextNode = nodeId;
	                }
	            }
	
	            if (nextNode == null) {
					if(((ExploreFSMAgent)this.myAgent).myMap.getOpenNodes().size() > 0) {
						nextNode = ((ExploreFSMAgent)this.myAgent).myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
						((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
						((ExploreFSMAgent)this.myAgent).increaseStep();
//						System.out.println(this.myAgent.getLocalName() + " want to move to " + nextNode);
					}
	            }

				if (((ExploreFSMAgent)this.myAgent).getCurrentAgentState().equals(AgentState.EXPLORE)) {
					boolean switched = ((ExploreFSMAgent) this.myAgent).switchToCollect();
					if (switched) {
						this.exitValue = 2;
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

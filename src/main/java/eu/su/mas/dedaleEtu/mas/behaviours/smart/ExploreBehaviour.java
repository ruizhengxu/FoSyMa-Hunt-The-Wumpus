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

public class ExploreBehaviour extends OneShotBehaviour {

    /**
	 *
	 */
	private static final long serialVersionUID = -4537975631305736934L;
	private int exitValue;

    public ExploreBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {

		this.exitValue = 0;

        if (((ExploreFSMAgent)this.myAgent).myMap == null)
            ((ExploreFSMAgent)this.myAgent).myMap = new MapRepresentation();

		if (((ExploreFSMAgent)this.myAgent).getCurrentAgentState().equals(AgentState.COLLECT)) {
			((ExploreFSMAgent)this.myAgent).generateStrategy();
			this.exitValue = 2;
		}

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
				Treasure t = null;
	            for(Couple<Observation,Integer> o:lObservations){
//					System.out.println(o);
					switch (o.getLeft()) {
	                    case DIAMOND:case GOLD:
							if (t == null) {
								t = new Treasure(myPosition, o.getLeft(), o.getRight(), ((ExploreFSMAgent)this.myAgent).getCurrentStep(), TreasureState.FOUND);
							} else {
								t.setLocation(myPosition);
								t.setType(o.getLeft());
								t.setValue(o.getRight());
								t.setLastModifiedDate(((ExploreFSMAgent)this.myAgent).getCurrentStep());
								t.setState(TreasureState.FOUND);
							}
							break;
						case STRENGH:
							if (t == null) {
								t = new Treasure(o.getRight());
							} else {
								t.setStrength(o.getRight());
							}
							break;
						case LOCKPICKING: case LOCKSTATUS: case STENCH: case AGENTNAME:
							break;
	                    default: // If there is nothing in my position
							if (((ExploreFSMAgent)this.myAgent).getTreasuresMap().containsKey(myPosition)) {// If the current position is in my treasure map so I need to remove it
								Treasure t_ = ((ExploreFSMAgent)this.myAgent).getTreasuresMap().get(myPosition);
								if (t_.getState().equals(TreasureState.FOUND)) {
									((ExploreFSMAgent) this.myAgent).missTreasure(myPosition);
								}
							}
	                        break;
	                }
	            }
				if (t != null) {
					((ExploreFSMAgent)this.myAgent).addTreasure(myPosition, t);
				}

	
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

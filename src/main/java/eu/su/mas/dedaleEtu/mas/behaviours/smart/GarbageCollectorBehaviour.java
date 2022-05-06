//package eu.su.mas.dedaleEtu.mas.behaviours.smart;
//
//import dataStructures.tuple.Couple;
//import eu.su.mas.dedale.env.Observation;
//import eu.su.mas.dedale.mas.AbstractDedaleAgent;
//import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
//import eu.su.mas.dedaleEtu.mas.knowledge.smart.AgentState;
//import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
//import eu.su.mas.dedaleEtu.mas.knowledge.smart.Treasure;
//import eu.su.mas.dedaleEtu.mas.knowledge.smart.TreasureState;
//import jade.core.behaviours.OneShotBehaviour;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Random;
//
//
//public class GarbageCollectorBehaviour extends OneShotBehaviour {
//
//    /**
//	 *
//	 */
//	private static final long serialVersionUID = -8418639395999827485L;
//	private int exitValue;
//
//	private int greed = 20; // in %
//
//	private boolean omw = true;
//	private int searchedTreasure = 0; //index du tresor cherché
//	private int moveToFindTreasure = 8;
//	private int countMove = 0;
//
//    public GarbageCollectorBehaviour(final AbstractDedaleAgent myAgent) {
//        super(myAgent);
//        this.exitValue = 0;
//    }
//
//    @Override
//    public void action() {
//
//		this.exitValue = 0;
//
//        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
//
//        if (myPosition != null) {
//
//        	//Adding past position into agent list
//        	((ExploreFSMAgent)this.myAgent).addPastPosition(myPosition);
//
//        	if (((ExploreFSMAgent)this.myAgent).isBlocked()) {
//				//System.out.println("BLOCKED IN MOVE");
//				if (((ExploreFSMAgent)this.myAgent).getNextMove() != null) {
//					((ExploreFSMAgent)this.myAgent).moveAfterBlocking();
//					return;
//				}
//
//        		this.exitValue = 1;
//
//        	} else {
//
//				try {
//	                this.myAgent.doWait(((ExploreFSMAgent)this.myAgent).getTime());
//	            } catch (Exception e) {
//	                e.printStackTrace();
//	            }
//
//				// Ici on cherche a retourner aux alentours de chaque trésor qui à été déplacé
//				// On part du principe que le plus gros tresor est en tête de liste
//				ArrayList<String> treasureNotFound = new ArrayList<String>();
//				int max = ((ExploreFSMAgent)this.myAgent).getIdealValueToTarget() + (((ExploreFSMAgent)this.myAgent).getIdealValueToTarget() * this.greed / 100);
//
//				if(treasureNotFound.size() != this.searchedTreasure) {
//
//					//On s'assure que le trésor actuel a pas déja été traité
//					while(((ExploreFSMAgent)this.myAgent).getFindedOnLastPass().contains(this.searchedTreasure))
//						++this.searchedTreasure;
//
//					// observation de l'agent
//					List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();
//
//					// On est arrivé à la position souhaité
//					if(omw == false){
//						// On gravite autours de la position
//						// On fait N random move à partir de la position initial du tresor MAIS EN SE PRIVANT DE LA POSITION D'ORIGINE (si possible)
//						// --> on check les N itération en dehors du if avec "countMove"
//
//
//						int index = 0;
//
//						if(obs.size() > 2) {
//							while(obs.get(index).getLeft() != myPosition)
//								index = new Random().nextInt(obs.size() - 1);
//						} else {
//							index = new Random().nextInt(obs.size() - 1);
//						}
//
//						String pos = obs.get(index).getLeft();
//
//						((AbstractDedaleAgent) this.myAgent).moveTo(pos);
//
//
//						++this.countMove;
//					}
//
//					if(this.countMove == moveToFindTreasure) {
//						// Si au bout de N tentative on a pas trouvé le trésor, on abandonne et on passe au suivant
//						++this.searchedTreasure;
//						omw = true;
//					}
//
//					// On va a la position souhaitée
//					if(omw == true){
//						String targetedNode = ((ExploreFSMAgent)this.myAgent).myMap.getShortestPath(myPosition, treasureNotFound.get(this.searchedTreasure)).get(0);
//						((AbstractDedaleAgent) this.myAgent).moveTo(targetedNode);
//						((ExploreFSMAgent)this.myAgent).increaseStep();
//
//						if(((AbstractDedaleAgent)this.myAgent).getCurrentPosition() == targetedNode) {
//							omw = false;
//						}
//					}
//
//					//On regarde si on marche sur un trésor
//					List<Couple<Observation,Integer>> lObservations= obs.get(0).getRight();
//	            	for(Couple<Observation,Integer> o:lObservations){
//						if (((ExploreFSMAgent)this.myAgent).getTreasureType() == o.getLeft()) {
//							//System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(((ExploreFSMAgent)this.myAgent).getTreasureType()));
//
//							int val = o.getLeft().values();
//
//							/* Si on trouve un même trésor que dans la liste*/
//							if (treasureNotFound.contains(o.getLeft())) {
//								int idx = treasureNotFound.indexOf(o.getLeft());
//								((ExploreFSMAgent)this.myAgent).addFindedOnLastPass(idx);
//							}
//							//On peut pick sans dépasser la limite fixée
//							if (max >= ((ExploreFSMAgent)this.myAgent).getValue() + val) {
//								int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
//								System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
//								((ExploreFSMAgent)this.myAgent).pickedTreasure(myPosition, pickedQuantity);
//							}
//						}
//					}
//
//					// -- --> On ramasse ce qu'on peut sans trop dépasser la valeur ideale (disons 20% au dessus)
//				} else {
//
//					// finish
//				}
//			}
//        }
//    }
//
//    @Override
//    public int onEnd() {
//        return this.exitValue;
//    }
//
//}

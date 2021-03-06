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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GarbageCollectorBehaviour extends OneShotBehaviour {

   /**
	 *
	 */
	private static final long serialVersionUID = -8418639395999827485L;
	private int exitValue;

	private int greed = 20; // in %

	private boolean omw = true;
	private int searchedTreasure = 0; //index du tresor cherché
	private int moveToFindTreasure = 8;
	private int countMove = 0;

   public GarbageCollectorBehaviour(final AbstractDedaleAgent myAgent) {
       super(myAgent);
       this.exitValue = 0;
   }

   @Override
   public void action() {

		this.exitValue = 0;

       	String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

       	if (myPosition != null) {

       	//Adding past position into agent list
       	((ExploreFSMAgent)this.myAgent).addPastPosition(myPosition);

       	if (((ExploreFSMAgent)this.myAgent).isBlocked()) {

				if (((ExploreFSMAgent)this.myAgent).getNextMove() != null) {
					((ExploreFSMAgent)this.myAgent).moveAfterBlocking();
					return;
				}

       		this.exitValue = 1;

       	} else {

				try {
	                this.myAgent.doWait(((ExploreFSMAgent)this.myAgent).getTime());
	            } catch (Exception e) {
	                e.printStackTrace();
	            }

				// Ici on cherche a retourner aux alentours de chaque trésor qui à été déplacé
				// On part du principe que le plus gros tresor est en tête de liste
				ArrayList<String> treasureNotFound = new ArrayList<String>(((ExploreFSMAgent)this.myAgent).getTreasureToPick());
				int max = ((ExploreFSMAgent)this.myAgent).getIdealValueToTarget() + (((ExploreFSMAgent)this.myAgent).getIdealValueToTarget() * this.greed / 100);

				if(treasureNotFound.size() != this.searchedTreasure + 1 && treasureNotFound.size() > 0) {

					System.out.println(this.myAgent.getLocalName() + " GARBAGE COLLECOR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					//On s'assure que le trésor actuel a pas déja été traité
					while(((ExploreFSMAgent)this.myAgent).getFindedOnLastPass().contains(this.searchedTreasure))
						++this.searchedTreasure;
					
					System.out.println(treasureNotFound.size());
					System.out.println(this.searchedTreasure);
					if (treasureNotFound.size() != this.searchedTreasure + 1){
						// observation de l'agent
						List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();

						// On est arrivé à la position souhaité
						if(omw == false){
							// On gravite autours de la position
							// On fait N random move à partir de la position initial du tresor MAIS EN SE PRIVANT DE LA POSITION D'ORIGINE (si possible)
							// --> on check les N itération en dehors du if avec "countMove"


							int index = 0;

							if(obs.size() > 2) {
								while(obs.get(index).getLeft() != myPosition)
									index = new Random().nextInt(obs.size() - 1);
							} else {
								index = new Random().nextInt(obs.size() - 1);
							}

							String pos = obs.get(index).getLeft();

							((AbstractDedaleAgent) this.myAgent).moveTo(pos);


							++this.countMove;
						}

						if(this.countMove == moveToFindTreasure) {
							// Si au bout de N tentative on a pas trouvé le trésor, on abandonne et on passe au suivant
							++this.searchedTreasure;
							omw = true;
						}

						// On va a la position souhaitée
						if(omw == true){
							String where = treasureNotFound.get(this.searchedTreasure);
							if(((AbstractDedaleAgent)this.myAgent).getCurrentPosition().equals(where)) {
								omw = false;
							} else {
								// System.out.println(myPosition);
								// System.out.println(where);
								if (((ExploreFSMAgent)this.myAgent).myMap.getOpenNodes().contains(where) || ((ExploreFSMAgent)this.myAgent).myMap.getClosedNodes().contains(where)){
									String targetedNode = ((ExploreFSMAgent)this.myAgent).myMap.getShortestPath(myPosition, where).get(0);
									((AbstractDedaleAgent) this.myAgent).moveTo(targetedNode);
									((ExploreFSMAgent)this.myAgent).increaseStep();
								}
							}
							
						}
					}

					

					// -- --> On ramasse ce qu'on peut sans trop dépasser la valeur ideale (disons 20% au dessus)
				} else {
					// finish
					System.out.println(this.myAgent.getLocalName() + " END -- RANDOM MOVE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					/* Move randomly */
					List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();
					int index = 1 + new Random().nextInt(obs.size() - 1);
					String pos = obs.get(index).getLeft();
					((AbstractDedaleAgent) this.myAgent).moveTo(pos);

					((ExploreFSMAgent)this.myAgent).myMap.clear();
					((ExploreFSMAgent)this.myAgent).setAgentState(AgentState.EXPLORE);
					((ExploreFSMAgent)this.myAgent).increasePassNb();
					this.exitValue = 2;
					return;
				}

				//On regarde si on marche sur un trésor
				// observation de l'agent
				List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent) this.myAgent).observe();
				List<Couple<Observation,Integer>> lObservations= obs.get(0).getRight();
				for(Couple<Observation,Integer> o:lObservations){
					if (((ExploreFSMAgent)this.myAgent).getTreasureType() == o.getLeft()) {
						//System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(((ExploreFSMAgent)this.myAgent).getTreasureType()));

						Treasure t = new Treasure(myPosition, o.getLeft(), o.getRight(), ((ExploreFSMAgent)this.myAgent).getCurrentStep(), TreasureState.OPENED);
						int val = t.getValue();
						//t.getType() Observation.GOLD
						/* Si on trouve un même trésor que dans la liste*/
						// if (treasureNotFound.contains(t.)) {
						// 	int idx = treasureNotFound.indexOf(t);
						// 	((ExploreFSMAgent)this.myAgent).addFindedOnLastPass(idx);
						// }
						
						//On peut pick sans dépasser la limite fixée
						if (max >= ((ExploreFSMAgent)this.myAgent).getCurrentValue(t.getType()) + val/*&& ((ExploreFSMAgent)this.myAgent).getCurrentValue(t.getType()) + val <= ((ExploreFSMAgent)this.myAgent).getCurrentCapacity()*/) {
							((AbstractDedaleAgent) this.myAgent).openLock(((ExploreFSMAgent) this.myAgent).getTreasureType());
							int pickedQuantity = ((AbstractDedaleAgent) this.myAgent).pick();
							System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + pickedQuantity);
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

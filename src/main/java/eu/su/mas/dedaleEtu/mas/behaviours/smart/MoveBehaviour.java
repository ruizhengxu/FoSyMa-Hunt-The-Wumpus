package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

import java.util.Iterator;
import java.util.List;

public class MoveBehaviour extends OneShotBehaviour {

    private int exitValue;

    public MoveBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {

        if (((ExploreFSMAgent)this.myAgent).myMap == null)
            ((ExploreFSMAgent)this.myAgent).myMap = new MapRepresentation();

        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

        if (myPosition != null) {
            //List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

            try {
                this.myAgent.doWait(1000);
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
                        System.out.println(Observation.DIAMOND);
                        System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+(((ExploreFSMAgent)this.myAgent).getTreasureType()));
                        System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
                        if (((ExploreFSMAgent)this.myAgent).getTreasureType().equals(Observation.ANY_TREASURE)) {
                            ((ExploreFSMAgent)this.myAgent).setTreasureType(o.getLeft());
                            System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + ((AbstractDedaleAgent) this.myAgent).pick());
                        } else if (((ExploreFSMAgent)this.myAgent).getTreasureType() == o.getLeft()) {
                            System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + ((AbstractDedaleAgent) this.myAgent).pick());
                        } else {
                            System.out.println(this.myAgent.getLocalName() + " - I can't pick up this treasure");
                        }
                        /*}*/
                        System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b=true;
                        break;
                    default:
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
                nextNode = ((ExploreFSMAgent)this.myAgent).myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                System.out.println(this.myAgent.getLocalName() + " want to move to " + nextNode);
            }
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
}

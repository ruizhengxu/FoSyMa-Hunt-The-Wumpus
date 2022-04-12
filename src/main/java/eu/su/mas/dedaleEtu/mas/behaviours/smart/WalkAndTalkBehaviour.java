package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Iterator;
import java.util.List;

/**
 * This behaviour allows agent to move in the map and send a short message around him.
 * At the same time, agent also checks if a message is received.
 */
public class WalkAndTalkBehaviour extends OneShotBehaviour {

    /**
     * exitValue : int
     *  - 0 : repeat this behaviour
     *  - 1 : message received
     *  - 2 : exploration done
     */
    private int exitValue;
    private MapRepresentation myMap;
    private List<String> receivers;

    public WalkAndTalkBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, List<String> receivers) {
        super(myAgent);
        this.exitValue = 0;
        this.myMap = myMap;
        this.receivers = receivers;
    }

    @Override
    public void action() {

        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }

        //0) Retrieve the current position
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

        if (myPosition!=null) {
            //List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

            /**
             * Just added here to let you see what the agent is doing, otherwise he will be too quick
             */
            try {
                this.myAgent.doWait(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 1) remove the current node from openlist and add it to closedNodes.
            this.myMap.addNode(myPosition, MapRepresentation.MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            String nextNode = null;
            Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
            while (iter.hasNext()) {
                String nodeId = iter.next().getLeft();
                boolean isNewNode = this.myMap.addNewNode(nodeId);
                //the node may exist, but not necessarily the edge
                if (myPosition != nodeId) {
                    this.myMap.addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode) nextNode = nodeId;
                }
            }

            if (!this.myMap.hasOpenNode()) {
                this.exitValue = 2; // exitValue = 2 --> end the exploration
                System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done");
            }else {
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode == null) {
                    //no directly accessible openNode
                    //chose one, compute the path and take the first step.
                    nextNode = this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    //System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
                }

                // Check message
                ACLMessage msgReceived = this.checkMsgBox();
                if (msgReceived != null) { // If received message
                    System.out.println(this.myAgent.getLocalName() +  " has received : " + msgReceived.getContent());
                } else {
                    // If no message received, move to next position and send message to everyone
                    System.out.println(this.myAgent.getLocalName() + " move to " + nextNode);
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                    sendMessage();
                }
            }
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }

    private ACLMessage checkMsgBox() {
        MessageTemplate msgTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-POS"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);

        return msgReceived;
    }

    private void sendMessage() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        // Add message's receivers
        for (String receiver : this.receivers) {
            if (! (this.myAgent.getLocalName().equals(receiver))) {
                msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
            }
        }
        msg.setProtocol("SHARE-POS");
        msg.setContent("Hi");
        System.out.println(this.myAgent.getLocalName() + " sending message..");
        this.myAgent.send(msg);
    }
}

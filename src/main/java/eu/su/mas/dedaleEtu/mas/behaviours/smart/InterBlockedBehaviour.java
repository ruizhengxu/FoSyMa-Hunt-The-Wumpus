package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InterBlockedBehaviour extends OneShotBehaviour {

    private static final long serialVersionUID = 5829805489827463023L;
    private int exitValue;
    private List<String> receivers;

    public InterBlockedBehaviour(final AbstractDedaleAgent myAgent, List<String> receivers) {
        super(myAgent);
        this.exitValue = 0;
        this.receivers = receivers;
    }

    @Override
    public void action() {
        System.out.println(myAgent.getLocalName() + " - BLOCKED!!!!");
        messageTool();

        // System.out.println(((ExploreFSMAgent) this.myAgent).getBusyNeighbors());

        // Recovering what the agent see
        List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

        Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
        ArrayList<String> possibleNodes = new ArrayList<String>();
        while (iter.hasNext()) {
            String nodeId = iter.next().getLeft();

            String cur = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();

            // If is is not the current node
            if (!nodeId.equals(cur)) {

                // then check if it is a busy node
                Boolean can = true;
                for (ArrayList<String> node : ((ExploreFSMAgent) this.myAgent).getBusyNeighbors()) {
                    if (node.contains(nodeId)) { // check by theire name DONT FORGET
                        can = false;
                        break;
                    }
                }

                // if it is not we could add it to our possible moves
                if (can) {
                    // System.out.println(((AbstractDedaleAgent) this.myAgent).observe());
                    // System.out.println( ((AbstractDedaleAgent) this.myAgent).getCurrentPosition()
                    // + " " + nodeId + " AVOIDING BLOCK");
                    // ((AbstractDedaleAgent) this.myAgent).moveTo(nodeId);

                    // here we add in first open node to focus on them
                    boolean isNewNode = ((ExploreFSMAgent) this.myAgent).myMap.addNewNode(nodeId);
                    if (isNewNode == true) {
                        possibleNodes.add(0, nodeId);
                    } else {
                        possibleNodes.add(nodeId);
                    }
                }
            }
        }

        // to finish we select the first move as the next move
        if (possibleNodes.size() > 0) {
            ((ExploreFSMAgent) this.myAgent).setNextMove(possibleNodes.get(0));
        }

        // usless ?
        // adding node to forget temporerly (to avoid reblocking)
        for (ArrayList<String> node : ((ExploreFSMAgent) this.myAgent).getBusyNeighbors()) {
            ((ExploreFSMAgent) this.myAgent).addToForget(node.get(1));
        }

        ((ExploreFSMAgent) this.myAgent).clearBusyNeighbors();
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }

    /*
     * Send a messages to every receiver to tell them that the agent is blocked at a
     * certain node. This is based on the number of agents. Each agents have to tell
     * others that it is blocked duringt a certain time range: It is a way handles
     * multiples messages at "the same time" (not really at the same time but you
     * got it...).
     * Then it add neighbors position to a set to remember position to don't move to !
     */
    private void messageTool() {
        //System.out.println(((AbstractDedaleAgent) this.myAgent).getLocalName());
        char[] currentName = ((AbstractDedaleAgent) this.myAgent).getLocalName().toCharArray();
        Integer idx = Character.getNumericValue(currentName[5]);

        for (int i = 0; i < ((ExploreFSMAgent) this.myAgent).getNbAgent(); i++) {

            this.myAgent.doWait(50);

            if (i + 1 == idx) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setSender(this.myAgent.getAID());
                for (String receiver : this.receivers) {
                    if (!(this.myAgent.getLocalName().equals(receiver))) {
                        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
                    }
                }
                msg.setProtocol("BLOCKED");
                msg.setContent(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
                this.myAgent.send(msg);
            } else {
                MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
                if (msgReceived != null) {
                    if (msgReceived.getProtocol().equals("BLOCKED")) {
                        ((ExploreFSMAgent) this.myAgent).addBusyNeighbors(msgReceived);
                    }
                }
            }
        }
    }

}
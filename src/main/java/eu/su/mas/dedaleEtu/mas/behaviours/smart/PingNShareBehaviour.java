package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;

public class PingNShareBehaviour extends OneShotBehaviour {

    private int exitValue;
    private List<String> receivers;

    public PingNShareBehaviour(final AbstractDedaleAgent myAgent, List<String> receivers) {
        super(myAgent);
        this.exitValue = 0;
        this.receivers = receivers;
    }

    @Override
    public void action() {

        if (((ExploreFSMAgent)this.myAgent).myMap == null)
            ((ExploreFSMAgent)this.myAgent).myMap = new MapRepresentation();

        // Send message ping
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.myAgent.getAID());
        // Add message's receivers
        for (String receiver : this.receivers) {
            if (! (this.myAgent.getLocalName().equals(receiver))) {
                msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
            }
        }
        msg.setProtocol("SHARE-PING");
        msg.setContent("Hi");
        System.out.println(this.myAgent.getLocalName() + " sending message..");
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

        // Check mailbox
        MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);

        if (msgReceived !=  null) {
            // If received ping message
            if (msgReceived.getProtocol().equals("SHARE-PING")) {
                System.out.println(this.myAgent.getLocalName() + " received : " + msgReceived.getContent());
                System.out.println("Sender is : " + msgReceived.getSender().getLocalName());
                this.shareMap(msgReceived.getSender());
                System.out.println(this.myAgent.getLocalName() + " send Map");
            }
            // If received map
            else if (msgReceived.getProtocol().equals("SHARE-MAP")) {
                SerializableSimpleGraph<String, MapRepresentation.MapAttribute> receivedMap = null;
                try {
                    receivedMap = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)msgReceived.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                ((ExploreFSMAgent)this.myAgent).myMap.mergeMap(receivedMap);
                System.out.println("Map Merged");
            }
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }

    private void shareMap(AID sender) {
        ACLMessage mapMsg = new ACLMessage(ACLMessage.INFORM);
        mapMsg.setProtocol("SHARE-MAP");
        mapMsg.setSender(this.myAgent.getAID());
        mapMsg.addReceiver(sender);

        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg = ((ExploreFSMAgent)this.myAgent).myMap.getSerializableGraph();
        try {
            mapMsg.setContentObject(sg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(mapMsg);
    }
}

package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.smart.ExploreFSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.AgentInfo;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.Treasure;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
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
        
        if(((ExploreFSMAgent)this.myAgent).isBlocked())
        	msg.setContent(STATE_BLOCKED);
        else
        	msg.setContent("Hi");
        
        System.out.println(this.myAgent.getLocalName() + " sending message..");
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

        //////////////////////////////////////////////////////////////////////////////////////////
        // Check mailbox
        MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);

        if (msgReceived !=  null) {
            switch (msgReceived.getProtocol()) {
                case "SHARE-PING":
                    // Share map
                    this.shareMap(msgReceived.getSender());
                    // Share treasure map
                    if (((ExploreFSMAgent) this.myAgent).getTreasuresMap().size() > 0 && ((ExploreFSMAgent)this.myAgent).isTreasureMapUpdated())
                        this.shareTreasuresMap(msgReceived.getSender());
                    // Share agent info
                    if (((ExploreFSMAgent)this.myAgent).isAgentInfoUpdated())
                        this.shareAgentInfo(msgReceived.getSender());
                    System.out.println(this.myAgent.getLocalName() + " share his knowledge");
                    break;
                case "SHARE-MAP":
                    SerializableSimpleGraph<String, MapRepresentation.MapAttribute> receivedMap = null;
                    try {
                        receivedMap = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>) msgReceived.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    ((ExploreFSMAgent) this.myAgent).myMap.mergeMap(receivedMap);
                    break;
                case "SHARE-TREASURE":
                    HashMap<String, Treasure> receivedTreasuresMap = null;
                    try {
                        receivedTreasuresMap = (HashMap<String, Treasure>) msgReceived.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    ((ExploreFSMAgent) this.myAgent).mergeTreasuresMap(receivedTreasuresMap);
                    break;
                case "SHARE-AGENT-INFO":
                    HashMap<String, AgentInfo> receivedAgentInfo = null;
                    try {
                        receivedAgentInfo = (HashMap<String, AgentInfo>) msgReceived.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    ((ExploreFSMAgent) this.myAgent).mergeAgentInfo(receivedAgentInfo);
                    break;
                case "SHARE-PAST-POSITION":
                    List<String> receivedPastPos = null;
                    try {
                        receivedPastPos = (List<String>) msgReceived.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    // DO SOME STUFF
                    break;
                default:
                    break;
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

    private void shareTreasuresMap(AID sender) {
        ACLMessage treasureMapMsg = new ACLMessage(ACLMessage.INFORM);
        treasureMapMsg.setProtocol("SHARE-TREASURE");
        treasureMapMsg.setSender(this.myAgent.getAID());
        treasureMapMsg.addReceiver(sender);

        HashMap<String, Treasure> tm = ((ExploreFSMAgent)this.myAgent).getTreasuresMap();
        try {
            treasureMapMsg.setContentObject(tm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(treasureMapMsg);
    }

    private void shareAgentInfo(AID sender) {
        ACLMessage agentInfoMsg = new ACLMessage(ACLMessage.INFORM);
        agentInfoMsg.setProtocol("SHARE-AGENT-INFO");
        agentInfoMsg.setSender(this.myAgent.getAID());
        agentInfoMsg.addReceiver(sender);

        HashMap<String, AgentInfo> infos = ((ExploreFSMAgent)this.myAgent).getAgentInfo();
        try {
            agentInfoMsg.setContentObject(infos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(agentInfoMsg);
    }
    
    //Only send a message if it is blocked
    private void sharePastPosition(AID sender) {
        ACLMessage positionMsg = new ACLMessage(ACLMessage.INFORM);
        positionMsg.setProtocol("SHARE-PAST-POSITION");
        positionMsg.setSender(this.myAgent.getAID());
        positionMsg.addReceiver(sender);

        try {
        	positionMsg.setContentObject((Serializable) ((ExploreFSMAgent) this.myAgent).getPastPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent)this.myAgent).sendMessage(positionMsg);
    }
}

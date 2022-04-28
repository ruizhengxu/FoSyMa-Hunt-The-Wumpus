package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareNMergeMapBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 7839710357718295253L;
	private MapRepresentation myMap;
	private List<String> receivers;

	public ShareNMergeMapBehaviour(Agent a, MapRepresentation mymap, List<String> receivers) {
		this.myMap=mymap;
		this.receivers=receivers;	
	}

	@Override
	public void action() {
		// Share Map
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-MAP");
		msg.setSender(this.myAgent.getAID());
		for (String agentName : receivers) {
			if(!(this.myAgent.getLocalName().equals(agentName))) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
		}

		SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg = this.myMap.getSerializableGraph();
		try {
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

		// Merge Map if exists
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);

		if (msgReceived!=null) {
			SerializableSimpleGraph<String, MapRepresentation.MapAttribute> receivedMap = null;
			try {
				receivedMap = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			this.myMap.mergeMap(receivedMap);
			System.out.println("Map Merged");
		}
	}
}

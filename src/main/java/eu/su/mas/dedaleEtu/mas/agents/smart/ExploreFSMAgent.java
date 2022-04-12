package eu.su.mas.dedaleEtu.mas.agents.smart;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.smart.*;
import eu.su.mas.dedaleEtu.mas.knowledge.smart.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreFSMAgent extends AbstractDedaleAgent {

    private static final long serialVersionUID = -7969469610244668140L;
    public MapRepresentation myMap;
    private Observation treasureType;
    
    // State names
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";

    protected void setup() {
        super.setup();

        this.treasureType = Observation.ANY_TREASURE;

        final Object[] args = getArguments();
        List<String> receivers = this.getReceivers(args);
        FSMBehaviour fsm = new FSMBehaviour(this);
        fsm.registerFirstState(new PingNShareBehaviour(this, receivers), A);
        fsm.registerState(new MoveBehaviour(this), B);
        fsm.registerDefaultTransition(A, B);
        fsm.registerDefaultTransition(B, A);

        List<Behaviour> lb=new ArrayList<Behaviour>();
        lb.add(fsm);
        addBehaviour(new startMyBehaviours(this,lb));

        System.out.println("the  agent "+this.getLocalName()+ " is started");
    }

    private List<String> getReceivers(Object[] args) {

        List<String> list_agentNames =  new ArrayList<String>();

        if(args.length==0){
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        }else{
            int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i<args.length) {
                list_agentNames.add((String)args[i]);
                i++;
            }
        }
        return list_agentNames;
    }

    public Observation getTreasureType() {
        return this.treasureType;
    }

    public void setTreasureType(Observation treasureType) {
        this.treasureType = treasureType;
    }

}
package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class InterBlockedBehaviour extends OneShotBehaviour {

	
    private int exitValue;
    private List<String> receivers;

    public InterBlockedBehaviour(final AbstractDedaleAgent myAgent, List<String> receivers) {
        super(myAgent);
        this.exitValue = 0;
        this.receivers = receivers;
    }

 
	@Override
	public void action() {
		System.out.println("BLOCKED!!!!");
		
	}
	
    @Override
    public int onEnd() {
        return this.exitValue;
    }

}

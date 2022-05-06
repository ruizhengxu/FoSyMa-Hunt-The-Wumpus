package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class FinishBehaviour extends OneShotBehaviour {

    private int exitValue;

    public FinishBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " is finished !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}

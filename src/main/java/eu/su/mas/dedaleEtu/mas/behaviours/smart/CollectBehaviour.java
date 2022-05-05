package eu.su.mas.dedaleEtu.mas.behaviours.smart;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class CollectBehaviour extends OneShotBehaviour {

    private int exitValue;

    public CollectBehaviour(final AbstractDedaleAgent myAgent) {
        super(myAgent);
        this.exitValue = 0;
    }

    @Override
    public void action() {

    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}

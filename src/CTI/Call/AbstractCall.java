/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import Daemon.EventHandler;
import Daemon.Events.AbstractEvent;
import Daemon.Events.PersistableInterface;
import Operator.Gateway.Client;
import java.util.List;

/**
 *
 * @author bsalmanov
 */
abstract class AbstractCall extends AbstractEvent implements CallInterface{

    protected Client client;
    protected String callID;
    protected Integer startTime = 0;
    protected Integer length = 0;
    protected Integer type = 0;
    protected boolean complete = false;

    public AbstractCall(Client client, String callID){
        this.client = client;
        this.callID = callID;
    }

    public void started(){
        startTime = getTimestamp();
    }

    public void complete(){
        if( 0 != startTime ){
            length = getTimestamp() - startTime;
        } else {
            startTime = getTimestamp();
        }
        complete = true;
    }

    public String getAgent() {
        return this.client.getLogin();
    }

    protected Client getClient(){
        return this.client;
    }

    public String getCallID() {
        return callID;
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public Integer getType() {
        return this.type;
    }

    @Override
    public Integer getStartTime() {
        return startTime;
    }

    @Override
    public Integer getDuration() {
        return length;
    }

    @Override
    public List<TimelineEvent> getTimeline() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final void persist(){
        EventHandler.logger.trace("Abstract call persist.");
        if( this instanceof SimpleCall){
            CompositeCall parent = ((SimpleCall)this).getCompositeCall();
            if( null != parent){
                Daemon.Server.events.proceedEvent( parent );
                return;
            }
        }
        Daemon.Server.events.proceedEvent( (PersistableInterface) this );
}
}

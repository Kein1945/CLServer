/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Gateway;

import CTI.Call.CallInterface;
import CTI.Call.CompositeCall;
import CTI.Call.SimpleCall;
import Daemon.Events.PersistableInterface;
import Operator.Gateway.Client;
import com.cisco.cti.ctios.cil.Arg;
import com.cisco.cti.ctios.cil.Arguments;
import com.cisco.cti.ctios.util.CtiOs_IKeywordIDs;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author bsalmanov
 */
public class CallPool {
    protected volatile Map<String, CallInterface> CallPool = new ConcurrentHashMap<>();

    protected Client client;

    public CallPool(Client c) {
        this.client = c;
    }

    private String getCallId(Arguments arg){
        return arg.GetValueString(CtiOs_IKeywordIDs.CTIOS_UNIQUEOBJECTID);
    }

    protected CallInterface getCallByArgs(Arguments args){
        return getCallById( this.getCallId(args) );
    }

    private CallInterface getCallById(String uniqID){
        if( CallPool.containsKey( uniqID ) ){
            return CallPool.get( uniqID );
        } else {
            return null;
        }
    }

    public SimpleCall callBegin(Arguments arg){
        String callUniqID = getCallId(arg);
        SimpleCall call = new SimpleCall( this.client, callUniqID );
        CallPool.put( callUniqID, call);


        String Number = arg.GetValueString(CtiOs_IKeywordIDs.CTIOS_ANI);
        String DeviceId = arg.GetValueString(CtiOs_IKeywordIDs.CTIOS_DEVICEID);
        call.setDevice(DeviceId);
        if( null != Number){
            call.setNumber(Number);
            call.setIncome();
        }
        call.onBegin();
        return call;
    }

    public SimpleCall callOriginated(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call){
            call.setOutgoing();
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call originated event");
        }
        return call;
    }

    public SimpleCall DataUpdate(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call){
            String DialedNumber = arg.GetValueString(CtiOs_IKeywordIDs.CTIOS_DIALEDNUMBER);
            if( null != DialedNumber) {
                if( DialedNumber.length() > 10 ){
                    DialedNumber = DialedNumber.substring( DialedNumber.length() - 10 );
                }
                call.setNumber( DialedNumber );
                Manager.logger.trace("we are calling: "+DialedNumber);
            }
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call update event");
        }
        return call;
    }

    public SimpleCall callEstablished(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call ){
            call.onEstablished();
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call established event");
        }
        return call;
    }

    public SimpleCall callHold(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call){
            call.onHold();
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call hold event");
        }
        return call;
    }

    public SimpleCall callUnhold(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call){
            call.onUnhold();
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call unhold event");
        }
        return call;
    }

    public SimpleCall callClear(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call ){
            call.onCleared();
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call cleared event");
        }
        return call;
    }

    public void callEnd(Arguments arg){
        SimpleCall call = (SimpleCall)getCallByArgs(arg);
        if( null != call){
            call.onEnd();
            call.persist();
            this.CallPool.remove( call.getCallID() );
        } else {
            Manager.logger.warn("Unknown call \""+ call.getCallID() +"\" send call end event");
        }
    }

    public CompositeCall callTransferred(Arguments arg){
        //Arguments device = arg.GetElementKey(1);
        Arg device = arg.GetValue("ConnectedParty");

        Manager.logger.error("oioi: "+arg.NumElements());

        Integer i = 1;

        Arguments ConnectedParty;
        String callUniqID = getCallId(arg);
        CompositeCall TrasferredCall = new CompositeCall(client, "composite." + callUniqID);
        do{
            ConnectedParty = (Arguments)arg.GetValue("ConnectedParty["+i+"]");
            String number = ConnectedParty.GetValueString(CtiOs_IKeywordIDs.CTIOS_CONNECTEDPARTYDEVICEID);
            for (Map.Entry<String, CallInterface> entry : CallPool.entrySet()) {
                CallInterface call = entry.getValue();
                if( call instanceof SimpleCall && ((SimpleCall)call).getNumber().equals(number) ){
                    CompositeCall parent = ((SimpleCall)call).getCompositeCall();
                    if(null == parent){
                        TrasferredCall.addCall((SimpleCall)call);
                        ((SimpleCall)call).setParent(TrasferredCall);
                    } else {
                        TrasferredCall = parent;
                    }
                }
            }
            i++;
        } while( ConnectedParty != null );
        Manager.logger.error("Oi");
        return TrasferredCall;
    }
}
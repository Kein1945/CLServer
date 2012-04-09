/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway;

import CTI.Gateway.Manager;
import Client.Gateway.Packets.*;
import Daemon.Events.AgentStateEvent;
import Daemon.Events.CallEvent;
import com.cisco.cti.ctios.cil.Call;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 *
 * @author bsalmanov
 */
public class Client {
    
    protected boolean authorized = false;

    protected String Login;
    protected String Password;
    protected String Instrument;
    protected Integer Extension;
    
    protected Channel channel;
    protected Manager manager;

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        setAuthorized( true );
        this.manager = manager;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Client)) return false;
        Client c = (Client)obj;
        return hashCode() == c.hashCode();
                /*Login.equals(c.getLogin())
                && Password.equals(c.getPassword())
                && Instrument.equals(c.getInstrument())
                && Extension.equals(c.getExtension());*/
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.Login);
        hash = 29 * hash + Objects.hashCode(this.Password);
        hash = 29 * hash + Objects.hashCode(this.Instrument);
        hash = 29 * hash + Objects.hashCode(this.Extension);
        return hash;
    }
    
    public void setAuthorizationObject(AuthorizePacket packet){
        Login = packet.getLogin();
        Password = packet.getPassword();
        Instrument = packet.getInstrument();
        Extension = packet.getExtension();
    }
    
    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        AgentStateEvent e = new AgentStateEvent(Login, Client.State.LOGIN);
        Daemon.Server.events.proceedEvent(e);
    }

    public Client(Channel channel) {
        this.channel = channel;
    }   
    public Integer getExtension() {
        return Extension;
    }
    public String getInstrument() {
        return Instrument;
    }
    public String getLogin() {
        return Login;
    }
    public String getPassword() {
        return Password;
    }
    public void setExtension(Integer Extension) {
        this.Extension = Extension;
    }
    public void setInstrument(String Instrument) {
        this.Instrument = Instrument;
    }
    public void setLogin(String Login) {
        this.Login = Login;
    }
    public void setPassword(String Password) {
        this.Password = Password;
    }
    
    public void acceptPacket(Packet packet){
        if( packet instanceof SetStatePacket ){
            manager.setAgentState(((SetStatePacket)packet).getState());
            this.onState(((SetStatePacket)packet).getState());
        } else if (packet instanceof CallClearPacket){
            manager.clearCall();
        } else if (packet instanceof HoldPacket) {
            //throw new UnsupportedOperationException("Hold command must be realized");
            manager.holdCall();
            // Manager.GetCurrentCall()Hold()
        }
    }
    
    public synchronized void onState(Integer state){
        SetStatePacket p = new SetStatePacket();
        p.setState(state);
        channel.write(p);
        AgentStateEvent e = new AgentStateEvent(Login, state);
        Daemon.Server.events.proceedEvent(e);
        //channel.write(p);
    }
    
    public synchronized void onCommingDial(String IncommingNumber, String DeviceNumber){
        CallBeginPacket p = new CallBeginPacket();
        p.setDevice(DeviceNumber);
        p.setNumber(IncommingNumber);
        ChannelFuture write = channel.write(p);
        try {
            write.await(10000);
            Server.logger.trace("Looks like writed");
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void onWarning(String text){
        WarningPacket wp = new WarningPacket();
        wp.setMessage(text);
        channel.write(wp);
    }

    public void onCallClear(Call call) {
        CallEvent c = new CallEvent(call);
        Daemon.Server.events.proceedEvent(c);
    }
    
    public interface State {
        public static final int LOGIN = 0;
        public static final int LOGOUT = 1;
        public static final int NOT_READY = 2;
        public static final int AVIABLE = 3;
        public static final int TALKING = 4;
        public static final int WORK_NOT_READY = 5;
        public static final int WORK_READY = 6;
        public static final int BUSY_OTHER = 7;
        public static final int RESERVED = 8;
        public static final int UNKNOWN = 9;
        public static final int HOLD = 10;
        public static final int OFFLINE = 11; // Not cti state.
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway;

import Operator.Gateway.Packets.NotificationPacket;
import Operator.Gateway.Packets.SetStatePacket;
import Operator.Gateway.Packets.ButtonMaskPacket;
import Operator.Gateway.Packets.WarningPacket;
import Operator.Gateway.Packets.CallBeginPacket;
import Operator.Gateway.Packets.HoldPacket;
import Operator.Gateway.Packets.ErrorPacket;
import Operator.Gateway.Packets.CallEndPacket;
import Operator.Gateway.Packets.HelloPacket;
import Operator.Gateway.Packets.AuthorizePacket;
import Operator.Gateway.Packets.InfoPacket;
import CTI.Gateway.Manager;
import Daemon.Events.AgentStateEvent;
import Daemon.Events.CallEvent;
import com.cisco.cti.ctios.cil.Call;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 *
 * @author bsalmanov
 */
public class Client {

    protected static Map<Client, Manager> connections = new ConcurrentHashMap();

    
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
        this.manager = manager;
    }

    public boolean isAuthorized() {
        return authorized;
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

    public void acceptPacket(Packet packet) {
        if (packet instanceof SetStatePacket) {
            manager.setAgentState(((SetStatePacket) packet).getState());
            this.onState(((SetStatePacket) packet).getState());
        } else if (packet instanceof CallEndPacket) {
            manager.clearCall();
        } else if (packet instanceof HoldPacket) {
            //throw new UnsupportedOperationException("Hold command must be realized");
            manager.holdCall();
            // Manager.GetCurrentCall()Hold()
        }
    }

    public void onConnected() {
        channel.write(new HelloPacket());
    }
    
    public void onDisconnected(){
        removeClient(this);
    }

    public void onAgentMode(){
        //AuthorizePacket ap = new AuthorizePacket();
        manager.loginAgent();
        //ap.setCode( AuthorizePacket.AUTHORIZATION_OK );
        //channel.write( ap );
    }
    
    public void onLogin(){
        authorized = true;
        connections.put(this, this.manager);
        AuthorizePacket ap = new AuthorizePacket();
        ap.setCode( AuthorizePacket.AUTHORIZATION_OK );
        channel.write(ap);
        this.onState( manager.getAgentState() );
    }
    
    public void onLoginFail(String error){
        AuthorizePacket ap = new AuthorizePacket();
        ap.setReason(error);
        ap.setCode( AuthorizePacket.NOT_AUTHORIZED_YET );
        channel.write(ap);
    }
    
    public synchronized void onState(Integer state) {
        SetStatePacket p = new SetStatePacket();
        p.setState(state);
        channel.write(p);
        AgentStateEvent e = new AgentStateEvent(Login, state);
        Daemon.Server.events.proceedEvent(e);
        //channel.write(p);
    }

    public synchronized void onCommingDial(String IncommingNumber, String DeviceNumber) {
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

    public synchronized void onButtonEnablementMaskChange(Integer mask) {
        ButtonMaskPacket packet = new ButtonMaskPacket();
        packet.setBitmask(mask);
        channel.write(packet);
    }

    public void onCallClear(Call call) {
        CallEvent c = new CallEvent(call);
        Daemon.Server.events.proceedEvent(c);
    }

    public void sendError(String text) {
        sendNotification(new ErrorPacket(), text);
        manager.disconnect();
    }
    public void sendWarning(String text) {
        sendNotification(new WarningPacket(), text);
    }
    public void sendInfo(String text) {
        sendNotification(new InfoPacket(), text);
    }
    private void sendNotification(NotificationPacket notify, String message) {
        notify.setMessage(message);
        channel.write(notify);
    }
    
    
    
    public static Map<Client, Manager> getClients(){
        return connections;  // static Map<Client, Manager> connections = new ConcurrentHashMap()
    }
    
    public static Manager removeClient(Client client){
        return connections.remove(client);
    }
    
    
    /**
     * 
     * Метод используется для получения клиента по имени, если он активен
     * @param clientLogin
     * @return 
     */
    public static Manager getClient(String clientLogin){
        for (Map.Entry<Client, Manager> entry : connections.entrySet()) {
            Client client = entry.getKey();
            if( clientLogin.toLowerCase().equals(client.getLogin().toLowerCase()) ){
                return (Manager) entry.getValue();
            }
        }
        return null;
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

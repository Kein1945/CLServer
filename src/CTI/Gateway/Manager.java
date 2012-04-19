/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Gateway;

import CTI.Call.CallInterface;
import CTI.Call.SimpleCall;
import Operator.Gateway.Client;
import com.cisco.cti.ctios.cil.Agent;
import com.cisco.cti.ctios.cil.Arguments;
import com.cisco.cti.ctios.cil.Call;
import com.cisco.cti.ctios.cil.CtiOs_Enums;
import com.cisco.cti.ctios.util.CtiOs_IKeywordIDs;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class Manager{
    public static final Logger logger = Logger.getLogger(Manager.class);
    // Все объекты класса всех подключенных операторов
    protected Properties conf;
    
    protected Map<String, CallInterface> CallPool = new ConcurrentHashMap<>();
    protected Client client;
    protected Connection connect;
    // Индикация что клиент авторизова на CTI сервере
    protected boolean ready = false;

    public Manager(Client client){
        this.conf = Daemon.Server.conf;
        this.client = client;
        this.connect = new Connection( this );
    }
    
    public boolean connect(){
        return this.connect.connect(
                this.conf.getProperty("cti.HostA","192.168.242.100"),
                Integer.parseInt( this.conf.getProperty("cti.PortA","42028") ),
                this.conf.getProperty("cti.HostB","192.168.242.101"),
                Integer.parseInt( this.conf.getProperty("cti.PortB","42028") ),
                Integer.parseInt( this.conf.getProperty("cti.HearBeat","0") )
            );
    }
    
    public void disconnect(){
        if( null != this.connect.getAgent())
            this.connect.getAgent().Logout(null);
        this.connect.disconnect();
    }
    
    public boolean setAgentMode(String login, String pasword, String instrument, Integer extension){
        return this.connect.setAgentMode(login, pasword, instrument, extension);
    }
    
    public void loginAgent(){
        connect.loginAgent();
    }
    
    public void onConnected(){
        client.onConnected();
    }
    
    public boolean isConnected(){
        return this.connect.isConnected();
    } // boolean isConnected
    
    public void onAgentMode(){
        client.onAgentMode();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
    public Integer getAgentState(){
        return this.getAgent().GetAgentState();
    }
        
    public synchronized boolean isAgentAuthorized() {
        int iCurrentState = getAgent().GetAgentState();
        return !( iCurrentState == CtiOs_Enums.AgentState.eUnknown || iCurrentState == CtiOs_Enums.AgentState.eLogout);

    } // isAgentLoggedIn
    
    /**
     * Вызывается из класса Connection при событии смены состояния оператора
     * @param state 
     */
    public void onAgentState(Integer state){
        client.onState(state);
    }
    
    /**
     * Устанвливает состояние оператора
     * @param state 
     */
    public synchronized void setAgentState(Integer state){
        this.connect.setAgentState(state);
    }
    
    /**
     * Возвращает родного агента класса пакета CTI
     * @return 
     */
    protected Agent getAgent(){
        return connect.getAgent();
    }
    
    public void buttonEnablementMask(Integer mask){
        client.onButtonEnablementMaskChange(mask);
    }
    /**
     * Прерывает текущий звонок
     */
    public void clearCall(){
        Call c = this.connect.getSession().GetCurrentCall();
    }
    /**
     * 
     */
    public void holdCall() {
        this.connect.getSession().GetCurrentCall().Hold(new Arguments());
    }
    
    /**
     * Совершает инициацию звонка для подключенного оператора
     * @param number 
     */
    public void makeCall(String number){
        Arguments rReqArgs = new Arguments();
                    // Set the dialed number
        logger.info("Make call: "+client.getLogin()+" - "+number);
        rReqArgs.SetValue( CtiOs_IKeywordIDs.CTIOS_DIALEDNUMBER, number );
        getAgent().MakeCall(rReqArgs);
    }
    
    /**
     * Вызывается при не критической ошибке на CTI сервере, что бы сообщить что-то клиенту
     * @param text 
     */
    protected synchronized void onWarning(String text){
        client.sendWarning(text);
    }
    
    /**
     * Вызывается классом Connection при событии начала звонка
     * @param DialNumber - номер с которого звонят
     * @param DeviceId - внутренний номер оператора
     */
    public synchronized void onCallBegin(Arguments rArgs){
        String Number = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_ANI);
        String DeviceId = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_DEVICEID);
        //currentCallType = rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_CALLTYPE);
        String callUniqID = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_UNIQUEOBJECTID);
        CallInterface call = new SimpleCall( client.getLogin(), callUniqID );
        CallPool.put( callUniqID, call);
        if( null != Number){
            ((SimpleCall)call).setNumber(Number);
            ((SimpleCall)call).setIncome();
            ((SimpleCall)call).onBegin();
            logger.info("Incomming dial: " + Number + " > " + DeviceId);
            client.onCommingDial(Number, DeviceId);   
        }
    }

    
    void onCallClear(Arguments rArgs) {
        SimpleCall call = (SimpleCall)getCallByArgs(rArgs);
        if( null != call ){
            call.onCleared();
            Daemon.Server.events.proceedEvent( call );
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call cleared event");
        }
        //client.onCallClear(c);
        client.onState( getAgentState() );
    }
    
    public void onError(String text){
        client.sendError(text);
    }

    void onPostLogout() {
        //:TODO Отправить клиенту уведомление о logout
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void onLoginFail(String error) {
        client.onLoginFail(error);
    }

    void onLogin(){
        if( isAgentAuthorized() ){
            if(getAgentState() == CtiOs_Enums.AgentState.eLogout){
                //setAgentState( CtiOs_Enums.AgentState.eNotReady );
            }
            client.onLogin();
        } else
            client.onLoginFail("Please connect phone");
    }

    public void answerCall() {
        this.connect.getSession().GetCurrentCall().Answer( new Arguments() );
    }

    void onCallEstablished(Arguments rArgs) {
        SimpleCall call = (SimpleCall)getCallByArgs(rArgs);
        if( null != call ){
            call.onEstablished();
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call established event");
        }
        client.onCallEsablishedEvent();
    }
    
    protected CallInterface getCallByArgs(Arguments rArgs){
        String uniqID = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_UNIQUEOBJECTID);
        return getCallByID(uniqID);
    }
    
    public CallInterface getCallByID(String uniqID){
        if( CallPool.containsKey(uniqID) ){
            return CallPool.get( uniqID );
        } else {
            return null;
        }
    }

    public void releaseCall() {
        this.connect.getSession().GetCurrentCall().ClearConnection( new Arguments() );
    }

    void onUnheld(Arguments rArgs) {
        SimpleCall call = (SimpleCall)this.getCallByArgs(rArgs);
        if( null != call){
            call.onUnhold();
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call unhold event");
        }
        this.client.onUnheld();
    }

    public void unholdCall() {
        this.connect.getCurrentCall().Retrieve(new Arguments());
    }

    void onHold(Arguments rArgs) {
        SimpleCall call = (SimpleCall)this.getCallByArgs(rArgs);
        if( null != call){
            call.onHold();
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call hold event");
        }
        this.client.onHold();
    }

    void onCallEnd(Arguments rArgs) {
        SimpleCall call = (SimpleCall)this.getCallByArgs(rArgs);
        if( null != call){
            call.onEnd();
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call end event");
        }
    }

    void onOriginated(Arguments rArgs) {
        SimpleCall call = (SimpleCall)this.getCallByArgs(rArgs);
        if( null != call){
            ((SimpleCall)call).setOutgoing();
        } else {
            logger.warn("Unknown call \""+ call.getCallID() +"\" send call originated event");
        }
    }
    
    public class FailedToConnectException extends Exception{}
    public class FailToAuthorizeException extends Exception{

        public FailToAuthorizeException(String message) {
            super(message);
        }
        
    }
    
    public interface State{
        public static final Integer AGENT_AUTHORIZATION_SUCCESS = 0;
        public static final Integer SET_AGENT_STATE = 0;
    }
}

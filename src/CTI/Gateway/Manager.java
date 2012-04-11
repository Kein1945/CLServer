/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Gateway;

import Client.Gateway.Client;
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
    protected static Map<Client, Manager> connections = new ConcurrentHashMap();
    protected Properties conf;
    
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
//        if( null != c && ((CtiOs_Enums.ButtonEnablement.ENABLE_RELEASE & connect.getButtonEnablementMask()) > 0)){
//            c.ClearConnection( new Arguments() );
//        }
    }
    /**
     * 
     */
    public void holdCall() {
        Call c = this.connect.getSession().GetCurrentCall();
//        if( null != c && ((CtiOs_Enums.ButtonEnablement.ENABLE_HOLD & connect.getButtonEnablementMask()) > 0)){
//            c.Hold( new Arguments() );
//        }
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
    public synchronized void onCallBegin(String DialNumber, String DeviceId){
        logger.info("Incomming dial: " + DialNumber + " > " + DeviceId);
        client.onCommingDial(DialNumber, DeviceId);
    }
    
    /**
     * 
     * Возвращает объект менеджер подключения, либо генерирует исключения
     * @param client
     * @return
     * @throws CTI.Gateway.Manager.FailedToConnectException
     * @throws CTI.Gateway.Manager.FailToAuthorizeException 
     */
    public static Manager getManager(Client client) throws FailedToConnectException, FailToAuthorizeException{
        Manager m = (Manager)connections.get(client); // static Map<Client, Manager> connections = new ConcurrentHashMap()
        if( null == m){
            m = new Manager(client);
//            m.identifyClient();
            connections.put(client, m);
        }
        return m;
    }
    
    /**
     * 
     * Метод используется для получения менеджера довервенными лицами, то есть для администраторов
     * @param clientLogin
     * @return 
     */
    public static Manager getManager(String clientLogin){
        for (Map.Entry<Client, Manager> entry : connections.entrySet()) {
            Client client = entry.getKey();
            if( clientLogin.toLowerCase().equals(client.getLogin().toLowerCase()) ){
                return (Manager) entry.getValue();
            }
        }
        return null;
    }
    
    public static Map<Client, Manager> getManagers(){
        return connections;  // static Map<Client, Manager> connections = new ConcurrentHashMap()
    }
    
    public static Manager removeManager(Client client){
        return connections.remove(client);
    }
    
    void onCallClear(Call c) {
        client.onCallClear(c);
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
        if( isAgentAuthorized() )
            client.onLogin();
        else
            client.onLoginFail("Please connect phone");
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

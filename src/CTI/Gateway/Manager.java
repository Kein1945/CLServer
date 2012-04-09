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
    
    public synchronized void connect() throws FailedToConnectException{
        try{
            this.connect.connect(
                    this.conf.getProperty("cti.HostA","192.168.242.100"),
                    Integer.parseInt( this.conf.getProperty("cti.PortA","42028") ),
                    this.conf.getProperty("cti.HostB","192.168.242.101"),
                    Integer.parseInt( this.conf.getProperty("cti.PortB","42028") ),
                    Integer.parseInt( this.conf.getProperty("cti.HearBeat","0") ));
        } catch (Connection.FailedToConnectException fail){
            throw new FailedToConnectException();
        }
        try {
            wait(4000); // Waiting events from ctios. If no event - connection fail
        } catch (InterruptedException ignored) {
        } finally {
            if( ! this.connect.isConnected() )
                throw new FailedToConnectException();
        }
    }
    
    public synchronized void disconnect(){
        this.connect.disconnect();
    }
    
    public synchronized void onConnected(){
        notify();
    }
    
    public boolean isConnected(){
        return this.connect.isConnected();
    } // boolean isConnected

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
    public Integer getAgentState(){
        return this.getAgent().GetAgentState();
    }

    /**
     * Попытка авторизации с существующим клиентом
     * @throws CTI.Gateway.Manager.FailToAuthorizeException 
     */
    public void authorize() throws FailToAuthorizeException{
        if( ! this.connect.isConnected() ) // Так не должно случаться, но мы это проверим
            logger.error("Try to authorize without connection");
        String error = connect.setAgent(client.getLogin(), client.getPassword(), client.getInstrument(), client.getExtension());
        if( !this.isAgentAuthorized() ){
            throw new FailToAuthorizeException(error);
        }
        ready = true;
    }
    
    /**
     * Пытается авторизоваться либо выбрасывает исключения
     * @throws CTI.Gateway.Manager.FailedToConnectException
     * @throws CTI.Gateway.Manager.FailToAuthorizeException 
     */
    private synchronized void identifyClient() throws FailedToConnectException, FailToAuthorizeException {
        if( ! this.connect.isConnected() )
            this.connect();
        this.authorize();
    }
    
    public synchronized boolean isAgentAuthorized() {
        int iCurrentState = getAgent().GetAgentState();
        return !( iCurrentState == CtiOs_Enums.AgentState.eUnknown);

    } // isAgentLoggedIn
    
    /**
     * Вызывается из класса Connection при событии смены состояния оператора
     * @param state 
     */
    public void onAgentState(Integer state){
        if( ready )
            client.onState(state);
        else
            logger.trace("On agent without tt");
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
    /**
     * Прерывает текущий звонок
     */
    public void clearCall(){
        Call c = this.connect.getSession().GetCurrentCall();
        if( null != c && ((CtiOs_Enums.ButtonEnablement.ENABLE_RELEASE & connect.getButtonEnablementMask()) > 0)){
            c.ClearConnection( new Arguments() );
        }
    }
    /**
     * 
     */
    public void holdCall() {
        Call c = this.connect.getSession().GetCurrentCall();
        if( null != c && ((CtiOs_Enums.ButtonEnablement.ENABLE_HOLD & connect.getButtonEnablementMask()) > 0)){
            c.Hold( new Arguments() );
        }
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
        client.onWarning(text);
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
            m.identifyClient();
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
    
    @Override
    protected void finalize() throws Throwable {
        this.connect.disconnect(); // Обязательно закроем подключение, возможно еще надо слать изменение статуса на NotReady
        super.finalize();
    }


    void onCallClear(Call c) {
        client.onCallClear(c);
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

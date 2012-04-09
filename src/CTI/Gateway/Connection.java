/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Gateway;

import com.cisco.cti.ctios.cil.*;
import com.cisco.cti.ctios.util.CtiOs_IKeywordIDs;
import org.apache.log4j.Logger;

/**
 *
 * @author kein
 */
public class Connection implements IGenericEvents {
    public static final Logger logger = Logger.getLogger(Connection.class);
    
    private CtiOsSession session;
    private Boolean isConnected = false;
    protected String currentCall = null;
    private long buttonEnablementMask = 0;
    protected String currentCallDeviceId;
    protected String currentCallNumber;
    protected Integer currentCallType;
    protected boolean setAgentState = false;
    protected boolean Authorization = false;
    
    private Manager manager;
    private String lastError = "OK";

    public long getButtonEnablementMask() {
        return buttonEnablementMask;
    }

    public Call getCurrentCall() {
        Call c = session.GetCurrentCall();
        if(null == c)
            c = (Call)session.GetObjectFromObjectID( currentCall );
        Manager.logger.trace("Return call object "+c.toString());
        return c;
    }

    public Connection(Manager manager) {
        this.manager = manager;
    }

    public CtiOsSession getSession() {
        return session;
    }
    
    public void connect(String HostA, int PortA, String HostB, int PortB, int HeartBeat) throws FailedToConnectException{
        session = new CtiOsSession();
        if (session == null) {
            // Проблемы
            //:TODO Some error notification for client
            logger.error("Failed to create CtiOsSession.");
            throw new FailedToConnectException();
        }
        if(session.AddEventListener(this, CtiOs_Enums.SubscriberList.eAllInOneList) < 0){
            // Проблемы на сервере, надо бы сообщить тут что-то клиенту
            logger.error("Failed to add subscriber for AllInOne events.");
            throw new FailedToConnectException();
        }
        Integer iRetVal;
        // Open the connection
        Arguments rArgSessionConn   = new Arguments();
        // Set the connection info in the Arguments
        rArgSessionConn.SetValue( CtiOs_IKeywordIDs.CTIOS_CTIOSA, HostA );
        rArgSessionConn.SetValue( CtiOs_IKeywordIDs.CTIOS_PORTA, PortA );
        rArgSessionConn.SetValue( CtiOs_IKeywordIDs.CTIOS_CTIOSB, HostB );
        rArgSessionConn.SetValue( CtiOs_IKeywordIDs.CTIOS_PORTB, PortB );
        rArgSessionConn.SetValue( CtiOs_IKeywordIDs.CTIOS_HEARTBEAT, HeartBeat );
        logger.trace("Connecting to CTIOS Server...");
        iRetVal = session.Connect(rArgSessionConn);

        if (iRetVal < 1) {
            logger.error("Session->Connect to CTIOS Server Failed");
            throw new FailedToConnectException();
        }
    }
    
    public boolean isConnected(){
        return isConnected;
    }
    
    /**
     * Авторизация на сервере
     * @param login
     * @param password
     * @param instrument
     * @param extension 
     */
    public synchronized String setAgent(String login, String password, String instrument, Integer extension ) {
        Agent agent = new Agent();
        agent.SetValue( CtiOs_IKeywordIDs.CTIOS_LOGINNAME, login);
        agent.SetValue( CtiOs_IKeywordIDs.CTIOS_AGENTPASSWORD, password);
        agent.SetValue( CtiOs_IKeywordIDs.CTIOS_AGENTINSTRUMENT, instrument);
        agent.SetValue( CtiOs_IKeywordIDs.CTIOS_PERIPHERALID, extension);
        agent.SetValue( CtiOs_IKeywordIDs.CTIOS_AUTOLOGIN, 1 );
        //logger.trace("Set agent mode: " + instrument +" /" + agent.toString());
        session.SetAgent( agent );
        logger.trace("Waiting authentication...");
        try{
            wait();
        } catch ( InterruptedException ignored ){}
        logger.trace("Set agent mode succesful");
        if( !setAgentState ){
            return lastError;
        }
        Arguments rArgs = new Arguments();
        rArgs.SetValue( CtiOs_IKeywordIDs.CTIOS_AGENTSREADY, 1 );
        session.GetCurrentAgent().Login(rArgs);
        logger.trace("Agent authentication...");
        try{
            wait();
        } catch ( InterruptedException ignored ){/*ignored.printStackTrace();*/}
        return null;
        //logger.trace("Authentication ready");
    } // setAgent
    
    /**
     * Закрытие сессии подклчюения к CTI
     */
    public void disconnect(){
        if( false == this.isConnected ) return;
        Arguments rWaitArgs = new Arguments();
        rWaitArgs.SetValue("Event1", CtiOs_Enums.EventID.eOnConnectionClosed);
        WaitObject rWaitObj = session.CreateWaitObject(rWaitArgs);
        session.Disconnect( new Arguments() );
        // Wait for OnConnectionClosed for 5 seconds
        rWaitObj.WaitOnMultipleEvents(5);
        // Allow the Session to clean up the wait object
        session.DestroyWaitObject(rWaitObj);
        session.RemoveEventListener(this, CtiOs_Enums.SubscriberList.eAllInOneList);
    }
    
    public Agent getAgent(){
        return session.GetCurrentAgent();
    }
    
    protected synchronized void onSetAgentMode(){
        notify();
    }
    
    /**
     * Отправка нового состояния оператора на CTI
     * @param state 
     */
    public void setAgentState(Integer state){
        Arguments rReqArgs = new Arguments();
        rReqArgs.SetValue( CtiOs_IKeywordIDs.CTIOS_AGENTSTATE, state);
        rReqArgs.SetValue( CtiOs_IKeywordIDs.CTIOS_EVENTREASONCODE, "1234");
        
        getAgent().SetAgentState(rReqArgs);
    }
    
    /**
     * Метод вызываемый при получении нового события с CTI сервера
     * @param iEventID
     * @param rArgs 
     */
    @Override
    public void OnEvent(int iEventID, Arguments rArgs) {
        //if(iEventID != CtiOs_Enums.EventID.eButtonEnablementMaskChange)
            logger.trace(">>> cti event " + CtiOs_EnumStrings.EventIDToString(iEventID) + " | " + rArgs.toString());
        Integer IState;
        Call c;
        switch (iEventID) {
            // Подключение к серверу CTI
            case CtiOs_Enums.EventID.eOnConnection:
                isConnected = true;
                manager.onConnected();
                break;
            case CtiOs_Enums.EventID.eOnConnectionClosed:
                //OnConnectionClosed(rArgs);
                break;
            case CtiOs_Enums.EventID.eOnConnectionFailure:
                break;
            case CtiOs_Enums.EventID.eCTIOSFailureEvent:
                if( null != rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_ERRORMESSAGE ) ){
                    logger.error("CTIOS Server event failure: "+rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_ERRORMESSAGE ) + " / " + rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_FAILURECODE ));
                    if( CtiOs_Enums.FailureCode.eRequiredArgMissing == rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_FAILURECODE ) ){
                        
                        this.onSetAgentMode();
                        lastError = rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_ERRORMESSAGE );
                    } else if( CtiOs_Enums.FailureCode.eAgentAlreadyLoggedIn == rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_FAILURECODE ) 
                            || CtiOs_Enums.FailureCode.eInconsistentAgentData == rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_FAILURECODE)){
                        
                        lastError = rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_ERRORMESSAGE );
                        manager.onWarning(lastError);
                        manager.onAgentState( this.getAgent().GetAgentState() );
                    }
                }
                //OnCtiosFailure(rArgs);
                break;
            case CtiOs_Enums.EventID.eControlFailureConf:
                manager.onWarning( rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_ERRORMESSAGE ) );
                break;
            case CtiOs_Enums.EventID.eQueryAgentStateConf:
                IState = rArgs.GetValueIntObj( CtiOs_IKeywordIDs.CTIOS_AGENTSTATE );
                //if( null!=IState )
                    //manager.onAgentState( IState.intValue() );
                this.onSetAgentMode();
                break;
            case CtiOs_Enums.EventID.eSetAgentModeEvent:
                setAgentState = true;
                this.onSetAgentMode();
                //server.Server.log("Agent authorization ok")
                break;
            case CtiOs_Enums.EventID.eSetAgentStateConf:
            case CtiOs_Enums.EventID.eAgentStateEvent:
                IState = rArgs.GetValueIntObj( CtiOs_IKeywordIDs.CTIOS_AGENTSTATE );
                Integer er = rArgs.GetValueIntObj( CtiOs_IKeywordIDs.CTIOS_EVENTREASONCODE );
                if( null!=IState ){
                    manager.onAgentState( IState.intValue() );
                }
                break;
            case CtiOs_Enums.EventID.eButtonEnablementMaskChange:
                Long LMask = rArgs.GetValueUIntObj( CtiOs_IKeywordIDs.CTIOS_ENABLEMENTMASK );
		if (null==LMask)
			return;

		buttonEnablementMask = LMask.longValue();
                //CtiOs_Enums.ButtonEnablement.
                //m_appFrame.m_btnAnswer.setEnabled(       ((bitMask & ENABLE_ANSWER)                 > 0));
                //m_appFrame.m_btnConference.setEnabled(   ((bitMask & ENABLE_CONFERENCE_COMPLETE)    > 0));
                //m_appFrame.m_btnCCConference.setEnabled( ((bitMask & ENABLE_CONFERENCE_INIT)        > 0));
                //m_appFrame.m_btnHold.setEnabled(         ((bitMask & ENABLE_HOLD)                   > 0));
                //m_appFrame.m_btnLogin.setEnabled(        ((bitMask & ENABLE_LOGIN)                  > 0));
                //m_appFrame.m_btnLogout.setEnabled(       ((bitMask & (ENABLE_LOGOUT | CtiOs_Enums.ButtonEnablement.ENABLE_LOGOUT_WITH_REASON)) > 0));
                //m_appFrame.m_btnMakeCall.setEnabled(     ((bitMask & ENABLE_MAKECALL)               > 0));
                //m_appFrame.m_btnNotReady.setEnabled(     ((bitMask & (ENABLE_NOTREADY | CtiOs_Enums.ButtonEnablement.ENABLE_NOTREADY_WITH_REASON)) > 0));
                //m_appFrame.m_btnReady.setEnabled(        ((bitMask & ENABLE_READY)                  > 0));
                //m_appFrame.m_btnRelease.setEnabled(      ((bitMask & ENABLE_RELEASE)                > 0));
                //m_appFrame.m_btnRetrieve.setEnabled(     ((bitMask & ENABLE_RETRIEVE)               > 0));
                //m_appFrame.m_btnSSTransfer.setEnabled(   ((bitMask & ENABLE_SINGLE_STEP_TRANSFER)   > 0));
                //m_appFrame.m_btnSSConference.setEnabled( ((bitMask & ENABLE_SINGLE_STEP_CONFERENCE) > 0));
                //m_appFrame.m_btnTransfer.setEnabled(     ((bitMask & ENABLE_TRANSFER_COMPLETE)      > 0));
                //m_appFrame.m_btnCCTransfer.setEnabled(   ((bitMask & ENABLE_TRANSFER_INIT)          > 0));
                break;
            case CtiOs_Enums.EventID.eCallOriginatedEvent:
                String DeviceID = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_DEVICEID);
                currentCallType = rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_CALLTYPE);
                if( null != DeviceID){
                     currentCallNumber = DeviceID;
                }
                break;
            case CtiOs_Enums.EventID.eCallBeginEvent:
                //logger.trace("Call Event!");
                String Number = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_ANI);
                String DeviceId = rArgs.GetValueString(CtiOs_IKeywordIDs.CTIOS_DEVICEID);
                currentCallType = rArgs.GetValueIntObj(CtiOs_IKeywordIDs.CTIOS_CALLTYPE);
                if( null != Number){
                    manager.onCallBegin( Number, DeviceId );
                }
                break;
            case CtiOs_Enums.EventID.eCallDeliveredEvent:
                currentCall = rArgs.GetValueString( CtiOs_IKeywordIDs.CTIOS_UNIQUEOBJECTID);
                break;
            case CtiOs_Enums.EventID.eCallEstablishedEvent:
                //manager.getClientAgent().callStart();
                c = session.GetCurrentCall();
                if( null != c ){
                    Arguments rRequestArgs = new Arguments();
                    Arguments args = c.GetValueArray(CtiOs_IKeywordIDs.CTIOS_ECC);
                    if( null == args){
                        args = new Arguments();
                    }
                    args.SetValueUInt("call.start", System.currentTimeMillis());
                    args.SetValueUInt("call.duration", 0L);
                    rRequestArgs.SetValue(CtiOs_IKeywordIDs.CTIOS_ECC, args);
                }
                break;
            case CtiOs_Enums.EventID.eCallRetrievedEvent:
            case CtiOs_Enums.EventID.eRTPStartedEvent:
                //manager.getClientAgent().callResume();
                
                break;
            case CtiOs_Enums.EventID.eRTPStoppedEvent:
                //manager.getClientAgent().callHold();
                c = session.GetCurrentCall();
                if( null != c ){
                    Arguments args = c.GetValueArray(CtiOs_IKeywordIDs.CTIOS_ECC);
                    if( null != args){
                        Arguments rRequestArgs = new Arguments();
                        args = new Arguments();
                        args.SetValueUInt("call.duration",args.GetValueUIntObj("call.duration")+ (System.currentTimeMillis() - args.GetValueUIntObj("call.start") )/1000L );
                        rRequestArgs.SetValue(CtiOs_IKeywordIDs.CTIOS_ECC, args);
                    }
                }
                c = session.GetCurrentCall();
                if( null == c){
                    c = (Call)session.GetObjectFromObjectID( currentCall );
                }
                    Arguments args = c.GetValueArray(CtiOs_IKeywordIDs.CTIOS_ECC);
                    if( null != args){
                        Arguments rRequestArgs = new Arguments();
                        args = new Arguments();
                        args.SetValueUInt("call.duration",args.GetValueUIntObj("call.duration")+ (System.currentTimeMillis() - args.GetValueUIntObj("call.start") )/1000L );
                        rRequestArgs.SetValue(CtiOs_IKeywordIDs.CTIOS_ECC, args);
                    }
                    manager.onCallClear( c );
                break;
            case CtiOs_Enums.EventID.eCallConnectionClearedEvent:
            case CtiOs_Enums.EventID.eCallClearedEvent:
                //int callDuration = manager.getClientAgent().callEnd();
                //server.Server.LOG.trace("Call duration was '"+callDuration+"' seconds");
                //this.manager.onCommand(new LogCallDurationEvent(callDuration, manager.getClientAgent().getId() ));
                break;
            case CtiOs_Enums.EventID.eCallEndEvent:
                break;
            case CtiOs_Enums.EventID.ePostLogoutEvent:
                // Call Post processing
                manager.disconnect();
                break;
            case CtiOs_Enums.EventID.eCallFailedEvent:;
                break;
            default:
                logger.trace(">? cti event " + CtiOs_EnumStrings.EventIDToString(iEventID));
        }
    }
    
    
    public class FailedToConnectException extends Exception{}
    public class FailedToAuthorizeException extends Exception{

        public FailedToAuthorizeException(String message) {
            super(message);
        }
    }
}
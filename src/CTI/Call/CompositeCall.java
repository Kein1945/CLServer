/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import Daemon.EventHandler;
import Daemon.Events.PersistableInterface;
import Operator.Gateway.Client;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author bsalmanov
 */
public class CompositeCall extends AbstractCall implements CallInterface, PersistableInterface{

    protected volatile Map<String, CallInterface> CallPool = new ConcurrentHashMap<>();
    protected Integer callDBID;
    private boolean saved = false;

    public CompositeCall(Client client, String callID) {
        super(client, callID);
    }

    public void setStranferType(){
        this.type = this.type | CallInterface.TRANSFER;
    }

    public void addCall(SimpleCall call){
        CallPool.put( call.getCallID() , call);
        if( 0 == getStartTime() || getStartTime() > call.getStartTime() ){
            this.startTime = call.getStartTime();
        }
    }

    public void setTransfer(){
        this.type = this.type | CallInterface.TRANSFER;
    }

    public Integer getId(){
        return this.callDBID;
    }

    @Override
    public void proceed(Connection dbConnection) {
        if( saved ){
            return;
        }
        boolean AllCallsEnded = false;
        for (Map.Entry<String, CallInterface> entry : CallPool.entrySet()) {
            String string = entry.getKey();
            SimpleCall call = (SimpleCall)entry.getValue();
            AllCallsEnded |= call.isEnded();
        }

        if( AllCallsEnded ){
            this.complete();
            try {
                Statement s = dbConnection.createStatement();
                //:TODO Userid from login, phone insert and ID set to phone field
                s.executeUpdate( String.format("INSERT INTO `call` (`id`, `uid`, `parent`, `start`, `length`, `type`, `phone`) VALUES (NULL, (SELECT userID FROM `chimaera`.`users` WHERE `domainName` = '%s' LIMIT 0,1), '0', '%d', '%d', '%d', '0');"
                        , getAgent(), getStartTime(), getDuration(), getType())
                        ,Statement.RETURN_GENERATED_KEYS
                );
                ResultSet rs = s.getGeneratedKeys();
                if (!rs.next()){
                    return;
                }
                callDBID = rs.getInt(1);
                s.close();
                for (Map.Entry<String, CallInterface> entry : CallPool.entrySet()) {
                    SimpleCall call = (SimpleCall)entry.getValue();
                    call.proceed(dbConnection);
                }
            } catch (SQLException ex) {
                EventHandler.logger.error(ex.toString());
            } catch( Exception e){
                e.printStackTrace();
            }
        }
        saved = true;
    }
}

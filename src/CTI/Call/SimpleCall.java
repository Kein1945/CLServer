/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import Daemon.EventHandler;
import Daemon.Events.PersistableInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bsalmanov
 */
public class SimpleCall extends AbstractCall implements CallInterface, PersistableInterface{
    
    protected String number;
    protected volatile List<TimelineEvent> timeline = new ArrayList<>();

    public SimpleCall(String agentLogin, String callID) {
        super(agentLogin, callID);
    }

    public void setIncome(){
        this.type = this.type | CallInterface.INCOME;
    }
    
    public void setOutgoing(){
        this.type = this.type | CallInterface.OUTGOING;
    }
    
    public void setAccepted(){
        this.type = this.type | CallInterface.ACCEPTED;
    }
  
    public void setNumber(String number) {
        this.number = number;
    }
    
    public void onBegin(){
        timeline.add( TimelineEvent.begin() );
    }
    
    public void onEstablished(){
        started();
        timeline.add( TimelineEvent.established() );
        this.setAccepted();
    }
    
    public void onCleared(){
        timeline.add( TimelineEvent.clear() );
        complete();
    }
    
    public void onEnd(){
        timeline.add( TimelineEvent.end() );
    }

    @Override
    public void proceed(Connection dbConnection) {
        try {
             Statement s = dbConnection.createStatement();
            //:TODO Userid from login, phone insert and ID set to phone field
            s.executeUpdate(String.format(
                    "INSERT INTO `plan_msec`.`phones`(`phone`,`relation`)VALUES('%s', 'contact') ON DUPLICATE KEY UPDATE ID=LAST_INSERT_ID(ID);"
                    , number
                    ));
            s.executeUpdate( String.format("INSERT INTO `call` (`id`, `uid`, `start`, `length`, `type`, `phone`) VALUES (NULL, (SELECT userID FROM `chimaera`.`users` WHERE `domainName` = '%s' LIMIT 0,1), '%d', '%d', '%d', (SELECT LAST_INSERT_ID()));"
                    , agent, getStartTime(), getDuration(), getType())
                    ,Statement.RETURN_GENERATED_KEYS
            );
            ResultSet rs = s.getGeneratedKeys();
            if (!rs.next()){
                return;
            }
            Integer callDBID = rs.getInt(1);
            EventHandler.logger.trace( "Insert id: " + callDBID );
            s.close();
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO `call_timeline` (`id`, `call`, `type`, `start`) VALUES (NULL, '"+callDBID+"', ?, ?);");
            for (int i = 0; i < timeline.size(); i++) {
                TimelineEvent te = timeline.get(i);
                ps.setInt(1, te.getType());
                ps.setInt(2, te.getStart());
                ps.addBatch();
            }
            ps.executeBatch();
            EventHandler.logger.trace("Simple call proceed");
        } catch (SQLException ex) {
            EventHandler.logger.error(ex.toString());
        } catch( Exception e){
            e.printStackTrace();
        }
    }

    public void onHold() {
        timeline.add( TimelineEvent.hold() );
    }

    public void onUnhold() {
        timeline.add( TimelineEvent.unhold() );
    }
}

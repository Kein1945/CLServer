/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import Daemon.EventHandler;
import Daemon.Events.PersistableInterface;
import Operator.Gateway.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bsalmanov
 */
public class SimpleCall extends AbstractCall implements CallInterface, PersistableInterface{

    protected String number;
    protected String device;
    protected CompositeCall Parent;
    protected volatile List<TimelineEvent> timeline = new ArrayList<>();
    private boolean ended = false;
    private boolean saved = false;

    public boolean isEnded() {
        return ended;
    }

    public SimpleCall(Client client, String callID) {
        super(client, callID);
    }

    public void setIncome(){
        this.type = this.type | CallInterface.INCOME;
    }

    public boolean isIncome(){
        return (this.type & CallInterface.INCOME) > 0;
    }

    public void setOutgoing(){
        this.type = this.type | CallInterface.OUTGOING;
    }
    public boolean isOutgoing(){
        return (this.type & CallInterface.OUTGOING) > 0;
    }

    public void setAccepted(){
        this.type = this.type | CallInterface.ACCEPTED;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber(){
        return this.number;
    }

    public void setDevice(String device){
        this.device = device;
    }

    public String getDevice(){
        return this.device;
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
        this.ended = true;
    }

    public CompositeCall getCompositeCall(){
        return this.Parent;
    }

    public void setParent(CompositeCall parent){
        this.Parent = parent;
    }

    @Override
    public void proceed(Connection dbConnection) {
        if( !saved ){
            if( !this.isComplete() ){
                this.complete();
            }
            try {
                Statement s = dbConnection.createStatement();
                //:TODO Userid from login, phone insert and ID set to phone field
                s.executeUpdate(String.format(
                        "INSERT INTO `plan_msec`.`phones`(`phone`,`relation`)VALUES('%s', 'contact') ON DUPLICATE KEY UPDATE ID=LAST_INSERT_ID(ID);"
                        , number
                        ));
                s.executeUpdate( String.format("INSERT INTO `call` (`id`, `uid`, `parent`, `start`, `length`, `type`, `phone`) VALUES (NULL, (SELECT userID FROM `chimaera`.`users` WHERE `domainName` = '%s' LIMIT 0,1), '%d', '%d', '%d', '%d', (SELECT LAST_INSERT_ID()));"
                        , getAgent(), getParenId(), getStartTime(), getDuration(), getType())
                        ,Statement.RETURN_GENERATED_KEYS
                );
                ResultSet rs = s.getGeneratedKeys();
                if (!rs.next()){
                    return;
                }
                Integer callDBID = rs.getInt(1);
                s.close();
                PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO `call_timeline` (`id`, `call`, `type`, `start`) VALUES (NULL, '"+callDBID+"', ?, ?);");
                for (int i = 0; i < timeline.size(); i++) {
                    TimelineEvent te = timeline.get(i);
                    ps.setInt(1, te.getType());
                    ps.setInt(2, te.getStart());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException ex) {
                EventHandler.logger.error(ex.toString());
            } catch( Exception e){
                e.printStackTrace();
            }
            saved = true;
        }
    }

    public Integer getParenId(){
        if( null == Parent){
            return 0;
        }
        return Parent.getId();
    }

    public void onHold() {
        timeline.add( TimelineEvent.hold() );
    }

    public void onUnhold() {
        timeline.add( TimelineEvent.unhold() );
    }
}

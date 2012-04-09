/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon.Events;

import Daemon.EventHandler;
import com.cisco.cti.ctios.cil.Arguments;
import com.cisco.cti.ctios.cil.Call;
import com.cisco.cti.ctios.util.CtiOs_IKeywordIDs;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bsalmanov
 */
public class CallEvent extends AbstractEvent implements EventInterface {

    protected String number;
    protected String device;
    protected Integer type;


    public CallEvent(Call c) {
        number = "11111";
        device = "1111111";
        Arguments args = c.GetValueArray(CtiOs_IKeywordIDs.CTIOS_ECC);
        type = 1;
        Daemon.Server.logger.trace( args.GetValueUIntObj("call.duration") );
    }
    
    @Override
    public void proceed(Connection dbConnection) {
        
        Statement s;
        try {
            s = dbConnection.createStatement ();
            /*s.executeUpdate (
                String.format(
                    "INSERT INTO agents_state (`uid`, `time`, `state`) VALUES( (SELECT userID FROM `chimaera`.`users` WHERE `domainName` = '%s' LIMIT 0,1), '%d', '%d')"
                , login, getTimestamp(), state)
            );*/
            s.executeUpdate (
                String.format(
                    "INSERT IGNORE INTO calls (`time`, `device`, `number`, `calltype`) VALUES( FROM_UNIXTIME('%d'), '%s', '%s', %d)"
                , getTimestamp(), device, number, type)
            );
        } catch (SQLException ex) {
            EventHandler.logger.error(ex.toString());
        }
    }
    
}

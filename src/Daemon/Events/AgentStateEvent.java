/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon.Events;

import Daemon.EventHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bsalmanov
 */
public class AgentStateEvent extends AbstractEvent implements PersistableInterface {

    private Integer state;
    private String login;
    
    public AgentStateEvent(String Login, Integer State) {
        super();
        this.state = State;
        this.login = Login;
    }
    
    @Override
    public void proceed(Connection dbConnection) {
        Statement s;
        try {
            s = dbConnection.createStatement ();
            s.executeUpdate (
                String.format(
                    "INSERT INTO agents_state (`uid`, `time`, `state`) VALUES( (SELECT userID FROM `chimaera`.`users` WHERE `domainName` = '%s' LIMIT 0,1), '%d', '%d')"
                , login, getTimestamp(), state)
            );
            s.executeUpdate (
                String.format(
                    "INSERT IGNORE INTO agents_state (`uid`, `time`, `state`) VALUES( '1', FROM_UNIXTIME('%d'), '%d')"
                , getTimestamp(), state)
            );
        } catch (SQLException ex) {
            EventHandler.logger.error(ex.toString());
        }
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon;

import Daemon.Events.PersistableInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author bsalmanov
 */
public class EventHandler {
    public static final Logger logger = Logger.getLogger(EventHandler.class);
    private Connection connection;
    
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public EventHandler(String url, String user, String password) throws NoConfigurationFoundException {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
        if(null==dbUrl || null==dbUser){// && null!=dbPassword)){
            throw new NoConfigurationFoundException();
        }
        
    }
    
    public void proceedEvent(PersistableInterface e){
        e.proceed(getConnection());
        try {
            getConnection().close();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        connection = null;
    }
    
    public Connection getConnection(){
        try {
            if( null == connection || connection.isClosed()){
                connect();
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }
    
    public boolean isConnectionOk(){
        return (null != getConnection());
    }
    
    public Connection connect(){
        try {
            if( null==connection || connection.isClosed() ){
                try {
                    String connectionUrl = "jdbc:mysql://".concat( dbUrl );
                    Class.forName ("com.mysql.jdbc.Driver").newInstance ();
                    connection = DriverManager.getConnection (connectionUrl, dbUser, dbPassword);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
                    Server.logger.error("Cannot connect to database server");
                }
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }
    
    public class NoConfigurationFoundException extends Exception{}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon;

import Daemon.EventHandler.NoConfigurationFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;


public class Server {
    public static final Logger logger = Logger.getLogger(Server.class);
    public static Properties conf = new Properties();
    public static long startTime;
    public static EventHandler events;

    public static void main(String[] args) {
        startTime = (new Date()).getTime();
        // Configuration -------------------
        String configFileName = "./config.properties";
        try{
            //configFile.load(configFile.getClass().getClassLoader().getResourceAsStream(configFileName));
            conf.load( new FileInputStream(configFileName) );
        } catch( java.lang.NullPointerException | IOException npe){
            logger.error("No configuration file found - "+configFileName+" required");
            return;
        }
        // -------------------- Configuration
        try {
            events = new EventHandler(conf.getProperty("events.db.url"), conf.getProperty("events.db.user"), conf.getProperty("events.db.password"));
        } catch (NoConfigurationFoundException ex) {
            logger.error("No configuration found for [events.db.*]. Required url, user, password.");
            //return;
        }
        if( !events.isConnectionOk() ){
            logger.error("Can't connect to events database server.");
            //return;
        }
        
        // Start gates -----------------------
        if( !Client.Gateway.Server.start() ) {
            logger.error("Controll gateway failed to start");
            return;
        }
        
        if( !Web.Gateway.Server.start() ) {
            logger.error("Web gateway failed to start");
        }
        // ---------------------- Gates started
        
        logger.info("Server started");
    }
}


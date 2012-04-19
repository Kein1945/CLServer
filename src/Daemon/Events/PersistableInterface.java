/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon.Events;

import java.sql.Connection;

/**
 *
 * @author bsalmanov
 */
public interface PersistableInterface {
    public void proceed(Connection dbConnection);
}

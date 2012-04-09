/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon.Events;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author bsalmanov
 */
public class AbstractEvent {

    private long createTime;
    
    public AbstractEvent() {
        createTime = (new Date().getTime());
    }

    public long getCreateTime() {
        return createTime;
    }
    
    public long getTimestamp(){
        return TimeUnit.MILLISECONDS.toSeconds(createTime);
    }
    
}

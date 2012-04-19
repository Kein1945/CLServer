/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon.Events;


/**
 *
 * @author bsalmanov
 */
public class AbstractEvent {

    private Integer createTime;
    
    public AbstractEvent() {
        createTime = (int) (System.currentTimeMillis() / 1000L);
    }

    public Integer getCreateTime() {
        return createTime;
    }
    
    public Integer getTimestamp(){
        return (int) (System.currentTimeMillis() / 1000L);
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author bsalmanov
 */
public class TimelineEvent {

    public static final Integer BEGIN = 1;
    public static final Integer ESTABLISHED = 2;
    public static final Integer HOLD = 3;
    public static final Integer UNHOLD = 4;
    public static final Integer CLEAR = 5;
    public static final Integer END = 6;

    protected Integer start;
    protected Integer type;
    
    public TimelineEvent(Integer eType) {
        type = eType;
        start = (int) (System.currentTimeMillis() / 1000L);
    }
    
    public static TimelineEvent begin(){
        return new TimelineEvent(BEGIN);
    }
    public static TimelineEvent established(){
        return new TimelineEvent(ESTABLISHED);
    }
    public static TimelineEvent hold(){
        return new TimelineEvent(HOLD);
    }
    public static TimelineEvent unhold(){
        return new TimelineEvent(UNHOLD);
    }
    public static TimelineEvent clear(){
        return new TimelineEvent(CLEAR);
    }
    public static TimelineEvent end(){
        return new TimelineEvent(END);
    }

    public Integer getStart() {
        return start;
    }

    public Integer getType() {
        return type;
    }
    
    
    
}

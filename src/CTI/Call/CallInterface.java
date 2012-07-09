/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

import java.util.List;

/**
 *
 * @author bsalmanov
 */
public interface CallInterface {
    public final static Integer ACCEPTED = 1;
    public final static Integer INCOME = 2;
    public final static Integer OUTGOING = 4;
    public final static Integer CONFERENCE = 8;
    public final static Integer TRANSFER = 8;
    public Integer getType();// Возвращает тип звонка
    public Integer getStartTime(); // Возвращает время начала соединения CallEstablished
    public Integer getDuration();
    public List<TimelineEvent> getTimeline();
    //public String getUniqID();

}

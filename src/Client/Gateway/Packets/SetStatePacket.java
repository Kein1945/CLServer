/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;

public class SetStatePacket extends Packet {
    
    final public static Map<Integer, String> stateLabel = new HashMap();
    
    static {
        stateLabel.put(0, "Login");
        stateLabel.put(1, "Logout");
        stateLabel.put(2, "Not ready");
        stateLabel.put(3, "Avaliable");
        stateLabel.put(4, "Talking");
        stateLabel.put(5, "Work not ready");
        stateLabel.put(6, "Work ready");
        stateLabel.put(7, "BusyOther");
        stateLabel.put(8, "Reserved");
        stateLabel.put(9, "Unknown");
        stateLabel.put(10, "Hold");
    }

    private Integer state;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    
    
    @Override
    public String getInfo(){
        return "Set state [" + stateLabel.get(this.state) +"|"+this.state+ "]";
    }
    
    @Override
    public Integer getId() {
        return Packet.SETSTATE;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        this.setState( buffer.readInt() );
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt(this.getState());
    }
    
}

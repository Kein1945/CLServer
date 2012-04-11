/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway.Packets;

import Operator.Gateway.Packet;
import Operator.Gateway.Server;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class GetStatePacket extends Packet{

    private Integer state;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    
    @Override
    public Integer getId() {
        return Packet.GETSTATE;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( this.getState() );
    }
    
}

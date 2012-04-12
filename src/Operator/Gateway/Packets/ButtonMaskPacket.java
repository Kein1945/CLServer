/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway.Packets;

import Operator.Gateway.Packet;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author bsalmanov
 */
public class ButtonMaskPacket extends Packet{

    private Integer bitmask;

    public Integer getBitmask() {
        return bitmask;
    }

    public void setBitmask(Integer bitmask) {
        this.bitmask = bitmask;
    }
    
    @Override
    public Integer getId() {
        return Packet.BTNMASK;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getInfo(){
        return "Button mask "+this.bitmask;
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( bitmask );
    }
    
}

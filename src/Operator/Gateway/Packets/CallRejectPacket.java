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
public class CallRejectPacket extends Packet {

    @Override
    public Integer getId() {
        return Packet.CALL_REJECT;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        
    }

    @Override
    public void send(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author bsalmanov
 */
public class CallAcceptPacket extends Packet {

    @Override
    public Integer getId() {
        return Packet.CALLACCEPT;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void send(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
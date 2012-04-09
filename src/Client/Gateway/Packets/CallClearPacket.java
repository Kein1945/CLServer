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
public class CallClearPacket extends Packet {

    @Override
    public Integer getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void get(ChannelBuffer buffer) {
        
    }

    @Override
    public void send(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

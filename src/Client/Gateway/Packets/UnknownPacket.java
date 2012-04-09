/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;
import org.jboss.netty.buffer.ChannelBuffer;

public class UnknownPacket extends Packet {

    public Integer packetCode = 0;

    public Integer getPacketCode() {
        return packetCode;
    }

    public void setPacketCode(Integer packetCode) {
        this.packetCode = packetCode;
    }
    
    
    
    @Override
    public Integer getId() {
        return Packet.UNKNOWN;
    }

    @Override
    public void get(ChannelBuffer buffer) {
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( this.getPacketCode() );
    }
    
}

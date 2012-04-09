/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;
import Client.Gateway.Server;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 *
 * @author bsalmanov
 */
public class ErrorPacket extends Packet{

    protected String error = "Unknown error";

    public ErrorPacket() {
    }

    public ErrorPacket(String msg) {
        this.error = msg;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public Integer getId() {
        return Packet.ERROR;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( error.length() );
        ChannelBuffer ch = ChannelBuffers.copiedBuffer( error, CharsetUtil.UTF_8);
        buffer.writeBytes(ch);
    }
    
}

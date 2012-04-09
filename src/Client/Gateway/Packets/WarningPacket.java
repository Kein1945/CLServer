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
public class WarningPacket extends Packet{
    
    protected String message = "Unknown warning";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message + "\0";
    }
    
    @Override
    public Integer getId() {
        return Packet.WARNING;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getInfo(){
        return "Warning: " + this.message;
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( message.length() );
        ChannelBuffer ch = ChannelBuffers.copiedBuffer( message, CharsetUtil.UTF_8);
        buffer.writeBytes(ch);
    }
    
}

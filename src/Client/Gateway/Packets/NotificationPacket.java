/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 *
 * @author bsalmanov
 */
public abstract class NotificationPacket extends Packet{

    public NotificationPacket(String message) {
        this.message = message;
    }

    protected String message;

    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet. Notification packet only send for client");
    }
    
    @Override
    public String getInfo(){
        return "Notification[" + this.getId() + "]: " + this.message;
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( message.length() );
        ChannelBuffer ch = ChannelBuffers.copiedBuffer( message, CharsetUtil.UTF_8);
        buffer.writeBytes(ch);
    }
    
}

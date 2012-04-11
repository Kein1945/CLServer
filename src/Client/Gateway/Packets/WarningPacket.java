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
public class WarningPacket extends NotificationPacket{

    public WarningPacket() {
        super("Unknwon warning");
    }
    
    @Override
    public Integer getId() {
        return Packet.WARNING;
    }
}

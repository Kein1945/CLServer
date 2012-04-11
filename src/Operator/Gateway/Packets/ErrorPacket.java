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
public class ErrorPacket extends NotificationPacket{


    public ErrorPacket() {
        super("Unknown error");
    }
    
    @Override
    public Integer getId() {
        return Packet.ERROR;
    }
}

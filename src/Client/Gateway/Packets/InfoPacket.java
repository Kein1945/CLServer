/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway.Packets;

import Client.Gateway.Packet;

/**
 *
 * @author bsalmanov
 */
public class InfoPacket extends NotificationPacket{

    public InfoPacket() {
        super("Unkown notification");
    }
    
    @Override
    public Integer getId() {
        return Packet.INFO;
    }
}

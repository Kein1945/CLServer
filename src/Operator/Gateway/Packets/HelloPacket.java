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
public class HelloPacket  extends Packet {
    
    protected Integer ClientVersion = 0;
    
    @Override
    public Integer getId(){
        return Packet.HELLO;
    }
    
    @Override
    public void get(ChannelBuffer buffer) {
        ClientVersion = buffer.readInt();
    }
    
    public Integer getVersion(){
        return ClientVersion;
    }

    @Override
    public void send(ChannelBuffer buffer) { }
}
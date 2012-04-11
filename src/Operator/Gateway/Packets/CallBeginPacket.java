/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway.Packets;

import Operator.Gateway.Packet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 *
 * @author bsalmanov
 */
public class CallBeginPacket extends Packet{

    protected String device = ""; // На какой номер звонят
    protected String number = ""; // С какого номер звонят

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    
    @Override
    public Integer getId() {
        return Packet.CALL_BEGIN;
    }

    @Override
    public void get(ChannelBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void send(ChannelBuffer buffer) {
        ChannelBuffer ch = ChannelBuffers.copiedBuffer( device, CharsetUtil.UTF_8);
        buffer.writeInt( device.length() );
        buffer.writeBytes(ch);
        
        buffer.writeInt( number.length() );
        ch = ChannelBuffers.copiedBuffer( number, CharsetUtil.UTF_8);
        buffer.writeBytes(ch);
    }
    
}

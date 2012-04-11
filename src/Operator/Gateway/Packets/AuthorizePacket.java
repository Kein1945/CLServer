/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway.Packets;

import Operator.Gateway.Packet;
import Operator.Gateway.Server;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
/**
 *
 * @author bsalmanov
 */
public class AuthorizePacket extends Packet{
    
    public final static Integer AUTHORIZATION_OK = 1;
    public final static Integer NOT_AUTHORIZED_YET = 2;
    public final static Integer INVALID_LOGINPASSWORD = 3;
    
    protected Integer code = 0;
    protected String reason = "OK";

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    protected String Login = "";
    protected String Password = "";
    protected String Instrument = "";
    protected Integer Extension;

    @Override
    public void get(ChannelBuffer buffer) {
        int loginLength = buffer.readInt();
        for (int i = 0; i < loginLength; i ++)
            Login += (char)buffer.readByte();
        
        int passwordLength = buffer.readInt();
        for (int i = 0; i < passwordLength; i ++)
            Password += (char)buffer.readByte();
        
        int InstrumentLength = buffer.readInt();
        for (int i = 0; i < InstrumentLength; i ++)
            Instrument += (char)buffer.readByte();
        Extension = buffer.readInt();
    }

    @Override
    public String getInfo(){
        return "Authorization ["
                +((0==this.code)?(this.Login+":"+this.Password+"@"+this.Instrument+"/"+this.Extension):this.reason)
                + "]";
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public Integer getId() {
        return Packet.AUTHORIZE;
    }

    @Override
    public void send(ChannelBuffer buffer) {
        buffer.writeInt( code );
        buffer.writeInt( reason.length() );
        ChannelBuffer ch = ChannelBuffers.copiedBuffer( reason, CharsetUtil.UTF_8);
        buffer.writeBytes(ch);
    }
    public Integer getExtension() {
        return Extension;
    }

    public String getInstrument() {
        return Instrument;
    }

    public String getLogin() {
        return Login;
    }

    public String getPassword() {
        return Password;
    }
    
    @Override
    public String toString(){
        return Login + '|' + Password + '|' + Instrument + '|' + Extension;
    }
}

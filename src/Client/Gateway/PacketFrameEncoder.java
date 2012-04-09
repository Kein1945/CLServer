/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
/**
 *
 * @author bsalmanov
 */
public class PacketFrameEncoder extends OneToOneEncoder {
    @Override
    protected synchronized Object encode(ChannelHandlerContext channelhandlercontext, Channel channel, Object obj) throws Exception {
        if(!(obj instanceof Packet)){
            System.out.println("Damn! We get not packet for send");
            return obj; // Если это не пакет, то просто пропускаем его дальше
        }
        Packet p = (Packet) obj;
        
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(); // Создаём динамический буфер для записи в него данных из пакета. Если Вы точно знаете длину пакета, Вам не обязательно использовать динамический буфер — ChannelBuffers предоставляет и буферы фиксированной длинны, они могут быть эффективнее.
        Packet.write(p, buffer); // Пишем пакет в буфер
        
        //Server.logger.trace(p.getClass().toString());
        //Server.logger.trace(buffer);
        
        return buffer; // Возвращаем буфер, который и будет записан в канал
    }
}

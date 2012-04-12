/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway;

import org.jboss.netty.channel.*;
/**
 *
 * @author bsalmanov
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {

    private PacketFrameDecoder packetFrameDecoder;
    private PacketFrameEncoder packetFrameEncoder;

    private ClientWorker worker;
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                // Событие вызывается при подключении клиента.
        worker = new ClientWorker(e.getChannel());
    }
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                // Событие закрытия канала. Используется в основном, чтобы освободить ресурсы, или выполнить другие действия, которые происходят при отключении пользователя.
                //Если его не обработать, вы можете и не заметить, что пользователь отключился, если он напрямую не сказал этого серверу, а просто оборвался канал.
        worker.disconnectedFromChannel();
    }
    
    /**
     * 
     * Метод вызываемый при получении пакета от клиента
     * @param ctx
     * @param e 
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        /*Channel channel = e.getChannel();
        if( channel.isOpen() ){
            ChannelFuture future = channel.write(packet);
            try {
                future.await(10000); // Ждём не более 10 секунд, пока действие закончится
            } catch(InterruptedException ignored) {}
            System.out.println("Close connection");
            channel.close();
        }*/
        Packet packet = (Packet) e.getMessage();
        worker.acceptPacket((Packet) e.getMessage());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
                // На канале произошло исключение. Выводим ошибку, закрываем канал.
        e.getCause().printStackTrace();
        Server.logger.error("Exception from downstream: " + e.getCause());
        worker.disconnectedFromChannel();
        ctx.getChannel().close();
    }
}

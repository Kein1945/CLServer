/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class Server {
    public static Properties conf;
    public static final Logger logger = Logger.getLogger(Server.class);
    // Инкременторы для статистики пакетов
    public static volatile Integer acceptedPackets = 0;
    public static volatile Integer sendPackets = 0;

	public static boolean start() {
            conf = Daemon.Server.conf;
            try{
                ExecutorService bossExec = new OrderedMemoryAwareThreadPoolExecutor(1, 400000000, 2000000000, 60, TimeUnit.SECONDS);
                ExecutorService ioExec = new OrderedMemoryAwareThreadPoolExecutor(4 /* число рабочих потоков */, 400000000, 2000000000, 60, TimeUnit.SECONDS);
                
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExec, ioExec));
                bootstrap.setOption("backlog", 500);
                bootstrap.setOption("connectTimeoutMillis", conf.getProperty("client.connectTimeoutMillis", "10000"));
                
		bootstrap.setPipelineFactory(new ServerPipelineFactory());
                Integer port = Integer.parseInt( conf.getProperty("clientgate.server.port", "8080") );
                Channel channel = bootstrap.bind(new InetSocketAddress( port ));
                Server.logger.info("Started at "+port+" port.");
                return true;
            } catch (org.jboss.netty.channel.ChannelException bindException){
                return false;
            }
	}

	/**
	 * Фабрика, создающая обработчики для входящих соединений.
	 */
	static class ServerPipelineFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
                        Server.logger.info("Client accepted");
                        PacketFrameDecoder decoder = new PacketFrameDecoder();
                        PacketFrameEncoder encoder = new PacketFrameEncoder();
                        return Channels.pipeline(decoder, encoder, new ClientHandler());
		}

	}
        
        public static void acceptPacket(){
            acceptedPackets++;
        }
        
        public static void sendPacket(){
            sendPackets++;
        }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
/**
 *
 * @author bsalmanov
 */
public class Server {
    public static Properties conf;
    public static final Logger logger = Logger.getLogger(Server.class);
    // Инкрементор для ститистики запросов
    public static Integer requests = 0;
    public static boolean start() {
        conf = Daemon.Server.conf;
        try{
            Executor threadPool = Executors.newCachedThreadPool();
            ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(threadPool, threadPool));
            bootstrap.setPipelineFactory(new ProxyServerPipelineFactory(threadPool));
            Integer port = Integer.parseInt( conf.getProperty("webgate.server.port", "80") );
            bootstrap.bind(new InetSocketAddress( port ));
            logger.info("Started at " + port + " port");
        } catch (org.jboss.netty.channel.ChannelException bindException){
            return false;
        }
        return true;
    }
    /**
    * Фабрика, создающая обработчики для входящих соединений.
    */
    static class ProxyServerPipelineFactory implements ChannelPipelineFactory {

            private Executor threadPool	= null;

            public ProxyServerPipelineFactory(Executor threadPool) {
                    this.threadPool = threadPool;
            }

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline p = Channels.pipeline();
                    p.addLast("decoder", new HttpRequestDecoder());
                    p.addLast("encoder", new HttpResponseEncoder());
                    p.addLast("handler", new Handler());
                    return p;
            }
    }
    
    public static void logRequest(String uri){
        requests++;
    }
}

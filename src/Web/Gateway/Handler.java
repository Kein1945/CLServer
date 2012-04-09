/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway;

import Web.Gateway.Controllers.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 *
 * @author bsalmanov
 */
@ChannelPipelineCoverage("one")
public class Handler extends SimpleChannelUpstreamHandler{
    
	private volatile Channel channel = null;
        protected String remoteAddr = "-";
        
        protected final static List<ControllerInterface> controllers = new ArrayList<>();
        
        static {
            controllers.add( new MakeCallController() );
            controllers.add( new ListAgentsController() );
            controllers.add( new StatisticController() );
        }

	public Handler() {
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		channel = e.getChannel();
                remoteAddr = getRemoteIPAddress(ctx);
                HttpRequest request = (HttpRequest) e.getMessage();
                Server.logRequest(request.getUri());
                ControllerInterface controller;
                if( isAllowedReqest( remoteAddr ) ){ // Разрешенно ли с этого клиента обращаться на сервер
                    Server.logger.info(remoteAddr+":"+request.getUri());
                    controller = findController( request.getUri() );
                    controller.proceed(request);
                } else { // Запрещенный IP
                    Server.logger.warn("Access forbidden. IP: "+remoteAddr);
                    controller = new ForbiddenController("Acces denied. Yor ip is "+remoteAddr+". Filter tactic \""+getIPFilter()+"\".");
                    controller.proceed(request);
                }
                writeResponseAndClose( ((AbstractController)controller).getResponse() );
	}
        
        /**
         * Проверяет URI на соотвествие существующим контроллерам
         * и возвращает объект контроллера
         * @param uri
         * @return 
         */
        protected ControllerInterface findController(String uri){
            ControllerInterface result = new UnknownRequestController();
            for (Iterator<ControllerInterface> it = controllers.iterator(); it.hasNext();) {
                ControllerInterface controllerInterface = it.next();
                ControllerInterface controller;
                try {
                    controller = ((ControllerInterface)controllerInterface).getClass().newInstance();
                } catch (    InstantiationException | IllegalAccessException ex) {
                    Server.logger.error("Cant instaniate controller");
                    result = new InternalErrorController();
                    break;
                }
                if( controller.isEquals(uri) ){
                    result = controller;
                    break;
                }
            }
            ((AbstractController)result).setURI(uri);
            ((AbstractController)result).setChannel(channel);
            return result;
        }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Server.logger.error("Unexpected exception from webgate server handler: " + e.getCause());
                //e.getCause().printStackTrace();
		e.getChannel().close();
	}

        /**
         * Проверят с учетом конфигурации, можно ли принимать соединение с данного IP
         * @param remoteAddr
         * @return 
         */
        protected boolean isAllowedReqest(String remoteAddr){
            switch (getIPFilter()) {
                case "deny":
                    String[] allow = getAllowedIP();
                    for(Integer i=0; i < allow.length; i++){
                        if( allow[i].equals(remoteAddr) ){
                            return true;
                        }
                    }
                    return false;
                case "allow":
                    String[] disallow = getDisallowedIP();
                    for(Integer i=0; i < disallow.length; i++){
                        if( disallow[i].equals(remoteAddr) ){
                            return false;
                        }
                    }
                    return true;
                default:
                    Server.logger.error("Bad value for configuration option webgate.ip.filter = \""+getIPFilter()+"\". Must be \"allow\" or \"deny\". All request will be declined.");
                    return false;
            }
        }
        
        private static String IPFilter = null;
        protected static String getIPFilter(){
            if( null == IPFilter ){
                IPFilter = Server.conf.getProperty("webgate.ip.filter", "deny").toLowerCase();
            }
            return IPFilter;
        }
        
        private static String[] IPAllowed = null;
        protected static String[] getAllowedIP(){
            if( null == IPAllowed ){
                IPAllowed = Server.conf.getProperty("webgate.ip.allow", "").split(";");
            }
            return IPAllowed;
        }
        private static String[] IPDisallowed = null;
        protected static String[] getDisallowedIP(){
            if( null == IPDisallowed ){
                IPDisallowed = Server.conf.getProperty("webgate.ip.disallow", "").split(";");
            }
            return IPDisallowed;
        }
        
	/**
	 * Отправляет формированный http-ответ во входящее соединение.
	 */
	private void writeResponseAndClose(HttpResponse response) {
            if (response != null) {
                response.setHeader(HttpHeaders.Names.CONNECTION, "close");
                channel.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                channel.close();
            }
	}
        
        protected static String getRemoteIPAddress(ChannelHandlerContext ctx) {
            String fullAddress = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
            // Address resolves to /x.x.x.x:zzzz we only want x.x.x.x
            if (fullAddress.startsWith("/")) {
                fullAddress = fullAddress.substring(1);
            }
            int i = fullAddress.indexOf(":");
            if (i != -1) {
                fullAddress = fullAddress.substring(0, i);
            }
            return fullAddress;
        }

}

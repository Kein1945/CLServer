/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.*;

/**
 *
 * @author bsalmanov
 */
public abstract class AbstractController{

    private String URI = "";
    protected HttpResponse response;
    protected Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getURI() {
        return URI;
    }
    
    public String[] parseUrl(){
        return URI.substring(1).split("/");
    }
    
    /**
     * Создает OK ответ сервера с контентом
     * @param content 
     */
    public void createOKResponse(String content){
        createResponse();
        setContent(content);
    }
        
    public void createDenyResponse(String errorText){
        createResponse(HttpResponseStatus.FORBIDDEN);
        setContent("<html><body><h3>" + errorText + "</h3></body></html>");
    }
    
    public void createNotFoundResponse(String errorText){
        createResponse(HttpResponseStatus.NOT_FOUND);
        setContent("<html><body><h3>" + errorText + "</h3></body></html>");
    }
    
    protected void createResponse(HttpResponseStatus status){
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf-8");
    }
    
    protected void createResponse(){
        createResponse(HttpResponseStatus.OK);
    }
    protected void setContent(String text){
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(text, "utf-8");
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
    }

    public final HttpResponse getResponse() {
        return response;
    }
}

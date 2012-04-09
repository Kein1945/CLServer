/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import Client.Gateway.Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.*;

/**
 *
 * @author bsalmanov
 */
public class UnknownRequestController extends AbstractController implements ControllerInterface{

    @Override
    public boolean isEquals(String uri) {
        return false;
    }

    @Override
    public void proceed(HttpRequest request) {
        File f = new File("www/"+getURI());
        if( f.exists() && f.isFile() ){
            response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain");// "image/x-icon");
            //response.setHeader(HttpHeaders.Names.CONTENT_TYPE, new MimetypesFileTypeMap().getContentType(f));// "image/x-icon")
            //Server.logger.info(getURI()+" - "+new MimetypesFileTypeMap().getContentType(f));
            
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, f.length());
            response.setHeader(HttpHeaders.Names.CONNECTION, "Close");
            FileInputStream fis;
            byte[] bs = new byte[8192];
            try {
                ChannelBuffer fileContent = ChannelBuffers.dynamicBuffer();
                fis = new FileInputStream(f);
                while (fis.read(bs) > 0) {
                    //ChannelBuffer buffer = ChannelBuffers.copiedBuffer(bs);
                    fileContent.writeBytes(bs);
                }
                response.setContent(fileContent);
                fis.close();
            } catch (FileNotFoundException ex ){
                createNotFoundResponse("Server can't proceed request "+getURI()+".<br/>File doesnt exists.");
            } catch(IOException ex) {
                createNotFoundResponse("Server can't proceed request "+getURI()+".<br/>File doesnt exists.");
            }
        } else {
            createNotFoundResponse("Server can't proceed request "+getURI()+".<br/><a href=\"/agents/list/active\">Active agents list</a>.");
        }
    }
    
}

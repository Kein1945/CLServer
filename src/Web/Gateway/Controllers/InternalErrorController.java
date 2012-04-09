/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author bsalmanov
 */
public class InternalErrorController extends AbstractController implements ControllerInterface{

    @Override
    public boolean isEquals(String uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void proceed(HttpRequest request) {
        createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        setContent("Internal server error.");
    }
    
}

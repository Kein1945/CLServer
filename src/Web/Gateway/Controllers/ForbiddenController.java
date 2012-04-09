/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 
 * @author bsalmanov
 */
public class ForbiddenController extends AbstractController implements ControllerInterface{

    protected String message;

    public ForbiddenController(String message) {
        this.message = message;
    }

    @Override
    public boolean isEquals(String uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void proceed(HttpRequest request) {
        createDenyResponse(message);
    }
    
}

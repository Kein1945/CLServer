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
public interface ControllerInterface extends Cloneable{
    public boolean isEquals(String uri);
    public void proceed(HttpRequest request);
}

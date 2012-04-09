/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import CTI.Gateway.Manager;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 *
 * @author bsalmanov
 */
public class MakeCallController extends AbstractController implements ControllerInterface{

    @Override
    public boolean isEquals(String uri) {
        setURI(uri);
        return (getURI().startsWith("/make_call/") && 3 == this.parseUrl().length);
    }

    @Override
    public void proceed(HttpRequest request) {
        String[] parts = this.parseUrl();
        String ntlmUser = parts[1];
        String number = parts[2];
        Manager manager = CTI.Gateway.Manager.getManager(ntlmUser);
        if( null != manager ){
            proceedOK(manager, number);
        } else {
            proceedFail(ntlmUser);
        }
    }
    
    protected void proceedOK(Manager manager, String number){
        createOKResponse("Calling to "+number+"..");
        manager.makeCall(number);
    }
    
    protected void proceedFail(String user){
        createNotFoundResponse("Did you foget run client? User \""+user+"\" not connected.");
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import CTI.Gateway.Manager;
import Operator.Gateway.Client;
import java.util.Map;
import org.jboss.netty.handler.codec.http.HttpRequest;
/**
 *
 * @author bsalmanov
 */
public class ListAgentsController extends AbstractController implements ControllerInterface {

    @Override
    public boolean isEquals(String uri) {
        return "/agents/list/active".equals(uri);
    }

    @Override
    public void proceed(HttpRequest request) {
        Map<Client, Manager> managers = Client.getClients();
        StringBuilder buffer = new StringBuilder();
        buffer.append("<p>").append(managers.size()).append(" agent connected</p>");
        if( !managers.isEmpty() ){
            buffer.append("<ul>");
            for (Map.Entry<Client, Manager> entry : managers.entrySet()) {
                Client client = entry.getKey();
                Manager manager = entry.getValue();
                buffer.append("<li>").append(client.getLogin()).append("</li>");
            }
            buffer.append("</ul>");
        }
        createOKResponse(buffer.toString());
    }
    
}

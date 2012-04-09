/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Web.Gateway.Controllers;

import CTI.Gateway.Manager;
import Web.Gateway.Server;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author bsalmanov
 */
public class StatisticController extends AbstractController implements ControllerInterface{

    @Override
    public boolean isEquals(String uri) {
        return "/statistic".equals(uri);
    }

    @Override
    public void proceed(HttpRequest request) {
        StringBuilder buffer = new StringBuilder();
        
        Map<Client.Gateway.Client, Manager> managers = Manager.getManagers();
        buffer.append("<h4>").append(managers.size()).append(" agent connected</h4><hr/>");
        if( !managers.isEmpty() ){
            buffer.append("<ul>");
            for (Map.Entry<Client.Gateway.Client, Manager> entry : managers.entrySet()) {
                Client.Gateway.Client client = entry.getKey();
                Manager manager = entry.getValue();
                buffer.append("<li>").append(client.getLogin()).append("</li>");
            }
            buffer.append("</ul>");
        }
        buffer.append("<h4>Total</h4><hr/>");
        long timeLast = (new Date()).getTime() - Daemon.Server.startTime;
        buffer.append("<p><strong>Server uptime</strong>: ").append(
                String.format("%d:%d, %d sec",
                        TimeUnit.MILLISECONDS.toHours(timeLast),
                        TimeUnit.MILLISECONDS.toMinutes( timeLast ) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLast)),
                        TimeUnit.MILLISECONDS.toSeconds( timeLast ) - 
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes( timeLast ))
                    )
                ).append("</p>");
        buffer.append("<p><strong>Client accept packets</strong>: ").append(Client.Gateway.Server.acceptedPackets).append(", ")
                .append(String.format("%.3f per minute", (float)(Client.Gateway.Server.acceptedPackets/ (TimeUnit.MILLISECONDS.toMinutes( timeLast )+1)) ))
                .append("</p>");
        buffer.append("<p><strong>Client send packets</strong>: ").append(Client.Gateway.Server.sendPackets).append(", ")
                .append(String.format("%.3f per minute", (float)(Client.Gateway.Server.sendPackets/ (TimeUnit.MILLISECONDS.toMinutes( timeLast )+1)) ))
                .append("</p>");
        buffer.append("<p><strong>Client total packets</strong>: ").append((Client.Gateway.Server.sendPackets+Client.Gateway.Server.acceptedPackets)).append(", ")
                .append(String.format("%.3f per minute", (float)((Client.Gateway.Server.sendPackets+Client.Gateway.Server.acceptedPackets)/ (TimeUnit.MILLISECONDS.toMinutes( timeLast )+1)) ))
                .append("</p>");
        buffer.append("<p><strong>Webgate requests amount</strong>: ").append(Server.requests).append(", ")
                .append(String.format("%.3f per minute", (float)(Server.requests/ (TimeUnit.MILLISECONDS.toMinutes( timeLast )+1)) ))
                .append("</p>");
        
        buffer.append("<h4>Memory usage</h4><hr/>");
        
        Runtime runtime = Runtime.getRuntime();
        float maxMemory = runtime.maxMemory();
        float allocatedMemory = (float)runtime.totalMemory();
        float freeMemory = (float)runtime.freeMemory();
        NumberFormat format = NumberFormat.getInstance();
        
        buffer.append("<ul>");
        buffer.append("<li><strong>free memory</strong>: ").append( String.format( "%.2f mb",(freeMemory / 1048576)) ).append("</li>");
        buffer.append("<li><strong>allocated memory</strong>: ").append(String.format( "%.2f mb",allocatedMemory / 1048576)).append("</li>");
        buffer.append("<li><strong>max memory</strong>: ").append(String.format( "%.2f mb",maxMemory / 1048576)).append("</li>");
        buffer.append("<li><strong>total free memory</strong>: ").append(String.format( "%.2f mb",(freeMemory + (maxMemory - allocatedMemory)) / 1048576)).append("</li>");
        buffer.append("</ul>");
        
        buffer.append("<h4>Threads pool</h4><hr/>");
        
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        if( threadArray.length > 0 ){
            buffer.append("<table><tbhead><tr><th>Group</th><th>Id</th><th>Name</th><th>State</th></tr></thead><tbody>");
            for (int i = 0; i < threadArray.length; i++) {
                Thread tr = threadArray[i];
                if( !"system".equals(tr.getThreadGroup().getName())){
                    buffer.append("<tr>")
                        .append("<td>").append(tr.getThreadGroup().getName()).append("</td>")
                        .append("<td>").append(tr.getId()).append("</td>")
                        .append("<td>").append(tr.getName()).append("</td>")
                        .append("<td>").append(tr.getState().toString()).append("</td>")
                        .append("</tr>");
                }
            }
            buffer.append("</tbody></table>");
        }
        createOKResponse( buffer.toString() );
    }
    
}

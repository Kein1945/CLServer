/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway;

import CTI.Gateway.Manager;
import Operator.Gateway.Packets.AuthorizePacket;
import Operator.Gateway.Packets.ErrorPacket;
import Operator.Gateway.Packets.HelloPacket;
import Daemon.Events.AgentStateEvent;
import org.jboss.netty.channel.Channel;
/**
 *
 * @author bsalmanov
 */
public class ClientWorker {
    private Channel channel;
    private boolean isHelloAccepted = false;
    
    private Client client;
    private Manager cti_session;

    public ClientWorker(Channel channel) {
        this.channel = channel;
        // Тут мы создаем клиента, не важно что он не авторизован и еще не идентифицирован, тем не менее это клиент.
        this.client = new Client(channel);
        this.cti_session = new Manager(this.client);
        client.setManager(cti_session);
    }
    
    public boolean ctiConnect(){
        return cti_session.connect();
    }
    
    /**
     * Вызывается при закрытии соединения
     */
    public void disconnectedFromChannel(){
        cti_session.disconnect();
        if( channel.isConnected() )
            channel.disconnect();
        AgentStateEvent e = new AgentStateEvent(client.getLogin(), Client.State.LOGOUT);
        Daemon.Server.events.proceedEvent(e);
        Server.logger.info("Client "+client.getLogin() + " disconnected.");
        client.onDisconnected();
    }
    
    /**
     * Метод получает пакет и обрабатывает логику подключения:
     * проверяет полученные ли были пакеты Hello (Для контроля версии протокола)
     * Проверяет авторизован ли клиент или нет
     * @param packet 
     */
    public void acceptPacket(Packet packet){
        if( !isUnderstandingReached() ){
            // Если с нами еще не поздоровались
            if( packet instanceof HelloPacket ){
                isHelloAccepted = acceptHello( (HelloPacket)packet );
            } else { // Вообще странное место, пакет известный но с нами не поздоровались. Сообщим клиенту но надо бы залогировать
                this.client.sendError("Server required hello."); // Похоже на попытку обхода
                Server.logger.warn("Received packet");
            }
        } else { // Если с нами поздоровались
            if( this.client.isAuthorized() ){ // Если клиент авторизован - мы можем выполнять всякие классные штуки
                client.acceptPacket(packet); // Сюда попадают разрешенные пакеты всех авторизованных клиентов
            } else { // Если не авторизован то попросим его об этом
                if( packet instanceof AuthorizePacket){
                    AuthorizePacket ap = (AuthorizePacket) packet;
                    Server.logger.trace("Authorization");
                    if( !cti_session.setAgentMode(ap.getLogin(), ap.getPassword(), ap.getInstrument(), ap.getExtension()) ){
                        client.sendError("Fail to set agent mode");
                    } else {
                        client.setLogin( ap.getLogin() );
                        client.setPassword( ap.getPassword() );
                        client.setInstrument( ap.getInstrument() );
                        client.setExtension( ap.getExtension() );
                    }
                } else { // Странное место, не авторизовались, а уже шлют комманды, надо бы писать в лог.
                    client.sendWarning("Server required authorization.");
                }
            }
            
        }
        
    }
    
    private boolean isUnderstandingReached(HelloPacket hello){
        return isHelloAccepted || hello.getVersion() == Packet.VERSION; // Сравним версии протокола,
            //  с ростом версии, можно сделать проверку с учетом совместимости
    }
    
    public boolean isUnderstandingReached(){
        return isHelloAccepted;
    }
    
    private boolean acceptHello(HelloPacket hello){
        if( isUnderstandingReached( hello ) ){ // Если "взаимопонимание достигнуто",
            boolean ctiConnect = this.ctiConnect();
            //Server.logger.warn("Connect cti is "+ctiConnect);
            return true;
        } else {
            ErrorPacket e = new ErrorPacket(); // Отправим ему пакет с ошибкой
            e.setMessage("Server protocol version: "
                    + Packet.VERSION + ". Your client protocol version("
                    + hello.getVersion()+") is unsupported.");
            channel.write(e);
            disconnectedFromChannel();
            return false;
        }
    }
}

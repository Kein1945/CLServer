/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway;

import CTI.Gateway.Manager;
import Client.Gateway.Packets.*;
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

    public ClientWorker(Channel channel) {
        this.channel = channel;
        // Тут мы создаем клиента, не важно что он не авторизован и еще не идентифицирован, тем не менее это клиент.
        this.client = new Client(channel);
    }
    
    /**
     * Вызывается при закрытии соединения
     */
    public void disconnectedFromChannel(){
        this.client.getManager().disconnect();
        Manager.removeManager(this.client);
        if( channel.isConnected() )
            channel.disconnect();
        AgentStateEvent e = new AgentStateEvent(client.getLogin(), Client.State.LOGOUT);
        Daemon.Server.events.proceedEvent(e);
        Server.logger.info("Client "+client.getLogin() + " disconnected.");
    }
    
    /**
     * Метод получает пакет и обрабатывает логику подключения:
     * проверяет полученные ли были пакеты Hello (Для контроля версии протокола)
     * Проверяет авторизован ли клиент или нет
     * @param packet 
     */
    public void acceptPacket(Packet packet){
        if( packet instanceof UnknownPacket || packet instanceof NullPacket){// Если мы получили не известный пакет
            channel.write(packet); // Отправим его клиенту, что бы знал что проблемы на клиенте - пакет не соотвествует протоколу
            return;
        }
        if( isHelloAccepted ){ // Если с нами поздоровались
            if( this.client.isAuthorized() ){ // Если клиент авторизован - мы можем выполнять всякие классные штуки
                client.acceptPacket(packet); // Сюда попадают разрешенные пакеты всех авторизованных клиентов
            } else { // Если не авторизован то попросим его об этом
                if( packet instanceof AuthorizePacket){
                    // Сообщим объекту клиента, что за клиент
                    // Объект client сам вытащит нужные поля из пакета
                    client.setAuthorizationObject( (AuthorizePacket) packet );
                    try{
                        Manager m = Manager.getManager( this.client ); // Тут мы получаем менеджера подключения к CTI
                        // Если вдруг чего пошло не так, то мы получим исключения, которые будут сигнализировать о провале авторизаии
                        client.setManager(m);
                        ((AuthorizePacket)packet).setCode( AuthorizePacket.AUTHORIZATION_OK );
                    } catch(Manager.FailedToConnectException fail){ // Нет соединения с сервером CTI - похоже на внешщние проблемы
                        ((AuthorizePacket)packet).setReason( "Failed to connect to CTI server" );
                        ((AuthorizePacket)packet).setCode( AuthorizePacket.NOT_AUTHORIZED_YET );
                    } catch (Manager.FailToAuthorizeException authFail){ // Ошибка авторизации
                        ((AuthorizePacket)packet).setReason( authFail.getMessage() );
                        ((AuthorizePacket)packet).setCode( AuthorizePacket.NOT_AUTHORIZED_YET );
                    } finally{
                        channel.write(packet);
                    }
                } else { // Странное место, не авторизовались, а уже шлют комманды, надо бы писать в лог.
                    WarningPacket e = new WarningPacket();
                    e.setMessage("Server required authorization.");
                    channel.write(e);
                }
            }
            
        } else { // Если с нами еще не поздоровались
            if( packet instanceof HelloPacket){
                if ( ((HelloPacket)packet).getVersion() >= Packet.VERSION ) { // Сравним версии протокола,
                    //  с ростом версии, можно сделать проверку с учетом совместимости
                    channel.write( packet ); // Если успешно, то отправим ему пакет Hello
                    isHelloAccepted = true;
                } else { // А вот тут пробелмы с протоколом
                    ErrorPacket e = new ErrorPacket(); // Отправим ему пакет с ошибкой
                    e.setError("Server protocol version: "
                            + Packet.VERSION + ". Your client protocol version("
                            +((HelloPacket)packet).getVersion()+") is unsupported.");
                    channel.write(e);
                    disconnectedFromChannel();
                }
            } else { // Вообще странное место, пакет известный но с нами не поздоровались. Сообщим клиенту но надо бы залогировать
                WarningPacket e = new WarningPacket(); // Похоже на попытку обхода
                e.setMessage("Server required hello packet.");
                channel.write(e);
                Server.logger.warn("Server required hello packet!");
            }
        }
        
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Operator.Gateway;

import Operator.Gateway.Packets.CallUnholdPacket;
import Operator.Gateway.Packets.SetStatePacket;
import Operator.Gateway.Packets.CallAnswerPacket;
import Operator.Gateway.Packets.CallBeginPacket;
import Operator.Gateway.Packets.GetStatePacket;
import Operator.Gateway.Packets.ErrorPacket;
import Operator.Gateway.Packets.CallRejectPacket;
import Operator.Gateway.Packets.CallEndPacket;
import Operator.Gateway.Packets.HelloPacket;
import Operator.Gateway.Packets.AuthorizePacket;
import Operator.Gateway.Packets.CallHoldPacket;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author bsalmanov
 */
public abstract class Packet {
    protected static Map<Integer, Packet> packetMap = new HashMap();
    public static volatile Integer packetId = 0;
    
    final public static Integer VERSION = 1;
    
    // Service Packets
    final public static Integer HELLO = 1;
    final public static Integer BTNMASK = 2;
    
    final public static Integer INFO = 5;
    final public static Integer WARNING = 6;
    final public static Integer ERROR = 7;
    
    // Agent packets
    final public static Integer AUTHORIZE = 101;
    final public static Integer SETSTATE = 105;
    final public static Integer GETSTATE = 106;
    
    // Call packets
    final public static Integer CALL_BEGIN = 201;
    
    final public static Integer CALL_ANSWER = 202;
    final public static Integer CALL_REJECT = 203;
    
    final public static Integer CALL_HOLD = 204;
    final public static Integer CALL_UNHOLD = 205;
    
    final public static Integer CALL_END = 220;
    
    static {
        Packet.packetMap.put(Packet.HELLO, new HelloPacket());
        Packet.packetMap.put(Packet.AUTHORIZE, new AuthorizePacket());
        
        Packet.packetMap.put(Packet.SETSTATE, new SetStatePacket());
        Packet.packetMap.put(Packet.GETSTATE, new GetStatePacket());
        
        Packet.packetMap.put(Packet.CALL_BEGIN, new CallBeginPacket());
        
        Packet.packetMap.put(Packet.CALL_ANSWER, new CallAnswerPacket());
        Packet.packetMap.put(Packet.CALL_REJECT, new CallRejectPacket());
        
        Packet.packetMap.put(Packet.CALL_HOLD, new CallHoldPacket());
        Packet.packetMap.put(Packet.CALL_UNHOLD, new CallUnholdPacket());
        
        Packet.packetMap.put(Packet.CALL_END, new CallEndPacket());
        
    }
    
    public static Packet read(ChannelBuffer buffer) throws IOException {
        Integer id = buffer.readInt(); // Получаем ID пришедшего пакета, чтобы определить, каким классом его читать
        Packet packet = (Packet)Packet.packetMap.get(id);
        if( null == packet){
            packet = new ErrorPacket();
            ((ErrorPacket)packet).setMessage("Unknown package with id " + id);
           Server.logger.error("?? Unknown packet: " + id);
           Packet.write(packet, buffer);
        } else {
            try {
                packet = packet.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Server.logger.error("Cannot instaniate "+Packet.class.getName());
            }
        }
        packet.get(buffer); // Читаем в пакет данные из буфера*/
        Server.logger.trace("<< "+packet.getInfo());
        Server.acceptPacket();
        return packet;
    }
    
    @Override
    public Packet clone() throws CloneNotSupportedException{
        return (Packet)super.clone();
    }

    // Проблема с записью
    //:TODO События от циски идут синхронно, наблюдается проблема
    // Когда приходит событие о смене статуса и события начала звонка
    // Пакеты синхронно пишутся в поток что сбивает работу, надо решить этот вопрос
    // синхронизацией записи в канал клиента
    // В данный момент идет синхронизация записи для Всех(!) подключенных операторов
    // Что в боевых условиях бред
    public static void write(Packet packet, ChannelBuffer buffer) {
        buffer.writeInt(packet.getId()); // Отправляем ID пакета
        buffer.writeInt(packet.getPacketId()); // Отправляем ID пакета
        
        String t = buffer.writerIndex() + " / " + buffer.readableBytes();
        
        packet.send(buffer); // Отправляем данные пакета
        Server.sendPacket();
        
        Server.logger.trace(">> [" + packet.getPacketId() + "] "+packet.getInfo()+" "+t);
    }
    
    private Integer packetLID = 1;
    
    public Packet(){
        //packetId = (int)(new Date().getTime());
        packetLID = packetId++;
    }
    final public int getPacketId(){
        return packetLID;
    }
    
    final public String getClassName(){
        return this.getClass().toString();
    }
    
    public String getInfo(){
        return this.getClassName();
    }

    // Функции, которые должен реализовать каждый класс пакета
    public abstract Integer getId();
    public abstract void get(ChannelBuffer buffer);
    public abstract void send(ChannelBuffer buffer);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Gateway;

import Client.Gateway.Packets.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author bsalmanov
 */
public abstract class Packet implements Cloneable {
    protected static Map<Integer, Packet> packetMap = new HashMap();
    public static Integer packetId = 0;
    
    final public static Integer HELLO = 1;
    final public static Integer WARNING = 2;
    
    final public static Integer ERROR = 3;
    final public static Integer AUTHORIZE = 4;
    
    final public static Integer SETSTATE = 5;
    final public static Integer GETSTATE = 6;
    final public static Integer UNKNOWN = 7;
    final public static Integer CALLSTOP = 8;
    final public static Integer CALLACCEPT = 9;
    final public static Integer CALLDECLINE = 10;
    final public static Integer CALLBEGIN = 11;
    final public static Integer CALLCLEAR = 12;
    
    final public static Integer HOLD = 13; // ----
    final public static Integer UNHOLD = 14; // ----
    
    final public static Integer REJECT = 15; // ----
    final public static Integer ANSWER = 17; // ----
    
    final public static Integer BTNMASK = 18; // ----
    
    final public static Integer NULL = 999;
    
    static {
        Packet.packetMap.put(Packet.HELLO, new HelloPacket());
        Packet.packetMap.put(Packet.AUTHORIZE, new AuthorizePacket());
        Packet.packetMap.put(Packet.UNKNOWN, new UnknownPacket());
        
        Packet.packetMap.put(Packet.SETSTATE, new SetStatePacket());
        Packet.packetMap.put(Packet.GETSTATE, new GetStatePacket());
        
        Packet.packetMap.put(Packet.CALLSTOP, new CallStopPacket());
        Packet.packetMap.put(Packet.CALLACCEPT, new CallAcceptPacket());
        Packet.packetMap.put(Packet.CALLDECLINE, new CallDeclinePacket());
        Packet.packetMap.put(Packet.CALLBEGIN, new CallBeginPacket());
        Packet.packetMap.put(Packet.CALLCLEAR, new CallClearPacket());
        
        Packet.packetMap.put(Packet.NULL, new NullPacket());
    }
    
    public static Packet read(ChannelBuffer buffer) throws IOException {
        Integer id = buffer.readInt(); // Получаем ID пришедшего пакета, чтобы определить, каким классом его читать
        Packet packet = (Packet)Packet.packetMap.get(id);
        if( null == packet){
            packet = new UnknownPacket();
            ((UnknownPacket)packet).setPacketCode(id);
           Server.logger.error("?? Unknown packet: "+id);
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
    //:TODO События от циски идут синхронно, наблюдаентся проблема
    // Когда приходит событие о смене статуса и события начала звонка
    // Пакеты синхронно пишутся в поток что сбивает работу, надо решить этот вопрос
    // синхронизацией записи в канал клиента
    // В данный момент идет синхронизация записи для Всех(!) подключенных операторов
    // Что в боевых условиях бред
    public synchronized static void write(Packet packet, ChannelBuffer buffer) {
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

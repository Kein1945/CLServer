/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Daemon;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bsalmanov
 */
public final class Argument {
    private Map<Integer, Integer> intData = new HashMap();
    private Map<Integer, String> strData = new HashMap();
    
    public Integer getIntValue(Integer key){
        if( intData.containsKey(key) ){
            return intData.get(key);
        }
        return null;
    }
    
    public void setIntValue(Integer key, Integer value){
        intData.put(key, value);
    }
    
    public String getStringValue(Integer key){
        if( strData.containsKey(key) ){
            return strData.get(key);
        }
        return null;
    }
    
    public void setStringValue(Integer key, String value){
        strData.put(key, value);
    }
}

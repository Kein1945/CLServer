/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CTI.Call;

/**
 *
 * @author bsalmanov
 */
public class CompositeCall extends AbstractCall implements CallInterface{

    public CompositeCall(String agentLogin, String callID) {
        super(agentLogin, callID);
    }   
}

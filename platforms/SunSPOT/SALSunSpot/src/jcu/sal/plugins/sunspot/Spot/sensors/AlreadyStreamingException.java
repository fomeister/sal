/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors;

/**
 *
 * @author gilles
 */
public class AlreadyStreamingException extends Exception{
    public AlreadyStreamingException(){
        super();
    }

    public AlreadyStreamingException(String m){
        super(m);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors;

/**
 *
 * @author gilles
 */
public interface IDataThread extends Runnable{
    public void start();
    public void stop();
}

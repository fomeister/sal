/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcu.sal.plugins.sunspot.Spot.sensors;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class maintains a map of ids and associated {@link StreamingThread}s.
 *
 * @author gilles
 */
public class ThreadPool {
    private final Hashtable threads;

    /**
     * This method creates a new thread pool
     */
    public ThreadPool(){
        threads = new Hashtable();
    }

    /**
     * This method adds a new {@link StreamingThread} to this pool.
     * This method also calls {@link StreamingThread#start()}.
     * @param id the id of the thread
     * @param st the streaming thread
     * @return true if the thread has been added, false if another
     * thread with the same id already exists
     */
    public boolean addThread(String id, IDataThread st){
        synchronized(threads){
            if (threads.containsKey(id))
                return false;

            System.out.println("Adding & starting data thread '"+id+"' to pool");
            threads.put(id, st);
            st.start();
        }
        return true;
    }

    /**
     * This method removes a {@link StreamingThread} from this pool, given its
     * id. This method also calls {@link StreamingThread#stop()}.
     * @param id the id of the thread
     */
    public void removeThread(String id){
        synchronized(threads){
            if(threads.containsKey(id)){
                System.out.println("Stopping & removing data thread '"+id+"' from pool");
                ((IDataThread) threads.remove(id)).stop();
            } else
                System.out.println("Stream not started - cant stop it");
        }
    }

    /**
     * this method removes and stops all streaming threads
     */
    public void removeAll(){
        Vector v = new Vector();
        synchronized(threads){
            Enumeration e = threads.keys();
            while(e.hasMoreElements())
                v.addElement(e.nextElement());

            for(int i = 0; i<v.size();i++)
                removeThread((String) v.elementAt(i));
        }
    }
}

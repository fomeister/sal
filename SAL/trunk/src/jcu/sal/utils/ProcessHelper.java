package jcu.sal.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class ProcessHelper {
	
	private static Logger logger = Logger.getLogger(ProcessHelper.class);
	static {
		Slog.setupLogger(logger);
	}

	/**
	 * Creates a new instance of a process from a command line
	 * @param cmdline the command to be run with any arguments it needs
	 * @return the new process
	 * @throws IOException if there is a problem creating the new process
	 */
	public static Process createProcess(String cmdline) throws IOException {
		return Runtime.getRuntime().exec(cmdline);
	}
	
	/**
	 * captures the standard output and error channel of a command
	 * @param cmdline the command to be run with any arguments it needs
	 * @return an array of 2 BufferedReaders ([0]: stdout, [1]: stderr)
	 * @throws IOException if there is a problem creating the new process
	 */
	public static BufferedReader[] captureOutputs(String cmdline) throws IOException {
		BufferedReader[] b = new BufferedReader[2];
		Process p = ProcessHelper.createProcess(cmdline);
		b[0] = new BufferedReader(new InputStreamReader(p.getInputStream()));
		b[1] = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		return b;
	}
	
	/**
	 * Gets the PID(s) of a process(es) whose name(s) matches a given pattern
	 * @param pattern the pattern
	 * @return the PIDs
	 * @throws IOException 
	 */
	public static ArrayList<Integer> getPid(String pattern) throws IOException {
		ArrayList<Integer> pids = new ArrayList<Integer>();		
		Process p;

		p = createProcess("pgrep " + pattern);
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while(b.ready()) { pids.add(Integer.parseInt(b.readLine())); }
		return pids;
		
	}
	
	/**
	 * Check if a process is currently running and return its arguments if it is running.
	 * If more than one process matches the pattern, the return value contains the
	 * arguments of all matching processes
	 * @param pattern the pattern used to match processes
	 * @return the arguments
	 */
	public static Hashtable<String,String> getRunningProcessArgs(String pattern)  {
		Hashtable<String,String> args = new Hashtable<String,String>();  
		ArrayList<Integer> pids = new ArrayList<Integer>();
		BufferedReader b = null;
		int instance = 0;

		try {
			pids = getPid(pattern);
			Iterator<Integer> iter = pids.iterator();
			while(iter.hasNext()) {
				//reads /proc/pid/cmdline and add it to args
				b =  new BufferedReader(new FileReader("/proc/" + String.valueOf(iter.next()) + "/cmdline"));
				String[] t = b.readLine().split("\00");
				String name = t[0] + "(instance " + String.valueOf(++instance) + ")";
				for (int i = 1; i < t.length; i++) {
					System.out.println("string " + String.valueOf(i) + " : "+t[i]);
					args.put(name, t[i]);
				}
			}

		} catch (IOException e) {
			logger.error("Error getting process args");
			e.printStackTrace();
		}
		return args;
	}
}

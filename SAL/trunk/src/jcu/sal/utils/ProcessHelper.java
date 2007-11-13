package jcu.sal.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
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
	 * captures the exit value, the standard output and error channel of a command
	 * @param cmdline the command to be run with any arguments it needs
	 * @return an array of 3 BufferedReaders ([0]: stdout, [1]: stderr, [2]: exit value)
	 * @throws IOException if there is a problem creating the new process
	 */
	public static BufferedReader[] captureOutputs(String cmdline) throws IOException {
		BufferedReader[] b = new BufferedReader[3];
		Process p = ProcessHelper.createProcess(cmdline);
		b[0] = new BufferedReader(new InputStreamReader(p.getInputStream()));
		b[1] = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		try {
			b[2] = new BufferedReader(new StringReader(String.valueOf(p.waitFor())));
		} catch (InterruptedException e) {
			System.err.println("The command '" + cmdline + "' has been interrupted" );
			throw new IOException();
		}
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
	
	public static void main(String[] args) throws IOException {
		BufferedReader[] b = captureOutputs("cut -f3 -d' ' /proc/loadavg");
		String out, err;
		out = b[0].readLine();
		err = b[1].readLine();
		int e = Integer.parseInt(b[2].readLine());
		System.out.println("gello \n" + out + "\n" +err + "\n" +e);
	
		
		/* String[] a = {"cut", "/proc/loadavg", "-f3", "-d' '"};
		ProcessBuilder pb = new ProcessBuilder(a);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		BufferedReader bb = new BufferedReader(new InputStreamReader(p.getInputStream()));
		try {
			System.out.println("exit value: " +p.waitFor());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		*/
		
		
		Process p = Runtime.getRuntime().exec("cat /proc/loadavg");
		BufferedReader[] bb = new BufferedReader[2]  
		bb[0] = new BufferedReader(new InputStreamReader(p.getInputStream()));
		bb[1] = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		try {
			System.out.println("exit value: " +p.waitFor());
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("out: " + bb[0].readLine());
		System.out.println("err: " + bb[1].readLine());
		
	}
}

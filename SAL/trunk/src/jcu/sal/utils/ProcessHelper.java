package jcu.sal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessHelper {

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
	 * Check if a process is currently running and return its arguments if it is running
	 * @param name the name of the executable
	 * @return the arguments
	 */
	public static String[] checkRunningProcess(String name) {
		String[] args = new String[10];
		String  pid="";
		try {
			Process p = createProcess("pgrep " + name);
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while(b.ready()) pid = b.readLine();
			
		}
		return args;
	}
}


package jcu.sal.plugins;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.lang.reflect.Modifier;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


// ===========================================================================
// TODO
// For the next version
// - detect unresolved links to classes / duplicate definitions
//   - have the option to either
//     - remove offending jars/files until next update and hope it's been fixed
//       - need to do logging so people know there's a problem
//     - add it to a list of suspended files and methods to browse/unsuspend those file
//       - also needs to work in combination with the logger
//     - fail with an error
//   - have to do the same for all files that depend on troubled files
//     - dependency chain might be nice when reporting on why a file has been acted on
// ===========================================================================

public class DirectoryClassLoader extends ClassLoader {

	private File directory;
	private URL directoryURL;

	private HashMap<String, FileInfo> classes;
	private HashMap<String, FileInfo> resources;
	private Set<String> managedFiles;

	public DirectoryClassLoader(String directoryName) {
		super();

		this.directory = new File(directoryName);

		try {
			directoryURL = this.directory.toURI().toURL();
		} catch (MalformedURLException mue) {
		}

		classes = new HashMap<String, FileInfo>();
		resources = new HashMap<String, FileInfo>();
		managedFiles = new HashSet<String>();
	}

// ===========================================================================
// Public API methods
// ===========================================================================

	public List<String> getClassNames() {
		ArrayList<String> names = new ArrayList<String>();

		for (FileInfo fi : classes.values()) {
			names.add(fi.name);
		}

		return names;
	}

	public List<String> getClassNames(Class interfaceToMatch) {
		ArrayList<String> names = new ArrayList<String>();

		if (!interfaceToMatch.isInterface()) {
			return names;
		}

		for (FileInfo fi : classes.values()) {
			fi.loadClassData();
			if(implementsInterfaceAndConcrete(fi.loadedClass, interfaceToMatch)) {
				names.add(fi.name);
			}
		}

		return names;
	}

	public List<String> getResourceNames() {
		ArrayList<String> names = new ArrayList<String>();

		for (FileInfo fi : resources.values()) {
			names.add(fi.name);
		}

		return names;
	}

	public void update() {
		updateClasses();
		updateResources();
		processDirectory(directory);
		loadAllLazyClasses();
	}

// ===========================================================================
// Overloaded superclass methods
// ===========================================================================

	public Class loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, true);
	}

	public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class result = null;

		try {
			result = super.loadClass(name, resolve);
			return result;
		} catch (ClassNotFoundException cnfe) {
		}

		updateClass(name);

		FileInfo fi = (FileInfo) classes.get(name);
		if (fi != null) {
			fi.loadClassData();
			return fi.loadedClass;
		} else {
			throw new ClassNotFoundException();
		}
	}

	public URL findResource(String name) {
		updateResource(name);

		FileInfo resource = (FileInfo) resources.get(name);

		if (resource == null) {
			return null;
		}

		return resource.location;
	}

	public Enumeration<URL> findResources(String name) {
		Vector<URL> v = new Vector<URL>();

		URL resourceURL = findResource(name);

		if (resourceURL != null) {
			v.add(resourceURL);
		}

		return v.elements();
	}

// ===========================================================================

	private boolean implementsInterfaceAndConcrete(Class testClass, Class interfaceToMatch) {
		if (testClass.isInterface() ||
				Modifier.isAbstract(testClass.getModifiers()) ||
				!Modifier.isPublic(testClass.getModifiers())) {
			return false;
		}

		return interfaceToMatch.isAssignableFrom(testClass);
	}

	private void updateClasses() {
		String[] names = classes.keySet().toArray(new String[0]);
		for (int i = 0; i < names.length; ++i) {
			updateClass(names[i]);
		}
	}

	private void updateClass(String name) {
		FileInfo pi = (FileInfo) classes.get(name);

		if (pi == null) {
			return;
		}

		String fileName = null;
		try {
			fileName = pi.source.getCanonicalPath();
		} catch(IOException ioe) {
			return;
		}

		if (!pi.exists()) {
			classes.remove(name);
			managedFiles.remove(fileName);
		} else if (pi.updated()){

			if (fileName.endsWith(".class")) {
				processClassFile(pi.source);
			} else if (fileName.endsWith(".jar")){
				processJarFile(pi.source);
			}
		}
	}

	private void updateResources() {
		String[] names = resources.keySet().toArray(new String[0]);
		for (int i = 0; i < names.length; ++i) {
			updateResource(names[i]);
		}
	}

	private void updateResource(String name) {
		FileInfo pi = (FileInfo) resources.get(name);

		if (pi == null) {
			return;
		}

		String fileName = null;
		try {
			fileName = pi.source.getCanonicalPath();
		} catch(IOException ioe) {
			return;
		}

		if (!pi.exists()) {
			resources.remove(name);
			managedFiles.remove(fileName);
		} else if (pi.updated()){
			if (fileName.endsWith(".jar")) {
				processJarFile(pi.source);
			} else {
				processResourceFile(pi.source);
			}
		}
	}

	private void processDirectory(File dir) {
		if (dir.exists() && dir.isDirectory()) {

			File[] files = dir.listFiles();

			for (int i = 0; i < files.length; i++) {

				if (files[i].isDirectory()) {
					processDirectory(files[i]);
				} else {

					String fileName = null;
					try {
						fileName = files[i].getCanonicalPath();
					} catch (IOException ioe) {
						continue;
					}

					if (!managedFiles.contains(fileName)) {

						managedFiles.add(fileName);

						if (fileName.endsWith(".class")) {
							processClassFile(files[i]);
						} else if (fileName.endsWith(".jar")) {
							processJarFile(files[i]);
						} else {
							processResourceFile(files[i]);
						}
					}
				}
			}
		}
	}

	private static URL fileToURL(File f) {
		try {
			return f.toURI().toURL();
		} catch (MalformedURLException mue) {
			return null;
		}
	}

	private String getRelativeFilename(URL url) {
		String protocol = url.getProtocol();
		String filename = url.getFile();

		if (protocol.equals("file")) {
			filename = filename.substring(filename.lastIndexOf(directoryURL.getFile()) + directoryURL.getFile().length());
		} else if(protocol.equals("jar")) {
			filename = filename.substring(filename.lastIndexOf("!/") + 2);
		}

		return filename;
	}

	private String getClassName(URL url) {
		String name = getRelativeFilename(url);
		name = name.substring(0, name.lastIndexOf(".class")).replace('/', '.');
		return name;
	}

	private void processClassFile(File file) {
		byte[] b = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(b);
			fis.close();
		} catch (IOException ioe) {
			return;
		}

		URL url = fileToURL(file);
		if (url != null) {
			String name = getClassName(url);
			classes.put(name, new FileInfo(this, name, file, url, b));
		}
	}

	private void processJarFile(File file) {
		URL fileURL = fileToURL(file);

		if (fileURL == null) {
			return;
		}

		try {
			JarInputStream jis = new JarInputStream(new FileInputStream(file));
			JarEntry je = null;

			while ((je = jis.getNextJarEntry()) != null) {
				if(je.getSize() != 0) {
					URL url = new URL("jar", "", fileURL + "!/" + je.getName());
					if (je.getName().endsWith(".class")) {
						byte[] data = new byte[(int) je.getSize()];

						int size = (int) je.getSize();
						int read = 0;
						while (read != size) {
							read += jis.read(data, read, size - read);
						}

						String name = getClassName(url);
						classes.put(name, new FileInfo(this, name, file, url, data));
					} else {
						processResourceFile(file, url);
					}
				}
			}

			jis.close();
		} catch (IOException ioe) {
		}
	}

	private void processResourceFile(File source) {
		URL url = fileToURL(source);
		if (url != null) {
			processResourceFile(source, url);
		}
	}

	private void processResourceFile(File source, URL location) {
		String filename = getRelativeFilename(location);
		resources.put(filename, new FileInfo(this, filename, source, location));
	}

	private void loadAllLazyClasses() {
		for (String name : classes.keySet()) {
			FileInfo fi = classes.get(name);
			if (fi != null) {
				fi.loadClassData();
			}
		}
	}

	private class FileInfo extends ClassLoader {
		public DirectoryClassLoader parent;
		public String name;
		public File source;
		public long lastModified;
		public URL location;
		public byte[] classData;
		public Class loadedClass;

		// for classes
		public FileInfo(DirectoryClassLoader parent, String name, File source, URL location, byte[] classData) {
			super(parent);

			this.parent = parent;
			this.name = name;
			this.source = source;
			this.lastModified = source.lastModified();
			this.location = location;
			this.classData = classData;
			this.loadedClass = null;
		}

		// for resources
		public FileInfo(DirectoryClassLoader parent, String name, File source, URL location) {
			super(parent);

			this.parent = parent;
			this.name = name;
			this.source = source;
			this.lastModified = source.lastModified();
			this.location = location;
			this.loadedClass = null;
		}

		public boolean exists() {
			return source.exists();
		}

		public boolean updated() {
			return (source.lastModified() != lastModified);
		}

		public void loadClassData() {
			if (loadedClass == null && classData != null) {
				try {
					loadedClass = defineClass(name, classData, 0, classData.length);
				} catch (ClassFormatError cfe) {
					loadedClass = null;
				}
				classData = null;
			}
		}

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (o == null || !(o instanceof FileInfo)) {
				return false;
			}

			FileInfo fi = (FileInfo) o;

			if ((loadedClass == null) != (fi.loadedClass == null)) {
				return false;
			}

			if ((classData == null) != (fi.classData == null)) {
				return false;
			}

			if (loadedClass != null && fi.loadedClass != null && !loadedClass.equals(fi.loadedClass)) {
				return false;
			}

			if (classData != null && fi.classData != null && !classData.equals(fi.classData)) {
				return false;
			}

			return (name.equals(fi.name) && location.equals(fi.location) && lastModified == fi.lastModified && loadedClass == fi.loadedClass && classData == fi.classData);
		}

		public int hashCode() {
			return name.hashCode();
		}
	}

// ===========================================================================
// Main class for preliminary manual testing
// ===========================================================================

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: DirectoryClassLoader [directory]");
			System.exit(1);
		}

		DirectoryClassLoader dcl = new DirectoryClassLoader(args[0]);

		System.out.println(dcl.getClass().getCanonicalName());

		while (true) {
			dcl.update();

			List<String> classNames = dcl.getClassNames();
			System.out.println("Class names:");
			for (String cn: classNames) {
				System.out.println("\t" + cn);
			}

			List<String> resourceNames = dcl.getResourceNames();
			System.out.println("Resource names:");
			for (String rn: resourceNames) {
				System.out.println("\t" + rn);
			}

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}

			try {
				Object o = dcl.loadClass("au.edu.jcu.hpc.srb.mcatext.notify.dummy.PathNotificationListenerImpl").newInstance();
			} catch (ClassNotFoundException cnfe) {
				System.out.println("Class not found");
			} catch (InstantiationException ie) {
				System.out.println("Could not create class");
			} catch (IllegalAccessException iae) {
				System.out.println("Could not access class");
			}
		}
	}
}

package flashsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import foxtrot.Job;
import foxtrot.Worker;
import gui.About;
import gui.FlasherGUI;

import org.adb.AdbUtility;
import org.logger.MyLogger;
import org.plugins.PluginInterface;
import org.system.AdbPhoneThread;
import org.system.ClassPath;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.Shell;
import org.system.StatusEvent;
import org.system.StatusListener;


public class FlasherConsole {
	
	private static AdbPhoneThread phoneWatchdog;
	private static String fsep = OS.getFileSeparator();
	private static Properties plugins = new Properties();
	
	public static void init(boolean withadb) {
			MyLogger.disableTextArea();
			MyLogger.setLevel("info");
			MyLogger.getLogger().info("Flashtool "+About.getVersion());
			FlasherGUI.guimode=false;
			if (withadb) {
			StatusListener phoneStatus = new StatusListener() {
				public void statusChanged(StatusEvent e) {
					if (!e.isDriverOk()) {
						MyLogger.getLogger().error("Drivers need to be installed for connected device.");
						MyLogger.getLogger().error("You can find them in the drivers folder of Flashtool.");
					}
					else {
						if (e.getNew().equals("adb")) {
							MyLogger.getLogger().info("Device connected with USB debugging on");
							MyLogger.getLogger().debug("Device connected, continuing with identification");
							doIdent();
						}
						if (e.getNew().equals("none")) {
							MyLogger.getLogger().info("Device disconnected");
						}
						if (e.getNew().equals("flash")) {
							MyLogger.getLogger().info("Device connected in flash mode");
						}
						if (e.getNew().equals("fastboot")) {
							MyLogger.getLogger().info("Device connected in fastboot mode");
						}
						if (e.getNew().equals("normal")) {
							MyLogger.getLogger().info("Device connected with USB debugging off");
							MyLogger.getLogger().info("For 2011 devices line, be sure you are not in MTP mode");
						}
					}
				}
			};
			phoneWatchdog = new AdbPhoneThread();
			phoneWatchdog.start();
			phoneWatchdog.addStatusListener(phoneStatus);
			}
			else DeviceChangedListener.start();
	}

	public static void exit() {
		DeviceChangedListener.stop();
		if (phoneWatchdog!=null) {
			phoneWatchdog.done();
			try {
				phoneWatchdog.join();
			}
			catch (Exception e) {
			}
		}
		MyLogger.writeFile();
		System.exit(0);
	}
	
	public static void doRoot() {
		Devices.waitForReboot(false);
		if (Devices.getCurrent().getVersion().contains("2.3")) {
			if (!Devices.getCurrent().hasRoot())
				doRootzergRush();
			else MyLogger.getLogger().error("Your device is already rooted");
		}
		else 
			if (!Devices.getCurrent().hasRoot())
				doRootpsneuter();
			else MyLogger.getLogger().error("Your device is already rooted");
		exit();
	}

	public static void doRootzergRush() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push(new File("."+fsep+"custom"+fsep+"root"+fsep+"zergrush.tar.uue").getAbsolutePath(),GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(true);
					Devices.waitForReboot(true);
					MyLogger.getLogger().info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.getLogger().info("Finished!.");
					MyLogger.getLogger().info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
	}

	public static void doRootpsneuter() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"psneuter.tar.uue",GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(false);
					Devices.waitForReboot(true);
					MyLogger.getLogger().info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.getLogger().info("Finished!.");
					MyLogger.getLogger().info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
	}

	public static void doBLUnlock() {
		try {
			Devices.waitForReboot(false);
			PluginInterface po = (PluginInterface)plugins.get("Bootloader Unlock");
			po.run();
		}
		catch (Exception e) {
			MyLogger.getLogger().error("The BootLoader Unlock plugin is not loaded");
		}
	}
	
	public static void doGetIMEI() throws Exception {
		X10flash f=null;
		try {
			Bundle b = new Bundle();
			b.setSimulate(false);
			f = new X10flash(b);
			MyLogger.getLogger().info("Please connect your phone in flash mode");
			while (!f.deviceFound());
			f.openDevice(false);
			MyLogger.getLogger().info("IMEI : "+f.getPhoneProperty("IMEI"));
			f.closeDevice();
			exit();
		}
		catch (Exception e) {
			if (f!=null) f.closeDevice();
			throw e;
		}		
	}
	
	public static void doFlash(String file,boolean wipedata,boolean wipecache,boolean excludebb,boolean excludekrnl, boolean excludesys) throws Exception {
		X10flash f=null;
		try {
			File bf = new File(file);
			if (!bf.exists()) {
				MyLogger.getLogger().error("File "+bf.getAbsolutePath()+" does not exist");
				exit();
			}
			MyLogger.getLogger().info("Choosed "+bf.getAbsolutePath());
			Bundle b = new Bundle(bf.getAbsolutePath(),Bundle.JARTYPE);
			b.setSimulate(false);
			b.getMeta().setCategEnabled("DATA", wipedata);
			b.getMeta().setCategEnabled("CACHE", wipecache);
			b.getMeta().setCategEnabled("BASEBAND", excludebb);
			b.getMeta().setCategEnabled("SYSTEM", excludesys);
			b.getMeta().setCategEnabled("KERNEL", excludekrnl);
			MyLogger.getLogger().info("Preparing files for flashing");
			b.open();
			f = new X10flash(b);
			MyLogger.getLogger().info("Please connect your phone in flash mode");
			while (!f.deviceFound());
			f.openDevice(false);
			f.flashDevice();
			b.close();
			exit();
		}
		catch (Exception e) {
			if (f!=null) f.closeDevice();
			throw e;
		}		
	}

	public static void doIdent() {
    		Enumeration<Object> e = Devices.listDevices(true);
    		if (!e.hasMoreElements()) {
    			MyLogger.getLogger().error("No device is registered in Flashtool.");
    			MyLogger.getLogger().error("You can only flash devices.");
    			return;
    		}
    		boolean found = false;
    		Properties founditems = new Properties();
    		founditems.clear();
    		Properties buildprop = new Properties();
    		buildprop.clear();
    		while (e.hasMoreElements()) {
    			DeviceEntry current = Devices.getDevice((String)e.nextElement());
    			String prop = current.getBuildProp();
    			if (!buildprop.containsKey(prop)) {
    				String readprop = DeviceProperties.getProperty(prop);
    				buildprop.setProperty(prop,readprop);
    			}
    			Iterator<String> i = current.getRecognitionList().iterator();
    			String localdev = buildprop.getProperty(prop);
    			while (i.hasNext()) {
    				String pattern = i.next().toUpperCase();
    				if (localdev.toUpperCase().contains(pattern)) {
    					founditems.put(current.getId(), current.getName());
    				}
    			}
    		}
    		if (founditems.size()==1) {
    			found = true;
    			Devices.setCurrent((String)founditems.keys().nextElement());
    			if (!Devices.isWaitingForReboot())
    				MyLogger.getLogger().info("Connected device : " + Devices.getCurrent().getId());
    		}
    		else {
    			MyLogger.getLogger().error("Cannot identify your device.");
        		MyLogger.getLogger().error("You can only flash devices.");
    		}
    		if (found) {
    			if (!Devices.isWaitingForReboot()) {
    				MyLogger.getLogger().info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion());
    				MyLogger.getLogger().info("Android version : "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion());
    			}
    			if (Devices.getCurrent().isRecovery()) {
    				MyLogger.getLogger().info("Phone in recovery mode");
    				if (!Devices.isWaitingForReboot())
    					MyLogger.getLogger().info("Root Access Allowed");
    			}
    			else {
    				boolean hasSU = Devices.getCurrent().hasSU();
    				if (hasSU) {
    					boolean hasRoot = Devices.getCurrent().hasRoot();
    					if (hasRoot)
    						if (!Devices.isWaitingForReboot())
    							MyLogger.getLogger().info("Root Access Allowed");
    				}
    			}
    			plugins.clear();
    			addDevicesPlugins();
    			addGenericPlugins();
    			MyLogger.getLogger().debug("Stop waiting for device");
    			if (Devices.isWaitingForReboot())
    				Devices.stopWaitForReboot();
    			MyLogger.getLogger().debug("End of identification");
    		}
	}

	public static void addDevicesPlugins() {
    	try {
	    	File dir = new File(Devices.getCurrent().getDeviceDir()+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" device plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			MyLogger.getLogger().debug("Registering "+p.getProperty("classname"));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("device",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {}
    }

    public static void addGenericPlugins() {
    	try {
	    	File dir = new File(OS.getWorkDir()+fsep+"custom"+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" generic plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("generic",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {
    		MyLogger.getLogger().debug(e.getMessage());
    	}
    }

    public static void registerPlugin(String type, String classname, String workdir) {
	    try {
	    	
	    	Class<?> pluginClass = Class.forName(classname);
            Constructor<?> constr = pluginClass.getConstructor();
            PluginInterface pluginObject = (PluginInterface)constr.newInstance();
            pluginObject.setWorkdir(workdir);
            boolean aenabled = false;
            String aversion = Devices.getCurrent().getVersion();
            Enumeration <String> e1 = pluginObject.getCompatibleAndroidVersions();
            while (e1.hasMoreElements()) {
            	String pversion = e1.nextElement();
            	if (aversion.startsWith(pversion) || pversion.equals("any")) aenabled=true;
            }
            
            boolean kenabled = false;
            String kversion = Devices.getCurrent().getKernelVersion();
            Enumeration <String> e2 = pluginObject.getCompatibleKernelVersions();
            while (e2.hasMoreElements()) {
            	String pversion = e2.nextElement();
            	if (kversion.equals(pversion) || pversion.equals("any")) kenabled=true;
            }
            
            boolean denabled = false;
            if (type.equals("generic")) {
	            String currdevid = Devices.getCurrent().getId();
	            Enumeration <String> e3 = pluginObject.getCompatibleDevices();
	            while (e3.hasMoreElements()) {
	            	String pversion = e3.nextElement();
	            	if (currdevid.equals(pversion) || pversion.equals("any")) denabled=true;
	            }
            }
            else
            	denabled=true;

            boolean hasroot=false;
            if (pluginObject.isRootNeeded()) hasroot=Devices.getCurrent().hasRoot();

            if (type.equals("device")&&aenabled&&kenabled&&denabled&&hasroot) {
            	plugins.put(pluginObject.getName(), pluginObject);
            }
            else
            	if (aenabled&&kenabled&&denabled&&hasroot)
            		plugins.put(pluginObject.getName(), pluginObject);
            Enumeration e = plugins.keys();
            while (e.hasMoreElements()) {
            	String pname = (String)e.nextElement();
            }
	    }
	    catch (Exception e) {
	    	MyLogger.getLogger().error(e.getMessage());
	    }
    }

}
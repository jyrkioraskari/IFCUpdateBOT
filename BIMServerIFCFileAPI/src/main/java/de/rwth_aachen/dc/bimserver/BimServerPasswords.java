package de.rwth_aachen.dc.bimserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BimServerPasswords {
    
	static public class BimServerContext {
		public String bimserver_user;
		public String bimserver_password;
	}

	static private BimServerContext bs = null;

	static public BimServerContext getContext() {
		if (bs == null) {
			try {
				File configDir = new File(System.getProperty("catalina.base"), "conf");
				File configFile = new File(configDir, "bim4ren.properties");
				if(!configFile.exists())
				{
					System.err.println("bim4ren.properties does not exist in: <tomcat>/conf");
					return null;
				}
				InputStream stream = new FileInputStream(configFile);
				Properties props = new Properties();
				props.load(stream);
				if(props.getProperty("bimserver_user")==null)
					return null;
				bs = new BimServerPasswords.BimServerContext();
				
				bs.bimserver_user = props.getProperty("bimserver_user").trim();
				bs.bimserver_password = props.getProperty("bimserver_password").trim();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bs;
	}
}

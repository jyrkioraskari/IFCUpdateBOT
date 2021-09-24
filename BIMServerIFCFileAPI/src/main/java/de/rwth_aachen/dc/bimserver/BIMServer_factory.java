package de.rwth_aachen.dc.bimserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bimserver.BimServer;
import org.bimserver.BimServerConfig;
import org.bimserver.BimserverDatabaseException;
import org.bimserver.LocalDevPluginLoader;
import org.bimserver.database.DatabaseRestartRequiredException;
import org.bimserver.database.berkeley.DatabaseInitException;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.store.ServerState;
import org.bimserver.shared.LocalDevelopmentResourceFetcher;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.interfaces.AdminInterface;
import org.bimserver.shared.interfaces.SettingsInterface;
import org.bimserver.webservices.authorization.SystemAuthorization;

import de.rwth_aachen.dc.bimserver.BimServerPasswords.BimServerContext;

public class BIMServer_factory {
	static private BimServer instance;
 static public BimServer get_instance()
 {
	 System.out.println("x1");
	 if(instance!=null)
		 return instance;
	 System.out.println("x2");
	 String folder="/etc/bim4ren_bimserver";
		System.out.println("Folder: "+folder);
		File b4r_folder=new File(folder);
		if(!b4r_folder.exists())
		   b4r_folder.mkdir();
		int port=8090;
		BimServerConfig config = new BimServerConfig();
	    config.setHomeDir(Paths.get(folder));
	    config.setResourceFetcher(new LocalDevelopmentResourceFetcher(Paths.get("../")));
	    config.setStartEmbeddedWebServer(true);
	    config.setClassPath(System.getProperty("java.class.path"));
	    config.setLocalDev(true);
	    config.setPort(port);
	    config.setStartCommandLine(true);
	    BimServer bimServer = new BimServer(config);
	    try {
	      bimServer.start();
	      if (bimServer.getServerInfo().getServerState() == ServerState.NOT_SETUP) {
	        AdminInterface adminInterface =
	            bimServer
	                .getServiceFactory()
	                .get(new SystemAuthorization(1, TimeUnit.HOURS), AccessMethod.INTERNAL)
	                .get(AdminInterface.class);
	        BimServerContext credentials = de.rwth_aachen.dc.bimserver.BimServerPasswords.getContext();
			if(credentials==null)
				adminInterface.setup(
			            "http://localhost:" + port,
			            "localhost",
			            "description",
			            null,
			            "Administrator",
			            "bim4en@bimserver.org",
			            "aaddmin");
				else
					adminInterface.setup(
				            "http://localhost:" + port,
				            "localhost",
				            "description",
				            null,
				            "Administrator",
				            credentials.bimserver_user,
				            credentials.bimserver_password);

	        
	        SettingsInterface settingsInterface =
	            bimServer
	                .getServiceFactory()
	                .get(new SystemAuthorization(1, TimeUnit.HOURS), AccessMethod.INTERNAL)
	                .get(SettingsInterface.class);
	        settingsInterface.setCacheOutputFiles(false);
	      }
	    } catch (PluginException e) {
	    	e.printStackTrace();
	    } catch (ServiceException e) {
	    	e.printStackTrace();
	    } catch (DatabaseInitException e) {
	    	e.printStackTrace();
	    } catch (BimserverDatabaseException e) {
	    	e.printStackTrace();
	    } catch (DatabaseRestartRequiredException e) {
	    	e.printStackTrace();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }


		instance=bimServer;
		return bimServer;
 }
}

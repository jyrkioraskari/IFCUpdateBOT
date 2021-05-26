package de.rwth_aachen.dc.ifc;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bimserver.BimServer;
import org.bimserver.BimServerConfig;
import org.bimserver.BimserverDatabaseException;
import org.bimserver.database.DatabaseRestartRequiredException;
import org.bimserver.database.berkeley.DatabaseInitException;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.shared.LocalDevelopmentResourceFetcher;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.interfaces.ServiceInterface;

public class IfcModel2x3 {
	public IfcModel2x3(Path ifcFilePath, Path ifcShcemaDirectory) {
		// Example
		BimServerConfig config = new BimServerConfig();
		config.setStartEmbeddedWebServer(false);
		config.setHomeDir(Paths.get("c:\\temp"));
		config.setResourceFetcher(new LocalDevelopmentResourceFetcher(Paths.get("[LOCATION]")));
		config.setClassPath(System.getProperty("java.class.path"));
		config.setPort(8080);
		config.setStartCommandLine(false);
		config.setLocalDev(true);
		config.setAutoMigrate(false);
		BimServer bimServer = new BimServer(config);
		try {
			bimServer.start();
			ServiceInterface si = bimServer.getServiceFactory().get(AccessMethod.INTERNAL).getServiceInterface();
			
		} catch (ServerException | DatabaseInitException | BimserverDatabaseException | PluginException
				| DatabaseRestartRequiredException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (UserException e) {
			e.printStackTrace();
		}	
	}

	public static void main(String[] args) {
		new IfcModel2x3(Paths.get("c:\\ifc\\20180731Dubal Herrera limpio.ifc"), Paths.get("."));
	}
}
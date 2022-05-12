package de.rwth_aachen.dc.uploadbot.service;

import java.nio.file.Path;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

public class UploadBotStardogManager {
	B4RStardogConnection stardog= new B4RStardogConnection();

	public UploadBotStardogManager() {
	}

	public void uploadTTL(Path path) {
		Runnable runnable = () -> {
			try {

				Model m = ModelFactory.createDefaultModel();
				RDFDataMgr.read(m, path.toFile().getAbsolutePath());
				
				stardog.sendModel(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

}

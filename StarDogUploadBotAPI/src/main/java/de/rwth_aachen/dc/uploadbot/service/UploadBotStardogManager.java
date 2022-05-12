package de.rwth_aachen.dc.uploadbot.service;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class UploadBotStardogManager {
	B4RStardogConnection stardog= new B4RStardogConnection();

	public UploadBotStardogManager() {
	}

	public void uploadTTL(Path path) {
		Runnable runnable = () -> {
			try {
				Model m = ModelFactory.createDefaultModel();
				m.read(new FileInputStream(path.toFile().getAbsoluteFile()),null,"TTL");
				
				stardog.sendModel(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public static void main(String[] args) {
		UploadBotStardogManager u=new UploadBotStardogManager();
		u.uploadTTL(Paths.get("C:\\jo\\2022-05-12_B4R-Stardog\\Connection_test1.ttl"));
	}
}

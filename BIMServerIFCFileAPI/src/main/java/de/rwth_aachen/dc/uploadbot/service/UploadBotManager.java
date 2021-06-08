package de.rwth_aachen.dc.uploadbot.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SObjectState;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;

import de.rwth_aachen.dc.BimServerPasswords;

public class UploadBotManager {

	public File uploadRelease(String projectName, java.nio.file.Path file) {
		try {
			JsonBimServerClientFactory factory = new JsonBimServerClientFactory("http://localhost:8090");
			BimServerClient client = factory.create(
					new UsernamePasswordAuthenticationInfo(BimServerPasswords.user, BimServerPasswords.password));

			List<SProject> projects = client.getServiceInterface().getAllReadableProjects();
			boolean project_exists=false;
			for (SProject p : projects) {
				if (p.getState() == SObjectState.ACTIVE)
					if (p.getName().equals(projectName)) {
						project_exists=true;
						break;
					}
			}
			
			if(!project_exists)
			{
				SProject p =client.getServiceInterface().addProject(projectName, "ifc2x3tc1"); 
				
				SDeserializerPluginConfiguration deserialize=client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", p.getOid());
				
				client.checkinSync(p.getOid(), "AUTOMATIC UPDATE", deserialize.getOid(), false, file);
				
			}
			
		} catch (BimServerClientException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	
	public File downloadLastRelease(String projectName) {
		try {
			JsonBimServerClientFactory factory = new JsonBimServerClientFactory("http://localhost:8090");
			BimServerClient client = factory.create(
					new UsernamePasswordAuthenticationInfo(BimServerPasswords.user, BimServerPasswords.password));

			List<SProject> projects = client.getServiceInterface().getAllReadableProjects();
			byte[] data = null;
			for (SProject p : projects) {
				if (p.getState() == SObjectState.ACTIVE)
					if (p.getLastRevisionId() >= 0 && p.getName().equals(projectName)) {

						System.out.println(p.getName() + " " + p.getState().name());
						SRevision revision = client.getServiceInterface().getRevision(p.getLastRevisionId());

						SSerializerPluginConfiguration serializerByContentType = client.getServiceInterface()
								.getSerializerByContentType("application/ifc");
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						client.download(revision.getOid(), serializerByContentType.getOid(), outputStream);
						data = outputStream.toByteArray();
						System.out.println("len: " + data.length);

						File tempFile = File.createTempFile("ifc2lbd", ".ifc");
						FileOutputStream fo = new FileOutputStream(tempFile);
						fo.write(data);
						fo.close();
						return tempFile;
					}
			}
		} catch (BimServerClientException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

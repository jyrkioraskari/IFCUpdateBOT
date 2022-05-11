package de.rwth_aachen.dc.uploadbot.rest;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.gson.Gson;

import de.rwth_aachen.dc.uploadbot.data.Service;


/*
 * Jyrki Oraskari, 2022
 * 
 */

@Path("/")
public class StarDogUploadBotAPI_OpenAPI {
	
	public StarDogUploadBotAPI_OpenAPI()
	{
	}
	
	/**
	 * The Open Stardog BIM Bot description specification of the services
	 */
	@GET
	@Path("/services")
	@Produces(MediaType.APPLICATION_JSON)
	public Response services_list(@Context UriInfo uriInfo) {
		List<Service> services = new ArrayList<>();
		System.out.println(uriInfo.getBaseUri().getHost());
	    System.out.println(uriInfo.getBaseUri().getPort());
		services.add(new Service(3031,"StarDogUpload", "StarDogUploadBotAPI RDF TTL Model Upload, default project", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/StarDogUploadBotAPI/api/upload", null, null));
	    String json = new Gson().toJson(services);
		return Response.ok(json, "application/json").build();
	}
	
	
	



	/**
	 * @param RDF file in TTL MULTIPART_FORM_DATA format
	 * @return Status
	 */
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.TEXT_PLAIN})
	public Response uploadIFCtoProjectAsMultiPartFormData(@FormDataParam("ifcFile") InputStream ifcFile) {
		try {
			File tempIfcFile = File.createTempFile("bimserver-", ".ifc");
			tempIfcFile.deleteOnExit();

			Files.copy(ifcFile, tempIfcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(ifcFile);
			//upload_manager.uploadRelease("default",tempIfcFile.toPath());
			return Response.ok("Upload thread started.").build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}
	
	

   
	

}
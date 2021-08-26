package de.rwth_aachen.dc.uploadbot.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.gson.Gson;

import de.rwth_aachen.dc.uploadbot.data.Service;
import de.rwth_aachen.dc.uploadbot.service.UpdateBotManager;
import de.rwth_aachen.dc.uploadbot.service.UploadBotManager;


/*
 * Jyrki Oraskari, 2020
 */

@Path("/")
public class BIMServerModelUpdate_OpenAPI {
	UploadBotManager upload_manager = new UploadBotManager();
	UpdateBotManager update_manager = new UpdateBotManager();
	

	@GET
	@Path("/services")
	@Produces(MediaType.APPLICATION_JSON)
	public Response services_list(@Context UriInfo uriInfo) {
		List<Service> services = new ArrayList<>();
		System.out.println(uriInfo.getBaseUri().getHost());
	    System.out.println(uriInfo.getBaseUri().getPort());
		services.add(new Service(3031,"BIMServerUpdate", "BIMServer IFC Model Update, default project", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/BIMServerIFCFileAPI/api/update", null, null));
		services.add(new Service(3031,"BIMServerUpload", "BIMServer IFC Model Upload, default project", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/BIMServerIFCFileAPI/api/upload", null, null));
		services.add(new Service(3032,"BIMServerUpload", "BIMServer IFC Model Upload", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/BIMServerIFCFileAPI/api/upload/{project_id}", null, null));
		services.add(new Service(3033,"BIMServerDownload", "BIMServer IFC Model Download, default project", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/BIMServerIFCFileAPI/api/download", null, null));
		services.add(new Service(3034,"BIMServerDownload", "BIMServer IFC Model Download", "BIM4REN", null, "http://"+uriInfo.getBaseUri().getHost()+"/BIMServerIFCFileAPI/api/download/{project_id}", null, null));
	    String json = new Gson().toJson(services);
		return Response.ok(json, "application/json").build();
	}
	
	
	

	@POST
	@Path("/update")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.TEXT_PLAIN, "application/ifc" })
	public Response updateIFCtoProjectAsMultiPartFormData(@FormDataParam("ifcFile")InputStream ifcFile,@FormDataParam("jocFile") InputStream jocFile) {
		try {
			File tempIfcFile = File.createTempFile("bimserver-", ".ifc");
			tempIfcFile.deleteOnExit();

			File tempJoCFile = File.createTempFile("bimserver-", ".json");
			tempJoCFile.deleteOnExit();
			
			Files.copy(ifcFile, tempIfcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(ifcFile);
			
			
			Files.copy(jocFile, tempJoCFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(ifcFile);
			
			
			File updated_file=update_manager.updateFile(tempIfcFile.toPath(), tempJoCFile.toPath());
			ResponseBuilder response = Response.ok((Object) updated_file);  
	        response.header("Content-Disposition","attachment; filename=\"updated_model.ifc\"");  
	        return response.build();  
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}

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
			upload_manager.uploadRelease("default",tempIfcFile.toPath());
			return Response.ok("Upload thread started.").build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}
	
	

	@POST
	@Path("/upload")
	@Consumes({ MediaType.TEXT_PLAIN, "application/ifc" })
	@Produces({MediaType.TEXT_PLAIN})
	public Response uploadIFCtoProjectAsTxt(String ifc_step_content) {
		String project_id="default";
		try {
			File tempIfcFile = File.createTempFile("bimserver-", ".ifc");
			tempIfcFile.deleteOnExit();
			try {
			      FileWriter myWriter = new FileWriter(tempIfcFile);
			      myWriter.write(ifc_step_content);
			      myWriter.close();
			    } catch (IOException e) {
			      e.printStackTrace();
			    }
			upload_manager.uploadRelease("default",tempIfcFile.toPath());
			return Response.ok("Upload thread started.").build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}
	
	
	@POST
	@Path("/upload/{project_id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"application/ld+json"})
	public Response uploadIFCtoProjectAsMultiPartFormData(@FormDataParam("ifcFile") InputStream ifcFile,@PathParam("project_id") String project_id) {
		try {
			File tempIfcFile = File.createTempFile("bimserver-", ".ifc");
			tempIfcFile.deleteOnExit();

			Files.copy(ifcFile, tempIfcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(ifcFile);
			upload_manager.uploadRelease(project_id,tempIfcFile.toPath());
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}
	
	
	
	@POST
	@Path("/upload/{project_id}")
	@Consumes({ MediaType.TEXT_PLAIN, "application/ifc" })
	@Produces({"application/ld+json"})
	public Response uploadIFCtoProjectAsTxt(String ifc_step_content,@PathParam("project_id") String project_id) {
		try {
			File tempIfcFile = File.createTempFile("bimserver-", ".ifc");
			tempIfcFile.deleteOnExit();
			try {
			      FileWriter myWriter = new FileWriter(tempIfcFile);
			      myWriter.write(ifc_step_content);
			      myWriter.close();
			    } catch (IOException e) {
			      e.printStackTrace();
			    }
			upload_manager.uploadRelease(project_id,tempIfcFile.toPath());
			return Response.ok().build();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.noContent().build();
	}
	
	

	@GET
	@Path("/download")
	@Produces({ MediaType.TEXT_PLAIN, "application/ifc" })
	public Response downloadIFCFromProject() {
		String project_id="default";
		File file=upload_manager.downloadLastRelease(project_id); 
		ResponseBuilder response = Response.ok((Object) file);  
        response.header("Content-Disposition","attachment; filename=\"default_model.ifc\"");  
        return response.build();  
	}
	

	

	@GET
	@Path("/download/{project_id}")
	@Produces({ MediaType.TEXT_PLAIN, "application/ifc" })
	public Response downloadIFCFromProject(@PathParam("project_id") String project_id) {
		File file=upload_manager.downloadLastRelease(project_id); 
		ResponseBuilder response = Response.ok((Object) file);  
        response.header("Content-Disposition","attachment; filename=\""+project_id+"_model.ifc\"");  
        return response.build();  
	}
	

   
	

}
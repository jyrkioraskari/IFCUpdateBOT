package de.rwth_aachen.dc.ifc.edit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.IfcModelInterfaceException;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.utils.IfcUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParseJoC {
	final IfcModelInstance model;
	final IfcModelInterface model_interface; 

	public ParseJoC(Path ifc_file,Path joc_file) throws IOException {
		this.model = new IfcModelInstance();
		// IFC2x3
		IfcModelInterface ifcmodel2x3 = model
				.readModel(ifc_file, Paths.get("."));
		ifcmodel2x3.resetExpressIds();
		ifcmodel2x3.fixOidCounter();
		this.model_interface=ifcmodel2x3;

		String jsonString = readFile(joc_file.toAbsolutePath().toString());
		execute(ifcmodel2x3, jsonString);
		System.out.println("--------------------------------------");
		//execute(ifcmodel2x3, jsonString);
		
	}
	
	
	// Support Java 1.8: read a file 
	private String readFile(String filename) throws IOException {
	    File file = new File(filename);
	    return FileUtils.readFileToString(file);
	}

	public File saveModel()
	{
		File tmp_file;
		try {
			tmp_file = File.createTempFile("ifc", ".ttl");
			model.saveModel(this.model_interface, Paths.get(tmp_file.getAbsolutePath()));
			return tmp_file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void execute(IfcModelInterface ifcmodel2x3, String jsonString) {
		JSONObject obj = new JSONObject(jsonString);

		JSONArray arr = obj.getJSONArray("changes");
		for (int i = 0; i < arr.length(); i++) {
			String guid;;
			String property_label;;
			String value;
			String description;
			if(arr.getJSONObject(i).has("property_label"))
			{
				// API format
				guid = arr.getJSONObject(i).getJSONObject("object_guid").getString("id");
				property_label = arr.getJSONObject(i).getJSONObject("property_label").getString("value");
				value = arr.getJSONObject(i).getJSONObject("property_value").getString("value");
				description = arr.getJSONObject(i).getJSONObject("property_uri").getString("value");
			}
			else
			{
				// Example file format
				guid = arr.getJSONObject(i).getJSONObject("object").getString("id");
				property_label = arr.getJSONObject(i).getJSONObject("property").getString("label");
				value = arr.getJSONObject(i).getString("value");
				description = arr.getJSONObject(i).getJSONObject("property").getString("uri");
				
			}

			IfcProduct product = (IfcProduct) ifcmodel2x3.getByGuid(guid);
			if (product != null) {
				// System.out.println(product);
				Set<String> properties = IfcUtils.listPropertyNames(product);
				// System.out.println(properties);
				IfcValue ifcValue = IfcModelInstance.getPropertySingleValue(product, property_label);
				if (ifcValue == null)
				{
					try {
						IfcModelInstance.createProperty(product, ifcmodel2x3,property_label,description,value);
					} catch (IfcModelInterfaceException e) {
						e.printStackTrace();
					}
					; 
				}
				else {
					if (ifcValue.getClass().getName().equals("org.bimserver.models.ifc2x3tc1.impl.IfcIdentifierImpl")) {
						System.out.println("String value: " + IfcUtils.nominalValueToString(ifcValue));
						System.out.println(guid + " :" + property_label + " :" + value);
					}
					IfcModelInstance.setStringProperty(ifcmodel2x3, product, property_label, value);
				}
			} else
				System.out.println("GUID does not exist in the model. GUID: " + guid);

		}
	}

	public static void main(String[] args) throws IOException {
		
	}

}

package de.rwth_aachen.dc.ifc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.IfcModelInterfaceException;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
import org.bimserver.ifc.step.deserializer.Ifc4StepDeserializer;
import org.bimserver.ifc.step.serializer.Ifc2x3tc1StepSerializer;
import org.bimserver.ifc.step.serializer.Ifc4StepSerializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcAreaMeasure;
import org.bimserver.models.ifc2x3tc1.IfcBoolean;
import org.bimserver.models.ifc2x3tc1.IfcCountMeasure;
import org.bimserver.models.ifc2x3tc1.IfcElectricCurrentMeasure;
import org.bimserver.models.ifc2x3tc1.IfcIdentifier;
import org.bimserver.models.ifc2x3tc1.IfcInteger;
import org.bimserver.models.ifc2x3tc1.IfcLabel;
import org.bimserver.models.ifc2x3tc1.IfcLengthMeasure;
import org.bimserver.models.ifc2x3tc1.IfcLogical;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc2x3tc1.IfcPlaneAngleMeasure;
import org.bimserver.models.ifc2x3tc1.IfcPowerMeasure;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcProperty;
import org.bimserver.models.ifc2x3tc1.IfcPropertySet;
import org.bimserver.models.ifc2x3tc1.IfcPropertySetDefinition;
import org.bimserver.models.ifc2x3tc1.IfcPropertySingleValue;
import org.bimserver.models.ifc2x3tc1.IfcReal;
import org.bimserver.models.ifc2x3tc1.IfcRelDefines;
import org.bimserver.models.ifc2x3tc1.IfcRelDefinesByProperties;
import org.bimserver.models.ifc2x3tc1.IfcText;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.models.ifc2x3tc1.IfcVolumeMeasure;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.ifc2x3tc1.Tristate;
import org.bimserver.models.ifc4.Ifc4Package;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.deserializers.Deserializer;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.utils.DeserializerUtils;
import org.bimserver.utils.IfcUtils;

@SuppressWarnings("deprecation")
public class IfcModelInstance {

	public enum IfcVersion {
		IFC2x3, IFC4, UNKNOWN
	}

	private Optional<IfcVersion> ifcversion = Optional.empty();

	public IfcModelInterface readModel(Path ifcFilePath, Path ifcShcemaDirectory) {
		if (ifcFilePath.toFile().exists() && ifcFilePath.toFile().isFile()) {
			switch (getExpressSchema(ifcFilePath.toString())) {
			case IFC2x3:
				ifcversion = Optional.of(IfcModelInstance.IfcVersion.IFC2x3);
				Deserializer deserializer2x3 = new Ifc2x3tc1StepDeserializer();
				PackageMetaData pmd2x3 = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1,
						ifcShcemaDirectory);
				deserializer2x3.init(pmd2x3);
				try {
					return DeserializerUtils.readFromFile(deserializer2x3, ifcFilePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case IFC4:
				ifcversion = Optional.of(IfcModelInstance.IfcVersion.IFC4);
				Deserializer deserializer4 = new Ifc4StepDeserializer(Schema.IFC4);
				PackageMetaData pmd4 = new PackageMetaData(Ifc4Package.eINSTANCE, Schema.IFC4, ifcShcemaDirectory);
				deserializer4.init(pmd4);
				try {
					return DeserializerUtils.readFromFile(deserializer4, ifcFilePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				return null;
			}
		}
		return null;
	}

	public void saveModel(IfcModelInterface model, Path ifcFilePath) {
		switch (ifcversion.get()) {
		case IFC2x3:
			Ifc2x3tc1StepSerializer serializer1 = new Ifc2x3tc1StepSerializer(new PluginConfiguration());
			try {
				serializer1.init(model, null, false);
				serializer1.writeToFile(ifcFilePath, null);
			} catch (SerializerException e) {
				e.printStackTrace();
			}

			break;
		case IFC4:
			Ifc4StepSerializer serializer2 = new Ifc4StepSerializer(new PluginConfiguration());
			try {
				serializer2.init(model, null, false);
				serializer2.writeToFile(ifcFilePath, null);
			} catch (SerializerException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("Nothing done");
		}
	}

	public IfcVersion getExpressSchema(String ifc_file) {
		try {
			FileInputStream fstream = new FileInputStream(ifc_file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			try {
				String strLine;
				while ((strLine = br.readLine()) != null) {
					if (strLine.length() > 0) {
						if (strLine.startsWith("FILE_SCHEMA")) {
							if (strLine.indexOf("IFC2X3") != -1)
								return IfcModelInstance.IfcVersion.IFC2x3;
							if (strLine.indexOf("IFC4") != -1)
								return IfcModelInstance.IfcVersion.IFC4;
							else
								return IfcModelInstance.IfcVersion.UNKNOWN;
						}
					}
				}
			} finally {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return IfcModelInstance.IfcVersion.UNKNOWN;
	}

	public Optional<IfcVersion> getIfcversion() {
		return ifcversion;
	}

	// https://github.com/opensourceBIM/BIMserver/blob/master/PluginBase/src/org/bimserver/utils/IfcUtils.java#L369

	public static void setStringProperty(IfcModelInterface model, IfcObject ifcObject, String propertyName,
			String value) {
		for (IfcRelDefines ifcRelDefines : ifcObject.getIsDefinedBy()) {
			if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
				IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
				IfcPropertySetDefinition propertySetDefinition = ifcRelDefinesByProperties
						.getRelatingPropertyDefinition();
				if (propertySetDefinition instanceof IfcPropertySet) {
					IfcPropertySet ifcPropertySet = (IfcPropertySet) propertySetDefinition;
					for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
						if (ifcProperty instanceof IfcPropertySingleValue) {
							IfcPropertySingleValue propertyValue = (IfcPropertySingleValue) ifcProperty;
							if (ifcProperty.getName().equals(propertyName)) {
								IfcText ifcValue;
								try {
									ifcValue = model.create(IfcText.class);
									ifcValue.setWrappedValue(value);
									propertyValue.setNominalValue(ifcValue);
								} catch (IfcModelInterfaceException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}

	public static void removeProperty(IfcModelInterface model, IfcObject ifcObject, String propertyName) {
		for (IfcRelDefines ifcRelDefines : ifcObject.getIsDefinedBy()) {
			if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
				IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
				IfcPropertySetDefinition propertySetDefinition = ifcRelDefinesByProperties
						.getRelatingPropertyDefinition();
				if (propertySetDefinition instanceof IfcPropertySet) {
					IfcPropertySet ifcPropertySet = (IfcPropertySet) propertySetDefinition;
					Optional<IfcProperty> remove_this = Optional.empty();
					for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
						if (ifcProperty instanceof IfcPropertySingleValue) {
							IfcPropertySingleValue propertyValue = (IfcPropertySingleValue) ifcProperty;
							if (ifcProperty.getName().equals(propertyName)) {
								remove_this = Optional.of(ifcProperty);
							}
						}
					}
					if (remove_this.isPresent())
						ifcPropertySet.getHasProperties().remove(remove_this.get());
				}
			}
		}
	}

	public static void createProperty(IfcProduct product, IfcModelInterface model, String name, String description,
			Object value) throws IfcModelInterfaceException {
		IfcRelDefinesByProperties ifcRelDefinesByProperties = model.create(IfcRelDefinesByProperties.class);
		product.getIsDefinedBy().add(ifcRelDefinesByProperties);
		IfcPropertySet propertySet = model.create(IfcPropertySet.class);
		ifcRelDefinesByProperties.setRelatingPropertyDefinition(propertySet);
		IfcPropertySingleValue property = model.create(IfcPropertySingleValue.class);
		propertySet.getHasProperties().add(property);
		property.setName(name);
		property.setDescription(description);
		if (value instanceof Boolean) {
			IfcBoolean ifcValue = model.create(IfcBoolean.class);
			ifcValue.setWrappedValue(((Boolean) value) ? Tristate.TRUE : Tristate.FALSE);
			property.setNominalValue(ifcValue);
		} else {
			IfcText ifcValue = model.create(IfcText.class);
			ifcValue.setWrappedValue((String) value);
			property.setNominalValue(ifcValue);
		}
	}

	public static IfcProperty getPropertySingleValue(IfcObject ifcObject, String propertyName) {
		for (IfcRelDefines ifcRelDefines : ifcObject.getIsDefinedBy()) {
			if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
				IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
				IfcPropertySetDefinition propertySetDefinition = ifcRelDefinesByProperties
						.getRelatingPropertyDefinition();
				if (propertySetDefinition instanceof IfcPropertySet) {
					IfcPropertySet ifcPropertySet = (IfcPropertySet) propertySetDefinition;
					for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
						if (ifcProperty instanceof IfcPropertySingleValue) {
							IfcPropertySingleValue propertyValue = (IfcPropertySingleValue) ifcProperty;
							if (ifcProperty.getName().equals(propertyName)) {
								IfcValue nominalValue = propertyValue.getNominalValue();
								return ifcProperty;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static void copyProperty(IfcProduct product, IfcModelInterface model, IfcProperty property)
			throws IfcModelInterfaceException {
		IfcRelDefinesByProperties ifcRelDefinesByProperties = model.create(IfcRelDefinesByProperties.class);
		product.getIsDefinedBy().add(ifcRelDefinesByProperties);
		IfcPropertySet propertySet = model.create(IfcPropertySet.class);
		ifcRelDefinesByProperties.setRelatingPropertyDefinition(propertySet);
		propertySet.getHasProperties().add(property);
	}

	public static void copyToPropertySet(IfcObject ifcObject, String propertySetName, IfcProperty property) {
		for (IfcRelDefines ifcRelDefines : ifcObject.getIsDefinedBy()) {
			if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
				IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
				IfcPropertySetDefinition propertySetDefinition = ifcRelDefinesByProperties
						.getRelatingPropertyDefinition();
				if (propertySetDefinition instanceof IfcPropertySet) {
					if (propertySetDefinition.getName().equals(propertySetName)) {
						IfcPropertySet ifcPropertySet = (IfcPropertySet) propertySetDefinition;
						ifcPropertySet.getHasProperties().add(property);
					}
				}
			}
		}
	}

	public static void createToPropertySet(IfcModelInterface model, IfcObject ifcObject, String propertySetName,
			String name, String description, String value) throws IfcModelInterfaceException {
		for (IfcRelDefines ifcRelDefines : ifcObject.getIsDefinedBy()) {
			if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
				IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
				IfcPropertySetDefinition propertySetDefinition = ifcRelDefinesByProperties
						.getRelatingPropertyDefinition();
				if (propertySetDefinition instanceof IfcPropertySet) {
					if (propertySetDefinition.getName().equals(propertySetName)) {
						System.out.println("Found property set");
						IfcPropertySet ifcPropertySet = (IfcPropertySet) propertySetDefinition;
						IfcPropertySingleValue property = model.create(IfcPropertySingleValue.class);
						System.out.println("property: "+property);
						property.setName(name);
						property.setDescription(description);
						IfcText ifcValue = model.create(IfcText.class);
						ifcValue.setWrappedValue((String) value);
						property.setNominalValue(ifcValue);
						ifcPropertySet.getHasProperties().add(property);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		IfcModelInstance model = new IfcModelInstance();
		// IFC2x3
		IfcModelInterface ifcmodel2x3 = model.readModel(Paths.get("c:\\ifc\\20180731Dubal Herrera limpio.ifc"),
				Paths.get("."));
		System.out.println(ifcmodel2x3.size());

		List<IfcWindow> windows = ifcmodel2x3.getAll(IfcWindow.class);
		System.out.println("windows: " + windows.size());
		IfcProperty ttproperty = null;
		for (IfcWindow w : windows) {
			System.out.println(w.getGlobalId() + ":");
			Set<String> properties = IfcUtils.listPropertyNames(w);
			System.out.println(properties);
			if (IfcUtils.hasProperty(w, "ThermalTransmittance")) {

				ttproperty = IfcModelInstance.getPropertySingleValue(w, "ThermalTransmittance");
				// System.out.println("tt: " + IfcUtils.getStringProperty(w,
				// "ThermalTransmittance"));

				// "traga luz 800x400 mm"
				// "1.80x1.50 m"
				// IfcModelInstance.setStringProperty(ifcmodel2x3, w,"Reference","1.80x1.50 m");
				// w:1.9
				// h:1.63

				// System.out.println("w:" + w.getOverallWidthAsString());
				// System.out.println("h:" + w.getOverallHeightAsString());

				// w.setOverallWidthAsString("1.9");
				// w.setOverallHeightAsString("1.63");
			}
		}

		for (IfcWindow w : windows) {
			System.out.println(w.getGlobalId() + ":");
			Set<String> properties = IfcUtils.listPropertyNames(w);
			System.out.println(properties);
			if (!IfcUtils.hasProperty(w, "ThermalTransmittance")) {
				IfcModelInstance.copyToPropertySet(w, "Pset_WindowCommon", ttproperty);
				System.out.println("has tt: " + IfcUtils.hasProperty(w, "ThermalTransmittance"));
			}

			try {
				IfcModelInstance.createToPropertySet(ifcmodel2x3, w, "Pset_WindowCommon", "MyProperty",
						"Description", "Own value");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		model.saveModel(ifcmodel2x3, Paths.get("c:\\temp\\output5	.ifc"));

		/*
		 * IfcProduct product =
		 * (IfcProduct)ifcmodel2x3.getByGuid("1hOSvn6df7F8_7GcBWlR72");
		 * System.out.println(product); Set<String>
		 * properties=IfcUtils.listPropertyNames(product);
		 * System.out.println(properties);
		 * 
		 * System.out.println(IfcUtils.hasProperty(product, "OmniClass Title"));
		 * System.out.println(IfcUtils.getStringProperty(product, "OmniClass Title"));
		 * 
		 * IfcModelInstance.setStringProperty(ifcmodel2x3, product,
		 * "OmniClass Title","123");
		 * System.out.println(IfcUtils.getStringProperty(product, "OmniClass Title"));
		 * 
		 * try { IfcModelInstance.createProperty(product, ifcmodel2x3,
		 * "My own property","Description", "Own value"); } catch
		 * (IfcModelInterfaceException e) { e.printStackTrace(); }
		 * System.out.println(IfcUtils.getStringProperty(product, "My own property"));
		 * 
		 * Set<String> properties2=IfcUtils.listPropertyNames(product);
		 * System.out.println(properties2);
		 * 
		 * IfcModelInstance.removeProperty(ifcmodel2x3, product, "My own property");
		 * 
		 * Set<String> properties3=IfcUtils.listPropertyNames(product);
		 * System.out.println(properties3);
		 * 
		 */
		// IFC4
		// IfcModelInterface ifcmodel4
		// =model.readModel(Paths.get("c:\\ifc\\20160125Autodesk_Hospital_Parking
		// Garage_2015 - IFC4.ifc"), Paths.get("."));
		// System.out.println(ifcmodel4.size());

	}
}

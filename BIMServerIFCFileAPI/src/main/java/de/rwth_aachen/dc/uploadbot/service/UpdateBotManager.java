package de.rwth_aachen.dc.uploadbot.service;

import java.io.File;
import java.io.IOException;

import de.rwth_aachen.dc.ifc.edit.ParseJoC;

public class UpdateBotManager {

	public File updateFile(java.nio.file.Path ifc_file,java.nio.file.Path joc_file) {
		
		try {
			ParseJoC p=new ParseJoC( ifc_file, joc_file);
			File f=p.saveModel();
			if(f!=null)
				return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

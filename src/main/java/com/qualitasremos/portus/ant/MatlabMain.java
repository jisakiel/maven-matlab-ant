package com.qualitasremos.portus.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;

/**
 * Stub class for representing the <main> children of <mcc>
 * @author ezequiel
 *
 */
public class MatlabMain {

	private File file;

	public File getFile() {
		if (this.file == null)
			throw new BuildException("file parameter must be defined in the <main> subelement");
		
		return file;
	}
	

	/**
	 * Required parameter file with the .m containing the main function
	 * 
	 * @param file the file path, relative to the ant folder
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
}
 
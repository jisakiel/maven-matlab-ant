/*
   Copyright 2012 Qualitas Remos

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
 

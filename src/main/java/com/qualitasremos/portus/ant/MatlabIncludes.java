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

import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;

/**
 * Stub class for the <includes> subelement of the <mcc>. 
 * 
 * It admits both Fileset and Dirsets. In case dirsets are passed, the folders will be expanded according to the patterns 
 * (by default, equivalent to "find -type d") and their names will be sent to the compiler, without searching for files.
 * 
 * @author ezequiel
 *
 */
public class MatlabIncludes {
	//there could conceivably be more than one <fileset> element
	private List<FileSet> filesets = new Vector<FileSet>();
	private List<DirSet> dirsets = new Vector<DirSet>();
	
	public List<FileSet> getFilesets() {
		return filesets;
	}

	public List<DirSet> getDirsets() {
		return dirsets;
	}

	public void addConfiguredDirSet(DirSet dirs) {
		dirsets.add(dirs);
	}

	public void addConfiguredFileSet(FileSet files) {
		filesets.add(files);
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean haveDirs = false;
		if (dirsets != null && !dirsets.isEmpty()) {
			haveDirs = true;
			sb.append("dirsets "+dirsets);
		}
		if (filesets != null && !filesets.isEmpty()) {
			if (haveDirs)
				sb.append(", ");
			sb.append("filesets "+filesets);
		}
			
		return sb.toString();
	}
}

package com.qualitasremos.portus.ant;

import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;

public class MatlabIncludes {
	//there could conceivably be more than one <fileset> element
	private List<FileSet> filesets = new Vector<FileSet>();
	public List<FileSet> getFilesets() {
		return filesets;
	}

	public List<DirSet> getDirsets() {
		return dirsets;
	}

	private List<DirSet> dirsets = new Vector<DirSet>();

	public MatlabIncludes() {}

	public void addConfiguredDirSet(DirSet dirs) {
		dirsets.add(dirs);
	}

	public void addConfiguredFileSet(FileSet files) {
		filesets.add(files);
	}


}

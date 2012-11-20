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
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Local;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.LogLevel;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;

public class MccTask extends Task {

	private static final String MCC_PROPNAME = "mcc";
	private static final String DEFAULTMCCARGS_PROPNAME = "mcc.defaultargs";
	private static final String[] TRASH_FILENAMES = new String[] {"readme.txt", "mccExcludedFiles.log"}; //TODO refactor to property
	private static final String UPTODATE_PROP = "isUpToDate-matlab-files";

	private String objName = null;
	private File mainFile = null;
	private List<File> includes = new LinkedList<File> ();
	private File targetDir = null;
	private MatlabIncludes includesParam;

	public void setName(String objName) {
		this.objName = objName;
	}

	public void addConfiguredMain(MatlabMain mainFile) {
		if (this.mainFile != null)
			throw new BuildException("Only one main element is accepted");
		this.mainFile = mainFile.getFile();
		log("Received file: "+mainFile.getFile());
	}

	public void setTargetdir(File targetDir) {
		this.targetDir = targetDir;
	}
	
	/**
	 * Convert passed DirSet and FileSet to a regular list of Files
	 * 
	 * @param includesObj our configured subelement
	 */
	public void addConfiguredIncludes(MatlabIncludes includesObj) {
		//store them as well for the up2date checker
		this.includesParam = includesObj;

		for (DirSet d : includesObj.getDirsets()) {
			DirectoryScanner ds = d.getDirectoryScanner(getProject());
			String[] includedDirs = ds.getIncludedDirectories(); //get directories but don't expand to files
			for (String dirname : includedDirs) {
				log("Listed include dir... "+d.getDir(getProject())+"/"+dirname,LogLevel.DEBUG.getLevel());
				File dir = new File(d.getDir(getProject()),dirname);
				assert(dir.isDirectory());
				includes.add(dir);
			}
		}

		for (FileSet f : includesObj.getFilesets()) {
			DirectoryScanner ds = f.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles(); //expand to files using patterns and so on
			for (String filename : includedFiles) {
				log("Listed include file... "+f.getDir(getProject())+"/"+filename,LogLevel.DEBUG.getLevel());
				File file = new File(f.getDir(getProject()),filename);
				assert(file.isFile());
				includes.add(file);
			}
		}
	}

	public void execute() throws BuildException {

		//bash file is only created in case the compilation succeded
		File targetFile = new File(targetDir, "run_"+objName+".sh");
		if (targetFile.exists()) {
			//first, check if already up-to-date
			UpToDate updateChecker = (UpToDate) getProject().createTask("uptodate");
			//traverse dirs and add them to fileset
			List<DirSet> dirSets = includesParam.getDirsets();
			if (dirSets != null) {
				log("Checking upToDate for dependencies (dirsets)",LogLevel.DEBUG.getLevel());
				for (DirSet dirSet : dirSets) {
					DirectoryScanner ds = dirSet.getDirectoryScanner(getProject());
					FileSet fs = new FileSet();
					fs.setDir(dirSet.getDir(getProject()));
					FileSelector[] fss = dirSet.getSelectors(getProject());
					for (FileSelector sel : fss) {
						fs.add(sel);
					}		
					updateChecker.addSrcfiles(fs);
				}
			}

			List<FileSet> fileSets = includesParam.getFilesets();
			if (fileSets != null) {
				log("Checking upToDate for dependencies (filesets)",LogLevel.DEBUG.getLevel());
				for (FileSet fs : fileSets)
					updateChecker.addSrcfiles(fs);
			}
			
			log("Checking upToDate for main file...",LogLevel.DEBUG.getLevel());
			FileSet fs = new FileSet();
			fs.setDir(getProject().getBaseDir());
			fs.setFile(mainFile);
			updateChecker.addSrcfiles(fs);

			updateChecker.setTargetFile(targetFile);

			updateChecker.setProperty(getUpToDateProp());
			updateChecker.perform();

			if (!Boolean.valueOf(getProject().getProperty(getUpToDateProp()))) {
				log("Source files or includes have been updated for "+objName+", recompiling...");
			} else {
				log("Neither source files nor includes have been updated for "+objName+" since last compile, skipping...");
				return;
			}
			
		}

		log("mcc creating object "+objName+" with main "+mainFile+" and includes "+includesParam);

		//create target dir using subtask
		Mkdir mkdir = (Mkdir) getProject().createTask("mkdir");
		mkdir.setDir(targetDir);
		mkdir.perform();
		//		if (!targetDir.exists() || !targetDir.isDirectory()) {
		//			log("Creating target directory "+targetDir.getPath());
		//			if (!targetDir.mkdirs())
		//				throw new BuildException("Cannot create target directory "+targetDir);
		//		}

		//now invocate compile using ExecTask (proper buildexception, etc). 

		//mcc -o project-name -W main:project-name -T link:exe -d targetdir -N (remove default dirs) -w warns... -R runtime-options,.. -v verbose -a folders-or-files-to-include
		ExecTask compile = (ExecTask) getProject().createTask("exec");
		Commandline cmd = new Commandline();
		Project p = getProject();
		cmd.setExecutable(p.getProperty(MCC_PROPNAME)); //FIXME deprecated
		String defaultArgs = p.getProperty(DEFAULTMCCARGS_PROPNAME);
		if (defaultArgs != null)
			cmd.addArguments(defaultArgs.split(" "));

		cmd.addArguments(new String[] {"-o",objName,"-W","main:"+objName,"-T","link:exe","-d",targetDir.getPath(),mainFile.getPath()});
		for (File f : this.includes) {
			cmd.addArguments(new String[] {"-a",f.getPath()});
		}
		compile.setCommand(cmd);
		log("Running cmd: "+cmd,LogLevel.VERBOSE.getLevel());
		compile.perform();

		//finally, delete readme.txt and excludedfiles (just rubbish)
		for (String filename : TRASH_FILENAMES) {
			log("Deleting espurious file "+filename, LogLevel.VERBOSE.getLevel());
			File f = new File(targetDir,filename);
			if (f.exists()) {
				f.delete();
			} else {
				log("File "+filename+" does not exist!",LogLevel.WARN.getLevel());
			}
		}
	}

	/**
	 * Setup a property name that is unique for the scope.
	 * 
	 * Properties are inmutable so must be unique inside a scope and cannot be overwritten (besides, that is not thread-safe)
	 * @return
	 */
	private String getUpToDateProp() {
		String propName = new StringBuilder(UPTODATE_PROP).append("-").append(objName).toString();
		PropertyHelper ph = PropertyHelper.getPropertyHelper(getProject());
		if (ph.containsProperties(propName))
			throw new BuildException("Executable names must be unique inside an ant context and "+objName+" has already been used");
		else
			return propName;
	}
}

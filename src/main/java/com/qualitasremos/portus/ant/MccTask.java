package com.qualitasremos.portus.ant;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.LogLevel;

public class MccTask extends Task {

	private static final String MCC_PROPNAME = "mcc";
	private static final String DEFAULTMCCARGS_PROPNAME = "mcc.defaultargs";
	private static final String[] TRASH_FILENAMES = new String[] {"readme.txt", "mccExcludedFiles.log"};
	
	private String objName;
	private File mainFile = null;
	private List<File> includes = new LinkedList<File> ();
	private File targetDir;

	public void setName(String objName) {
		this.objName = objName;
	}

	public void addConfiguredMain(MatlabMain mainFile) {
		this.mainFile = mainFile.getFile();
		log("Received file: "+mainFile.getFile());
	}

	public void setTargetdir(File targetDir) {
		this.targetDir = targetDir;
	}

	/**
	 * Convert passed DirSet and FileSet to a regular list of Files
	 * @param includesObj our configured subelement
	 */
	public void addConfiguredIncludes(MatlabIncludes includesObj) {

		for (DirSet d : includesObj.getDirsets()) {
			DirectoryScanner ds = d.getDirectoryScanner(getProject());
			String[] includedDirs = ds.getIncludedDirectories(); //get directories but don't expand to files
			for (String dirname : includedDirs) {
				log("Listed include dir... "+dirname,LogLevel.DEBUG.getLevel());
				File dir = new File(d.getDir(getProject()),dirname);
				assert(dir.isDirectory());
				includes.add(dir);
			}
		}

		for (FileSet f : includesObj.getFilesets()) {
			DirectoryScanner ds = f.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles(); //expand to files using patterns and so on
			for (String filename : includedFiles) {
				log("Listed include file... "+filename,LogLevel.DEBUG.getLevel());
				File file = new File(f.getDir(getProject()),filename);
				assert(file.isFile());
				includes.add(file);
			}
		}
	}

	public void execute() throws BuildException {
		log("mcc creating object "+objName+" with main "+mainFile+" and includes "+includes);

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
		log("Running cmd: "+cmd,LogLevel.DEBUG.getLevel());
		compile.perform();

		//finally, delete readme.txt and excludedfiles (just rubbish)
		for (String filename : TRASH_FILENAMES) {
			File f = new File(targetDir,filename);
			if (f.exists())
				f.delete();
		}
	}
}

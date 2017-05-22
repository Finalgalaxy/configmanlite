package com.finalgalaxy.configmanlite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ConfigManager{
	//private static final Logger LOGGER = Logger.getLogger( ConfigManager.class.getName() );
	
	private HashMap<String,ConfigFile> cfg_list;
	private String global_path;
	
	public ConfigManager(){
		cfg_list=new HashMap<String,ConfigFile>();
	}
	
	public boolean setGlobalPath(String path) throws SecurityException{
		File dir = new File(path);
		boolean dir_exists=true;
		if(!dir.isDirectory()) if(!dir.mkdir()) dir_exists=false;
		if(dir_exists) this.global_path=path;
		return dir_exists;
	}
	
	public ConfigFile open(String path, boolean use_global_path){
		return this.open(path,use_global_path,ConfigFile.Versioning.ENABLED);
	}
	
	public ConfigFile open(String path, boolean use_global_path, ConfigFile.Versioning versioning){
		if(use_global_path) path=this.global_path+path;
		if(!cfg_list.containsKey(path))	this.cfg_list.put(path,new ConfigFile(path,versioning));
		return this.cfg_list.get(path);
	}
	
	public ConfigFile close(String path, boolean use_global_path) throws IOException{
		if(use_global_path) path=this.global_path+path;
		if(this.cfg_list.containsKey(path)){
			this.cfg_list.get(path).release();
			return this.cfg_list.remove(path);
		}else{
			return null;
		}
	}
}

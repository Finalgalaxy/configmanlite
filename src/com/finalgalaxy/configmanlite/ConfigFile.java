package com.finalgalaxy.configmanlite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConfigFile{
	// Versioning enum; if versioning_mode is ENABLED, "ConfigFile_version" will be used as versioning field.
	public enum Versioning{
		ENABLED, ENABLED_STRICT, DISABLED
	}
	
	/*
	 * Properties object we use in order to access file data. Class ConfigFile actually wraps this object.
	 * In case of new needs, it can be replaced with another type of object without change methods behavior.
	 */
	private ConfigSortedProperties file_prop;	
	private File file_asfile;	// Cached file in case getFile() is invoked.
	private String path;	// The path of the physical file.
	private FileInputStream input;
	private FileOutputStream output;
	private HashMap<String,String> default_values;	// Map where default values are stored; also it stores correct keys for new config versions.
	private Versioning versioning_mode;
	
	
	
	public ConfigFile(String path, ConfigFile.Versioning versioning_mode){
		this.file_prop = new ConfigSortedProperties();
		this.path=path;
		this.versioning_mode=versioning_mode;
	}
	
	public ConfigFile(String path, ConfigFile.Versioning versioning_mode, HashMap<String,String> default_values){
		this(path,versioning_mode);
		this.default_values=default_values;
	}
	
	
	
	public void setDefaultValues(HashMap<String,String> default_values){
		this.setDefaultValues(default_values,-1);
	}
	public void setDefaultValues(HashMap<String,String> default_values, int actual_version){
		default_values.put("ConfigFile_version",String.valueOf(actual_version));
		this.default_values=default_values;
	}
	
	public void resetParametersToDefault(){
		if(!this.file_prop.isEmpty()) this.resetParameters();
		for(HashMap.Entry<String,String> entry : this.default_values.entrySet()){
			this.setParameter(entry.getKey(),entry.getValue());
		}
	}
	
	public boolean isVersionEqual(int actual_version) throws FileNotFoundException, IOException{
		this.loadParameters();
		return (actual_version==this.getIntParameter("ConfigFile_version"));
	}
	
	public void validateVersion(int actual_version) throws FileNotFoundException, IOException{
		if(!this.getFile().exists()){	// If file doesn't exists
			System.out.println("General config file created (it was not existing).");
			this.resetParametersToDefault();	// Reset properties values to default
			this.saveParameters();	// Save parameters, so generate a new file
			this.release();	// Release output stream
		}else{	// If file instead exists
			System.out.println("Config file at path "+path+" was already present into system.");
			this.loadParameters();
			if((this.versioning_mode == ConfigFile.Versioning.ENABLED_STRICT) || !this.isVersionEqual(actual_version)){
				Set<Object> keys_not_evaluated = new HashSet<Object>();
				keys_not_evaluated.addAll(this.default_values.keySet());
				HashMap<Object,Object> map_merged_properties = new HashMap<Object,Object>();
				for(HashMap.Entry<Object,Object> entry : this.file_prop.entrySet()){	// Scan ConfigFile params
					if(this.default_values.containsKey(entry.getKey())){	// If key is allowed
						if(this.validateField(entry)){	// Check if associated value is valid
							map_merged_properties.putIfAbsent(entry.getKey(),entry.getValue());	// Add file value to map
						}else{	// If value is not valid, use default one
							map_merged_properties.putIfAbsent(entry.getKey(),this.default_values.get(entry.getKey()));
						}
						keys_not_evaluated.remove(entry.getKey());
					}
				}	// At this point, new map is built (or has no values)
				this.release();	// Close input stream and reset properties
				if(!map_merged_properties.isEmpty()){
					for(HashMap.Entry<Object,Object> entry : map_merged_properties.entrySet()){	// Scan merged map
						this.setParameter(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));	// Set map entries to properties
					}
					for(Object key : keys_not_evaluated){
						this.setParameter(String.valueOf(key),this.default_values.get(key));
					}
					this.saveParameters();	// Commit changes to file
					this.release(); // Close output stream and reset properties file
				}
				this.loadParameters();	// Reload file in open mode
			}
		}
	}
	
	public boolean validateField(HashMap.Entry<Object,Object> field){
		return true;
	}
	
	// Methods as wrappers to access properties and file for parameters management and I/O operations.
	public File getFile(){
		if(this.file_asfile==null) this.file_asfile=new File(path);
		return this.file_asfile;
	}
	public FileInputStream getInputStream() throws FileNotFoundException{
		if(this.input==null)	this.input = new FileInputStream(this.path);
		return this.input;
	}
	public FileOutputStream getOutputStream() throws FileNotFoundException{
		if(this.output==null)	this.output = new FileOutputStream(this.path);
		return this.output;
	}
	public void loadParameters() throws FileNotFoundException, IOException{
		if(this.file_prop.isEmpty())	this.file_prop.load(this.getInputStream());
	}
	public void release() throws IOException{
		if(this.input!=null){this.input.close();this.input=null;}
		if(this.output!=null){this.output.close();this.output=null;}
		this.file_prop.clear();
	}
	public boolean hasNoParameters(){
		return this.file_prop.isEmpty();
	}
	public void resetParameters(){
		this.file_prop.clear();
	}
	public void setParameter(String key, Object value){
		String parsed_value;
		if(value instanceof Integer)	parsed_value=String.valueOf((int)(value));
		else if(value instanceof Boolean)	parsed_value=String.valueOf(((Boolean)(value)?1:0));
		else if(value instanceof String)	parsed_value=(String)value;
		else	parsed_value=String.valueOf(value);
		this.file_prop.setProperty(key, parsed_value);
	}
	public void setParameters(HashMap<String,Object> map_new_values){
		for(HashMap.Entry<String,Object> entry : map_new_values.entrySet()){
			this.setParameter(entry.getKey(),entry.getValue());
		}
	}
	public void setVersion(int actual_version){
		this.setParameter("ConfigFile_version",actual_version);
	}
	public String getParameter(String key){
		return this.file_prop.getProperty(key);
	}
	public int getIntParameter(String key){
		String param=this.getParameter(key);
		return param!=null ? Integer.parseInt(param) : -1;
	}
	public boolean getBooleanParameter(String key){
		String param=this.getParameter(key);
		return param!=null ? (param.charAt(0)=='1'?true:false): null;
	}
	public void saveParameters() throws IOException{
		this.file_prop.store(this.getOutputStream(), null);
	}
}

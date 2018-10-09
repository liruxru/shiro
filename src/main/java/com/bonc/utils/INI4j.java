package com.bonc.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
/**
 * 有序读取 ini配置文件
 *
 */
public class INI4j {
    
    /**
     * 用linked hash map 来保持有序的读取
     * 
     */
    final LinkedHashMap<String,LinkedHashMap<String, String>>  coreMap = new LinkedHashMap<String, LinkedHashMap<String,String>>();
    /**
     * 当前Section的引用
     */
    String currentSection = null;
     
	/**
	 * 读取
	 * @param file 文件
	 * @throws FileNotFoundException 
	 */
	public INI4j(File file) throws FileNotFoundException {
		this.init(new BufferedReader(new FileReader(file)));
	}
	/***
	 * 重载读取(方法重载)
	 * @param path 给文件路径
	 * @throws FileNotFoundException 
	 */
	public INI4j(String path) throws FileNotFoundException {
	    this.init(new BufferedReader(new FileReader(path)));
	}
	/***
	 * 重载读取(方法重载)
	 * @param source ClassPathResource 文件，如果文件在resource 里，那么直接 new ClassPathResource("file name");
	 * @throws IOException 
	 */
	public INI4j(ClassPathResource source) throws IOException {
		this(source.getFile());
	}

    /**
     * 初始化方法，读取配置文件的入口
     * @param bufferedReader
     */
    void init(BufferedReader bufferedReader){
    	try {
    		read(bufferedReader);
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw new RuntimeException("IO Exception:" + e);
    	}
    }
    /**
     * 读取文件
     * @param reader
     * @throws IOException
     */
    void read(BufferedReader reader) throws IOException {
        String line = null;
        while((line=reader.readLine())!=null) {
//            解析读取的文件
            parseLine(line);
        }
    }
     
    /**
     *  解析读取的文件，解析封装到coreMap
     * @param line
     */
    void parseLine(String line) {
        line = line.trim();
        // #开头的行是注释  读取到#开头的行，直接返回，不需要解析
        if(line.matches("^\\#.*$")) {
            return;
        }else if (line.matches("^\\[\\S+\\]$")) {
            // 这个匹配的是配置文件中[]的行,  [main][user] section(部分,部门,片段)
            String section = line.replaceFirst("^\\[(\\S+)\\]$","$1");
            addSection(section);
        }else if (line.matches("^\\S+=.*$")) {
            // key ,value   添加key  value
            int i = line.indexOf("=");
            String key = line.substring(0, i).trim();
            String value =line.substring(i + 1).trim();
            addKeyValue(currentSection,key,value);
        }
    }
 
 
    /**
     * 增加新的Key和Value
     * @param currentSection
     * @param key
     * @param value
     */
    void addKeyValue(String currentSection,String key, String value) {
        if(!coreMap.containsKey(currentSection)) {
            return;
        }
        //具体结构是key 为section  他的值是个map  map里包含了 配置文件中=左右的key value
        Map<String, String> childMap = coreMap.get(currentSection);
        childMap.put(key, value);
    }
 
 
    /**
     * 增加Section
     * @param section
     */
    void addSection(String section) {
        if (!coreMap.containsKey(section)) {
            currentSection = section;
            LinkedHashMap<String,String> childMap = new LinkedHashMap<String,String>();
            coreMap.put(section, childMap);
        }
    }
     
    /**
     * 获取配置文件指定Section和指定子键的值
     * @param section
     * @param key
     * @return
     */
    public String get(String section,String key){
        if(coreMap.containsKey(section)) {
            return  get(section).containsKey(key) ?  get(section).get(key): null;
        }
        return null;
    }
     
     
     
    /**
     * 获取配置文件指定Section的子键和值
     * @param section
     * @return
     */
    public Map<String, String> get(String section){
        return  coreMap.containsKey(section) ? coreMap.get(section) : null;
    }
     
    /**
     * 获取这个配置文件全部的节点和值
     * @return
     */
    public LinkedHashMap<String, LinkedHashMap<String, String>> get(){
        return coreMap;
    }
     
}


package com.finalgalaxy.configmanlite;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

public class ConfigSortedProperties extends Properties{
	private static final long serialVersionUID=1L;
	
  @Override
  public synchronized Enumeration<Object> keys() {
      return Collections.enumeration(new TreeSet<Object>(super.keySet()));
  }
}
package com.quancheng.saluki.zuul.groovy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters only .groovy files
 */
public class GroovyFileFilter implements FilenameFilter {
  public boolean accept(File dir, String name) {
    return name.endsWith(".groovy");
  }

}

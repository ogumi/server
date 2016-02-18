
package com.naymspace.ogumi.model.server;

import java.net.URL;
import java.net.URLClassLoader;

public class ModelClassLoader extends URLClassLoader{

    public ModelClassLoader(URL[] urls, ClassLoader cl) {
        super(urls, cl);
    }
    
    @Override
    public void addURL(URL url){
        super.addURL(url);
    }
    
}

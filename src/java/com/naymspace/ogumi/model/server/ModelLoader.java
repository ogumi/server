package com.naymspace.ogumi.model.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModelLoader {
    
    public static final ModelClassLoader classLoader = new ModelClassLoader(new URL[]{}, ModelClassLoader.class.getClassLoader());

    public static String loadModel(File jar) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        classLoader.addURL(jar.toURI().toURL());
        List<String> classList = ModelLoader.getClassNamesInPackage(jar);
        String clz = null;
        for (String claz : classList) {
            Class cls = classLoader.loadClass(claz);
            if(Class.forName("com.naymspace.ogumi.model.AbstractModel").isAssignableFrom(cls)){
                if(!claz.equals("com.naymspace.ogumi.model.example.OgumiExampleModel") && !claz.equals("com.naymspace.ogumi.model.AbstractModel")){
                    clz = claz;
                }
            }
        }
        return clz;
    }

    protected static List<String> getClassNamesInPackage(File file) throws IOException {
        ArrayList classes = new ArrayList();
        JarInputStream jarFile = new JarInputStream(new FileInputStream(file));
        JarEntry jarEntry;
        while (true) {
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if ((jarEntry.getName().endsWith(".class"))) {
                classes.add(jarEntry.getName().replaceAll("/", "\\.").replace(".class", ""));
            }
        }
        return classes;
    }

}

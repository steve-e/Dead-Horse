package com.example.steve.deadhorse;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: xmlFilename, outputClassName");
            System.exit(-1);
        }
        String className = args[1];
        XmlGG xmlGG = new XmlGG(className);
        String src = xmlGG.generateSrc(fileContents(args[0]));
        File javaFile = new File(String.format("%s.java", className));
        FileUtils.writeStringToFile(javaFile, src);
        System.out.println(String.format("Wrote file %s", javaFile.getAbsolutePath()));
    }

    private static String fileContents(String arg) throws IOException {
        return FileUtils.readFileToString(new File(arg));
    }
}

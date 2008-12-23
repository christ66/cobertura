/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 James Seigel
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2008 John Lewis
 * 
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

// Creates structre:
//  src0/
//    com/
//      example/
//        Sample1.java (contains a @Deprecated annotation to make sure the complexity works with annotations)
//        Sample2.java
//  src1/
//    com/
//      example/
//        Sample3.java
//        Sample4.java
//  src2/
//    com/
//      example/
//        Sample5.java
//        Sample6.java
//  src3/
//    com/
//      example/
//        Sample7.java
//        Sample8.java
public class FileFixture {
    public static final String[] SOURCE_DIRECTORY_IDENTIFIER = { "src0", "src1", "src2", "src3" };
    public HashMap sourceDirectories;

    public void setUp() throws IOException {
        File tempFile = File.createTempFile("temp", ".tmp");
        tempFile.deleteOnExit();
        sourceDirectories = new HashMap();

        for( int i=0; i<SOURCE_DIRECTORY_IDENTIFIER.length; i++) {
        	File directory = new File(tempFile.getParent(), SOURCE_DIRECTORY_IDENTIFIER[i]);
        	directory.mkdir();
        	filesUnderSourceDir(directory, 1+i*2);
        	sourceDirectories.put(SOURCE_DIRECTORY_IDENTIFIER[i], directory);
        }
    }

    private void filesUnderSourceDir(File srcDirectory, int number) throws IOException, FileNotFoundException {
        File sub = new File(srcDirectory, "com");
        sub.mkdir();
        File temp = new File(sub, "example");
        temp.mkdir();
        File sample1 = new File(temp, "Sample" + number + ".java");
        sample1.createNewFile();
        FileWriter writer1 = new FileWriter(sample1);
        writer1.write( "package com.example;\n");
        writer1.write( "public class Sample" + number + " {\n");
        /*
         * Add an annotation to make sure the complexity works with them
         */
        if (number == 1) {
        	writer1.write( "@Deprecated\n");
        }
        writer1.write( "    public void someMethod(int v) {\n");
        writer1.write( "        if(v<0) System.out.println();\n");
        writer1.write( "        else System.out.println('x');\n");
        writer1.write( "    }\n");
        writer1.write( "}\n");
        writer1.close();
        
        File sample2 = new File(temp, "Sample" + (number + 1) + ".java");
        sample2.createNewFile();
        FileWriter writer2 = new FileWriter(sample2);
        writer2.write( "package com.example;\n");
        writer2.write( "class Sample" + (number + 1) + " {\n");
        writer2.write( "    private String otherMethod() {\n");
        writer2.write( "        return \"OtherValue\";\n");
        writer2.write( "    }\n");
        writer2.write( "}\n");
        writer2.close();
    }

    protected void deleteTree(File fileRoot) {
        if (fileRoot.isFile()) {
            fileRoot.delete();
            return;
        }

        File[] files = fileRoot.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteTree(files[i]);
        }
        fileRoot.delete();
    }

    public void tearDown() {
        for( int i=0; i<SOURCE_DIRECTORY_IDENTIFIER.length; i++) {
        	deleteTree(sourceDirectory(SOURCE_DIRECTORY_IDENTIFIER[i]));
        }
    }

    public File sourceDirectory(String directoryIdentifier) {
        return (File) sourceDirectories.get(directoryIdentifier);
    }
}

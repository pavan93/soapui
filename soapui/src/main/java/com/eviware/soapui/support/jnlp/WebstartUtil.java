/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support.jnlp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class WebstartUtil {

    static String createWebStartDirectory(String name, String jarUrl) throws IOException {

        String deploymentUserTmp = System.getProperty("deployment.user.tmp");
        JarFile jar = getJar(jarUrl);
        String dir = createDirectory(deploymentUserTmp, name);
        extract(jar, dir);
        return dir;
    }

    private static void extract(JarFile jar, String dir) throws IOException {
        makeDirectories(jar, dir);
        extractFiles(jar, dir);
    }

    @SuppressWarnings("unchecked")
    private static void extractFiles(JarFile jar, String eviwareDir) throws IOException {
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry file = (JarEntry) entries.nextElement();
            File f = new File(eviwareDir + File.separator + file.getName());

            if (file.isDirectory()) { // if its a directory, skip it
                continue;
            }
            // System.out.println(f);
            InputStream is = jar.getInputStream(file);
            FileOutputStream fos = new FileOutputStream(f);

            while (is.available() > 0) {
                fos.write(is.read());
            }

            fos.close();
            is.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static void makeDirectories(JarFile jar, String eviwareDir) {
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry file = (JarEntry) entries.nextElement();
            File f = new File(eviwareDir + File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                // System.out.println(f);
            }
        }
    }

    private static JarFile getJar(String jarUrl) throws IOException {
        // String reportsJarUrl = System.getProperty("reports.jar.url");
        URL url = new URL("jar:" + jarUrl + "!/");
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        JarFile jar = jarConnection.getJarFile();
        return jar;
    }

    private static String createDirectory(String deploymentUserTmp, String folderName) {
        File folder = new File(deploymentUserTmp + File.separator + folderName);
        folder.mkdir();
        // System.out.println(folder.getAbsolutePath());
        return folder.getAbsolutePath();
    }

    static boolean isWebStart() {
        String webstart = System.getProperty("com.eviware.soapui.webstart", "false");
        return "true".equalsIgnoreCase(webstart);

    }

}

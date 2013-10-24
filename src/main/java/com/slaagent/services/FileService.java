package com.slaagent.services;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileService {

    private static PropertiesService properties = PropertiesService.getInstance();
    
    public FileService() {
    }

    public void createFile(String name) {

        try {

            File file = new File(PropertiesService.getInstance().getProperties().getProperty("logs.files.path") + name);
            if (file.createNewFile()) {
                System.out.println("File has been created!");
            } else {
                System.out.println("File " + file.getName() + " already exists.");
                System.out.println("deleting...");
                if (file.delete()) {
                    System.out.println(file.getName() + " is deleted!");
                } else {
                    System.out.println("Delete operation is failed.");
                }
                System.out.println("\ncreating...");
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(String name, StringBuffer sb) {

        try {
            File file = new File(PropertiesService.getInstance().getProperties().getProperty("logs.files.path") + name);
            if (file.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File " + file.getName() + " already exists.");
                System.out.println("deleting...");
                if (file.delete()) {
                    System.out.println(file.getName() + " is deleted!");
                } else {
                    System.out.println("Delete operation is failed.");
                }
                System.out.println("\ncreating...");
                file.createNewFile();
            }

            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(sb.toString());
                out.close();
                System.out.println("File Updated and Saved");
            } catch (IOException e) {
                System.out.println("Exception ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    public void downloadFileByURL(String urlString, String path, String fileName) {
        
        byte[] data = null;
        int len = Integer.parseInt(PropertiesService.getInstance().getProperties().getProperty("file.download.size"));
        System.out.println("max file size to be downloaded = " + len + " KB");
        try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();
            InputStream is = new BufferedInputStream(uc.getInputStream());
            
            try {
                data = new byte[len];
                int offset = 0;
                while (offset < len) {
                    int read = is.read(data, offset, data.length - offset);
                    if (read < 0) {
                        break;
                    }
                    offset += read;
                }

                String os = System.getProperty("os.name");
                String delimiter = "/";
                if (os.toLowerCase().contains("windows")) {
                    delimiter = "\\";
                }
                FileOutputStream fos = new FileOutputStream(path+delimiter+fileName);
                fos.write(data);
                fos.close();
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(FileService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

package it.unipi.dii.aide.mircv.utils;


import java.io.File;
import java.io.PrintWriter;


public class FileUtils {

    public static void CreateOrCleanFile(String path){
        File file = new File(path);

        try {
            if(file.createNewFile()){
                System.out.println("File created: " + file.getName() + " at path: " + file.getPath());

            } else {
                System.out.println("File Already exists.");
                try(PrintWriter writer = new PrintWriter(path)){
                    writer.print("");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

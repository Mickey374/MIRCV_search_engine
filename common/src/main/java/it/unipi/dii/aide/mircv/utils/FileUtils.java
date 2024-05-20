package it.unipi.dii.aide.mircv.utils;


import java.io.File;
import java.io.PrintWriter;

public class FileUtils {

    /**
     * Creates the file if it does not exist, else flushes it
     * @param path is the path of file to create or clean
     */
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

    /**
     * Creates the file if not exist
     * @param path is the path of the file to be created
     */
    public static void createIfNotExists(String path){
        File file = new File(path);
        try {
            if(file.createNewFile()) {
                System.out.println("File created: "+ file.getName() + " at path: " + file.getPath());
            } else {
                System.out.println("File " + file.getName() + " already exists, at path: " + file.getPath());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

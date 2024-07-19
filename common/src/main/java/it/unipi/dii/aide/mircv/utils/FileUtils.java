package it.unipi.dii.aide.mircv.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for file operations
 */
public class FileUtils {

    /**
     * creates the file if not exists
     * @param path is the path of the file to be created
     */
    public static void createIfNotExists(String path){
        File file = new File(path);

        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the file if it exists
     * @param path is the path of the file to be removed
     */
    public static void removeFile(String path) {
        File file = new File(path);
        if (file.exists())
            file.delete();
    }

    /**
     * Creates a directory of given path
     * @param path directory path
     **/
    public static void createDirectory(String path) {
        try {
            Path dirPath = Paths.get(path);

            Files.createDirectories(dirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path directory path
     *  Deletes directory of given path
     **/
    public static void deleteDirectory(String path) {
        File directory = new File(path);

        if(!directory.exists())
            return;

        boolean successful = true;
        // Before deleting the directory, delete all files
        for(File file : Objects.requireNonNull(directory.listFiles()))
            successful = file.delete();

        if(!successful)
            return;

        // Delete the directory
        successful = directory.delete();
    }
}

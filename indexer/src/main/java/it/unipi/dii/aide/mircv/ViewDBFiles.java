package it.unipi.dii.aide.mircv;


import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class ViewDBFiles {
    public static void main(String[] args) {
        // Path to your .db file
        String dbFilePath = "data/partialIndex.db";

        System.out.println("Viewing the contents of the .db file has " + dbFilePath.length());

        // Open the database
        DB db = DBMaker.fileDB(dbFilePath).make();

        // Access the map (assuming you have a map named "myMap")
        HTreeMap<Object, Object> map = (HTreeMap<Object, Object>) db.hashMap("myMap").createOrOpen();

        // Print the contents of the map
        map.forEach((key, value) -> System.out.println(key + " = " + value));

        // Close the database
        db.close();
    }
}



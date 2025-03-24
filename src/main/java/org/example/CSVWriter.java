package org.example;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
    private static boolean isFirstWrite = true;

    public static void writeToCSV(String data) {
        try {
            // Overwrite on first write, append thereafter
            FileWriter writer = new FileWriter("crave_scraped_data.csv", !isFirstWrite);
            writer.append(data);
            writer.append("\n");
            writer.close();
            isFirstWrite = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
import javafx.scene.control.TextArea;

import java.io.*;

public class Utilities {

    public static void exportToFile(String content, File file, TextArea log){
        try {
            //FileWriter fileWriter = null;
            //fileWriter = new FileWriter(file);
            //fileWriter.write(content);
            //fileWriter.close();
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),  "UTF8"));
            out.append(content);
            out.flush();
            out.close();
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            log.appendText("Error: " + e.getMessage());
        }
    }
}

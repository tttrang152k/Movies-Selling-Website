
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SyncAppend {
    //String dir;
    String filePath;

    public SyncAppend(String contextPath) {
        filePath = contextPath + "log.txt";
    }

    public synchronized void syncAppend(long time, int flag) throws IOException {
        File outFile = new File(filePath);
        if (!outFile.exists())
            outFile.createNewFile();

        // Append new TJ/TS value to outFile
        FileWriter fwriter = new FileWriter(outFile, true);
        if (flag == 0) { // TJ
            fwriter.write(String.valueOf(time) + " ");
            System.out.println("TJ time for search: " + time);
        }
        else {           // TS
            fwriter.write(String.valueOf(time) + "\n");
            System.out.println("TS time for search: " + time);
        }
        fwriter.flush();
        fwriter.close();

    }
}

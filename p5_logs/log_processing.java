import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class log_processing {

    public static void main(String[] args) {

        int count = 0;
        long sumTJ = 0;
        long sumTS = 0;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String fileName = args[i];
                System.out.println("File: " + fileName);
                try {
                    //File file = new File(fileName);
                    Scanner scanner = new Scanner(new File(fileName));

                    while (scanner.hasNextLine()){
                        String line = scanner.nextLine();
                        String[] values = line.split(" ");
                        sumTJ += Long.parseLong(values[0]);
                        sumTS += Long.parseLong(values[1]);
                        count++;
                    }
                }
                catch (FileNotFoundException e) {
                    System.out.println("File not found: " + e.getMessage());
                }
            }
            if (count > 0){
                double TJaverage = (double) sumTJ / count;
                System.out.println("TJ Average: " + TJaverage + " ms");
                double TSaverage = (double) sumTS / count;
                System.out.println("TS Average: " + TSaverage + " ms");
            }
            else {
                System.out.println("Empty file");
            }

        }
        else
            System.out.println("No file provided");

    }

}
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TextCounter {
    private int[] countedLines = null;
    private String[] textsToCount;
    public TextCounter(){}

    /**
     * Counts the number of lines in a given text
     * @param textsToCount - The texts to have their lines counted
     */
    public void countLines(String[] textsToCount){
        this.textsToCount = textsToCount;
        if (textsToCount.length == 0){
            throw new RuntimeException("No texts to be read");
        }
        int[] count = new int[textsToCount.length];
        for (int i = 0 ; i <textsToCount.length;i++){count[i] = 0;}
        for (int i = 0 ; i < textsToCount.length; i++) {
            File textFile;
            Scanner scanner;
            try {
                textFile = new File(textsToCount[i]);
                scanner = new Scanner(textFile);
            } catch (FileNotFoundException e){
                count[i] = -1;
                continue;
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                count[i]++;
            }
            scanner.close();
        }
        this.countedLines = count;
    }

    /**
     * Prints out the number of lines each text has
     */
    public void printCountedLines(){
        for (int i = 0; i < countedLines.length; i++) {
            if (countedLines[i] != -1){
                System.out.println(textsToCount[i]  + " has " + countedLines[i] + " lines");
            } else {
                System.out.println(textsToCount[i] + " does not exist.");
                continue;
            }

        }

        }
}

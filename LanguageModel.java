import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<>();
    }

    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<>();
    }

    public void train(String fileName) {
        CharDataMap.clear();

        String text;
        try {
            text = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            throw new IllegalArgumentException("File not found");
        }

        if (text.length() <= windowLength) return;

        for (int i = 0; i + windowLength < text.length(); i++) {
            String window = text.substring(i, i + windowLength);
            char next = text.charAt(i + windowLength);

            List list = CharDataMap.get(window);
            if (list == null) {
                list = new List();
                CharDataMap.put(window, list);
            }
            list.update(next);
        }
    }

    /** LEFT → RIGHT cumulative probability */
    public void calculateProbabilities(List probs) {
        if (probs == null || probs.getSize() == 0) return;

        CharData[] arr = probs.toArray();

        int total = 0;
        for (CharData cd : arr) total += cd.count;

        double cumulative = 0.0;
        for (int i = 0; i < arr.length; i++) {
            arr[i].p = (double) arr[i].count / total;
            cumulative += arr[i].p;
            arr[i].cp = cumulative;
        }

        arr[arr.length - 1].cp = 1.0;
    }

    public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        for (CharData cd : probs.toArray()) {
            if (r <= cd.cp) return cd.chr;
        }
        return probs.toArray()[probs.getSize() - 1].chr;
    }

    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) return initialText;

        StringBuilder result = new StringBuilder(initialText);
        int target = initialText.length() + textLength;

        while (result.length() < target) {
            String window = result.substring(result.length() - windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) break;

            calculateProbabilities(probs);
            result.append(getRandomChar(probs));
        }
        return result.toString();
    }
}
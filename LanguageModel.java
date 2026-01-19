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
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        if (text.length() < windowLength + 1) return;

        // Go RIGHT -> LEFT so addFirst/update produce the expected list order
        for (int i = text.length() - windowLength - 1; i >= 0; i--) {
            String window = text.substring(i, i + windowLength);
            char next = text.charAt(i + windowLength);

            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(next);
        }

        // Compute probabilities for every list
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

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
        arr[arr.length - 1].cp = 1.0; // rounding guard
    }

    public char getRandomChar(List probs) {
       if (probs == null || probs.getSize() == 0) {
        throw new IllegalArgumentException("Probability list is empty");
        }

        double r = randomGenerator.nextDouble(); // r in [0,1)
        CharData[] arr = probs.toArray();

        for (CharData cd : arr) {
            if (cd.cp > r) {
                return cd.chr;
            }
        }

        return arr[arr.length - 1].chr;
    }

    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) return initialText;

        StringBuilder out = new StringBuilder(initialText);

        while (out.length() < textLength) {
            String window = out.substring(out.length() - windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) break;
            out.append(getRandomChar(probs));
        }
        return out.toString();
    }
}
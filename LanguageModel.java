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
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        CharDataMap.clear();

        String text;
        try {
            text = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        if (text.length() <= windowLength) return;

        for (int i = 0; i + windowLength < text.length(); i++) {
            String window = text.substring(i, i + windowLength);
            char nextChr = text.charAt(i + windowLength);

            List lst = CharDataMap.get(window);
            if (lst == null) {
                lst = new List();
                CharDataMap.put(window, lst);
            }
            lst.update(nextChr);
        }
}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list.
	public void calculateProbabilities(List probs) {
        if (probs == null || probs.getSize() == 0) return;

        CharData[] arr = probs.toArray();

        int total = 0;
        for (CharData cd : arr) {
            total += cd.count;
        }

        double cumulative = 0.0;

        for (int i = 0; i < arr.length; i++) {
            arr[i].p = (double) arr[i].count / total;
            cumulative += arr[i].p;
            arr[i].cp = cumulative;
        }

        arr[arr.length - 1].cp = 1.0;
    }

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		
        if (probs == null || probs.getSize() == 0) {
        throw new IllegalArgumentException("Probability list is empty");
        }

        double r = randomGenerator.nextDouble();

        CharData[] arr = probs.toArray();
        for (CharData cd : arr) {
            if (r <= cd.cp) {
                return cd.chr;
            }
        }

        return arr[arr.length - 1].chr;
    }

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
	    
        StringBuilder result = new StringBuilder(initialText);

        if (initialText.length() < windowLength) {
            return initialText;
        }

        int targetLength = initialText.length() + textLength;

        while (result.length() < targetLength) {
            String window = result.substring(result.length() - windowLength);

            List probs = CharDataMap.get(window);
            if (probs == null || probs.getSize() == 0) {
                break;
            }

            calculateProbabilities(probs);
            char nextChar = getRandomChar(probs);
            result.append(nextChar);
        }

    return result.toString();
    }

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}
}

import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /**
     * Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the
     * same seed value will produce the same random texts. Good for debugging.
     */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production.
     */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        In reader = new In(fileName); // Open a file.
        String txt = reader.readAll(); // Read the file's text.
        for (int i = 0; i < txt.length() - windowLength; ++i) {
            String key = txt.substring(i, i + windowLength);
            // Check if the key exists.
            if (CharDataMap.get(key) == null) {
                CharDataMap.put(key, new List());
            }
            CharDataMap.get(key).update(txt.charAt(i + windowLength));
            calculateProbabilities(CharDataMap.get(key));
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    public void calculateProbabilities(List probs) {
        int totalCharCount = 0;
        // Computes the total number of characters in the text.
        for (int i = 0; i < probs.getSize(); i++) {
            CharData currentCharData = probs.get(i);
            totalCharCount += currentCharData.count;
        }
        double cp = 0.0;
        // Computes and sets p & cp for each list element.
        for (int i = 0; i < probs.getSize(); i++) {
            CharData currentCharData = probs.get(i);
            currentCharData.p = (double) currentCharData.count / totalCharCount;
            cp += currentCharData.p;
            currentCharData.cp = cp;
        }
    }

    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble(); // draw a random number in [0,1).
        for (int i = 0; i < probs.getSize(); i++) {
            CharData currentCharData = probs.get(i);
            // Check if the element whose cumulative probability is greater than r.
            if (currentCharData.cp > r) {
                return currentCharData.chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training. 
     * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
     * doesn't appear as a key in Map, we generate no text and return only the initial text. 
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        String window = ""; 
        String generatedText = initialText; 
        char chr; 
        if (windowLength > initialText.length() || initialText.length() >= textLength) {
            return initialText;
        } else {
            window = initialText.substring(initialText.length() - windowLength);
            while (generatedText.length() - windowLength < textLength) {
                if (CharDataMap.containsKey(window)) {
                    chr = getRandomChar(CharDataMap.get(window));
                    generatedText += chr;
                    window = window.substring(1) + chr;
                } else {
                    return generatedText;
                }
            }
            return generatedText;
        }
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

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Creates the LanguageModel object.
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}

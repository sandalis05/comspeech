import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.translation.*;

public class App {

    static final String SPEECH__SUBSCRIPTION__KEY = System.getenv("SPEECH__SUBSCRIPTION__KEY");
    static final String SPEECH__SERVICE__REGION = System.getenv("SPEECH__SERVICE__REGION");

    public static void main(String[] args) {
        try {
            translateSpeech();
            System.exit(0);
        } catch (Exception ex) {
            System.out.println(ex);
            System.exit(1);
        }
    }

    static void translateSpeech() throws ExecutionException, FileNotFoundException, InterruptedException, IOException {
        SpeechTranslationConfig translationConfig = SpeechTranslationConfig.fromSubscription(
                SPEECH__SUBSCRIPTION__KEY, SPEECH__SERVICE__REGION);

        String fromLanguage = "en-US";
        String toLanguage = "de";
        translationConfig.setSpeechRecognitionLanguage(fromLanguage);
        translationConfig.addTargetLanguage(toLanguage);

        // See: https://aka.ms/speech/sdkregion#standard-and-neural-voices
        translationConfig.setVoiceName("de-DE-Hedda");

        try (TranslationRecognizer recognizer = new TranslationRecognizer(translationConfig)) {
            recognizer.synthesizing.addEventListener((s, e) -> {
                byte[] audio = e.getResult().getAudio();
                int size = audio.length;
                System.out.println("Audio synthesized: " + size + " byte(s)" + (size == 0 ? "(COMPLETE)" : ""));

                if (size > 0) {
                    try (FileOutputStream file = new FileOutputStream("translation.wav")) {
                        file.write(audio);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            System.out.printf("Say something in '%s' and we'll translate...", fromLanguage);

            TranslationRecognitionResult result = recognizer.recognizeOnceAsync().get();
            if (result.getReason() == ResultReason.TranslatedSpeech) {
                System.out.printf("Recognized: \"%s\"\n", result.getText());
                for (Map.Entry<String, String> pair : result.getTranslations().entrySet()) {
                    String language = pair.getKey();
                    String translation = pair.getValue();
                    System.out.printf("Translated into '%s': %s\n", language, translation);
                }
            }
        }
    }

}

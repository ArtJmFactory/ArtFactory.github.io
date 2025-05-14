package com.example.verbs1;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent; // Pour le lien sponsor
import android.net.Uri;      // Pour le lien sponsor
import android.widget.ImageView; // Pour le logo sponsor

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuizFragment extends Fragment implements TextToSpeech.OnInitListener {

    // --- Vues du layout ---
    private TextView scoreTextView;
    private TextView encouragementTextView;
    private TextView levelInfoTextView;
    private TextView frenchVerbTextView;
    private ImageButton speakFormsButton;
    private EditText baseFormEditText;
    private EditText pastSimpleEditText;
    private EditText pastParticipleEditText;
    private TextView feedbackTextView;
    private TextView triesSummaryTitleTextView;    // Pour le titre du résumé
    private TextView triesSummaryDetailsTextView;  // Pour les détails du résumé
    private Button validateButton;
    private Button nextButton;
    private Button backButton;
    private ImageView sponsorLogoImageView;

    // --- Données du quiz ---
    private List<IrregularVerb> verbsForQuiz = new ArrayList<>();
    private IrregularVerb currentVerb;
    private int currentVerbIndex = -1;
    private int score = 0;
    private String userName = "";
    private SharedPreferencesHelper prefsHelper;
    private int remainingTries = 3;
    private int successTry1Count = 0;
    private int successTry2Count = 0;
    private int successTry3Count = 0;
    private int failedCount = 0;
    private String selectedLevel = "ALL";
    private Random random = new Random();
    private int defaultEditTextColor;

    // --- Variable pour TextToSpeech ---
    private TextToSpeech tts;
    private boolean ttsInitialized = false;

    // --- Variables pour SoundPool ---
    private SoundPool soundPool;
    private int successSoundId;
    private int failSoundId;
    private boolean soundPoolLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        tts = new TextToSpeech(requireContext(), this);
        setupSoundPool();
        return view;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "La langue Anglais (UK/US) n'est pas supportée ou les données sont manquantes.");
                Toast.makeText(requireContext(), "Synthèse vocale pour l'anglais non disponible.", Toast.LENGTH_LONG).show();
                ttsInitialized = false;
            } else {
                ttsInitialized = true;
                Log.i("TTS", "TextToSpeech initialisé avec succès pour l'anglais.");
            }
        } else {
            Log.e("TTS", "Échec de l'initialisation de TextToSpeech! Status: " + status);
            Toast.makeText(requireContext(), "Impossible d'initialiser la synthèse vocale.", Toast.LENGTH_LONG).show();
            ttsInitialized = false;
        }
    }

    private void setupSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) Log.d("SoundPool", "Sound loaded: ID " + sampleId);
            else Log.e("SoundPool", "Error loading sound: ID " + sampleId + " Status: " + status);
        });
        successSoundId = soundPool.load(requireContext(), R.raw.success, 1);
        failSoundId = soundPool.load(requireContext(), R.raw.fail, 1);
        soundPoolLoaded = true;
        Log.d("SoundPool", "Sound loading initiated. Success ID: " + successSoundId + ", Fail ID: " + failSoundId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new SharedPreferencesHelper(requireContext());

        scoreTextView = view.findViewById(R.id.textview_quiz_score);
        encouragementTextView = view.findViewById(R.id.textview_quiz_encouragement);
        levelInfoTextView = view.findViewById(R.id.textview_quiz_level_info);
        frenchVerbTextView = view.findViewById(R.id.textview_quiz_french_verb);
        speakFormsButton = view.findViewById(R.id.button_quiz_speak_forms);
        baseFormEditText = view.findViewById(R.id.edittext_quiz_base_form);
        pastSimpleEditText = view.findViewById(R.id.edittext_quiz_past_simple);
        pastParticipleEditText = view.findViewById(R.id.edittext_quiz_past_participle);
        feedbackTextView = view.findViewById(R.id.textview_quiz_feedback);
        triesSummaryTitleTextView = view.findViewById(R.id.textview_quiz_summary_title);
        triesSummaryDetailsTextView = view.findViewById(R.id.textview_quiz_summary_details);
        validateButton = view.findViewById(R.id.button_quiz_validate);
        nextButton = view.findViewById(R.id.button_quiz_next);
        backButton = view.findViewById(R.id.button_quiz_back);
        sponsorLogoImageView = view.findViewById(R.id.imageview_sponsor_logo);

        if (baseFormEditText != null) {
            defaultEditTextColor = baseFormEditText.getCurrentTextColor();
        } else {
            defaultEditTextColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text); // Fallback plus neutre
            Log.e("QuizFragment", "baseFormEditText is null in onViewCreated!");
        }

        userName = prefsHelper.getUserName();
        if (TextUtils.isEmpty(userName)) {
            userName = "Joueur";
        }

        if (getArguments() != null) {
            selectedLevel = getArguments().getString("USER_LEVEL_CODE", "ALL");
        } else {
            selectedLevel = "ALL";
        }
        Log.d("QuizFragment", "Using level code: " + selectedLevel);

        setupListeners();
        loadVerbsAndStartGame();
    }

    private void loadVerbsAndStartGame() {
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Log.d("QuizFragment", "Loading verbs for level: " + selectedLevel);
            switch (selectedLevel) {
                case "D": verbsForQuiz = verbDao.getVerbsByCategory("D"); break;
                case "C+D": verbsForQuiz = verbDao.getVerbsForIntermediateLevel(); break;
                case "ALL": default: verbsForQuiz = verbDao.getAllVerbsSortedByFrench(); break;
            }
            Log.d("QuizFragment", "Loaded " + verbsForQuiz.size() + " verbs for level " + selectedLevel);
            final int finalVerbCount = (verbsForQuiz != null) ? verbsForQuiz.size() : 0;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (finalVerbCount == 0) {
                        if(feedbackTextView != null) feedbackTextView.setText("Erreur: Aucun verbe trouvé pour ce niveau.");
                        if(validateButton != null) validateButton.setEnabled(false);
                        if(levelInfoTextView != null) levelInfoTextView.setText("");
                        if(triesSummaryTitleTextView != null) triesSummaryTitleTextView.setVisibility(View.GONE);
                        if(triesSummaryDetailsTextView != null) triesSummaryDetailsTextView.setVisibility(View.GONE);
                    } else {
                        if(validateButton != null) validateButton.setEnabled(true);
                        String levelName;
                        String levelFormat = getString(R.string.level_choice_format);
                        switch (selectedLevel) {
                            case "D": levelName = getString(R.string.level_choice_beginner_base); break;
                            case "C+D": levelName = getString(R.string.level_choice_intermediate_base); break;
                            case "ALL": default: levelName = getString(R.string.level_choice_expert_base); break;
                        }
                        if (levelInfoTextView != null) {
                            levelInfoTextView.setText(String.format(levelFormat, levelName, finalVerbCount));
                        }
                        startGame();
                    }
                });
            }
        });
    }

    private void setupListeners() {
        if(validateButton != null) validateButton.setOnClickListener(v -> validateAnswer());
        if(nextButton != null) nextButton.setOnClickListener(v -> loadNextVerb());
        if(backButton != null) backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        if (speakFormsButton != null) {
            speakFormsButton.setOnClickListener(v -> speakCurrentVerbForms());
        }
        if (sponsorLogoImageView != null) {
            sponsorLogoImageView.setOnClickListener(v -> {
                String url = "https://theenglishspeakingcenter.fr/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Impossible d'ouvrir le lien du sponsor.", Toast.LENGTH_SHORT).show();
                    Log.e("QuizFragment", "Error opening sponsor URL: " + url, e);
                }
            });
        }
    }

    private void startGame() {
        score = 0;
        successTry1Count = 0;
        successTry2Count = 0;
        successTry3Count = 0;
        failedCount = 0;
        updateScoreDisplay();
        updateTriesSummaryDisplay(); // Appeler pour afficher le résumé initial (tout à 0)
        if (verbsForQuiz != null && !verbsForQuiz.isEmpty()) Collections.shuffle(verbsForQuiz);
        currentVerbIndex = -1;
        loadNextVerb();
    }

    private void loadNextVerb() {
        if (verbsForQuiz == null || verbsForQuiz.isEmpty()) {
            if(feedbackTextView != null) feedbackTextView.setText("Aucun verbe à réviser !");
            if(validateButton != null) validateButton.setEnabled(false);
            if(nextButton != null) nextButton.setVisibility(View.GONE);
            if(frenchVerbTextView != null) frenchVerbTextView.setText("");
            clearInputFields(); setInputFieldsEnabled(false);
            if (speakFormsButton != null) speakFormsButton.setVisibility(View.GONE);
            // Cache le résumé des essais quand on charge un nouveau verbe
            if (triesSummaryTitleTextView != null) triesSummaryTitleTextView.setVisibility(View.GONE);
            if (triesSummaryDetailsTextView != null) triesSummaryDetailsTextView.setVisibility(View.GONE);
            return;
        }
        if(validateButton != null) validateButton.setEnabled(true);
        if (speakFormsButton != null) speakFormsButton.setVisibility(View.GONE);

        currentVerbIndex++;
        if (currentVerbIndex >= verbsForQuiz.size()) {
            Collections.shuffle(verbsForQuiz);
            currentVerbIndex = 0;
            Toast.makeText(requireContext(), "Liste terminée, on recommence !", Toast.LENGTH_SHORT).show();
        }
        currentVerb = verbsForQuiz.get(currentVerbIndex);
        remainingTries = 3;

        // Ne pas afficher le résumé ici, mais le mettre à jour après chaque réponse
        // updateTriesSummaryDisplay(); // Commenté, car on veut l'afficher APRÈS une réponse.

        if(frenchVerbTextView != null) frenchVerbTextView.setText(currentVerb.getFrench());
        updateEncouragementMessage();
        clearInputFields();
        resetEditTextColors();
        if(feedbackTextView != null) feedbackTextView.setText("");
        if(feedbackTextView != null) feedbackTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        if(validateButton != null) validateButton.setVisibility(View.VISIBLE);
        if(nextButton != null) nextButton.setVisibility(View.GONE);
        setInputFieldsEnabled(true);
    }

    private void resetEditTextColors() {
        if(baseFormEditText != null) baseFormEditText.setTextColor(defaultEditTextColor);
        if(pastSimpleEditText != null) pastSimpleEditText.setTextColor(defaultEditTextColor);
        if(pastParticipleEditText != null) pastParticipleEditText.setTextColor(defaultEditTextColor);
    }

    private void validateAnswer() {
        if (currentVerb == null) return;
        resetEditTextColors();
        // Cache le résumé si on revalide avant de passer au suivant (il sera réaffiché après la validation)
        if (triesSummaryTitleTextView != null) triesSummaryTitleTextView.setVisibility(View.GONE);
        if (triesSummaryDetailsTextView != null) triesSummaryDetailsTextView.setVisibility(View.GONE);

        String baseAnswer = baseFormEditText.getText().toString().trim();
        String pastSimpleAnswer = pastSimpleEditText.getText().toString().trim();
        String pastParticipleAnswer = pastParticipleEditText.getText().toString().trim();

        boolean isBaseCorrect = baseAnswer.equalsIgnoreCase(currentVerb.getBaseForm());
        boolean isPastSimpleCorrect = checkAlternativeForms(pastSimpleAnswer, currentVerb.getPastSimple());
        boolean isPastParticipleCorrect = pastParticipleAnswer.equalsIgnoreCase(currentVerb.getPastParticiple());

        if (isBaseCorrect && isPastSimpleCorrect && isPastParticipleCorrect) {
            handleCorrectAnswer();
        } else {
            if (!isBaseCorrect && baseFormEditText != null) { baseFormEditText.setTextColor(Color.RED); }
            if (!isPastSimpleCorrect && pastSimpleEditText != null) { pastSimpleEditText.setTextColor(Color.RED); }
            if (!isPastParticipleCorrect && pastParticipleEditText != null) { pastParticipleEditText.setTextColor(Color.RED); }
            handleIncorrectAnswer();
        }
    }

    private void handleCorrectAnswer() {
        resetEditTextColors();
        int points = 0;
        String feedbackMsg = "";
        switch (remainingTries) {
            case 3: points = 10; feedbackMsg = getString(R.string.feedback_correct_try1); successTry1Count++; break;
            case 2: points = 5;  feedbackMsg = getString(R.string.feedback_correct_try2); successTry2Count++; break;
            case 1: points = 3;  feedbackMsg = getString(R.string.feedback_correct_try3); successTry3Count++; break;
        }
        score += points;
        updateScoreDisplay();
        updateTriesSummaryDisplay(); // Mettre à jour et afficher après incrémentation

        if(feedbackTextView != null) feedbackTextView.setText(feedbackMsg);
        if(feedbackTextView != null) feedbackTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.feedback_green_dark));

        int bestScore = prefsHelper.getBestScore();
        if (score > bestScore) {
            prefsHelper.saveBestScore(score);
            prefsHelper.saveBestScoreUserName(this.userName);
            Toast.makeText(requireContext(), "Nouveau record : " + score + "!", Toast.LENGTH_SHORT).show();
        }
        playSound(successSoundId);
        speakCurrentVerbForms();
        if (speakFormsButton != null) speakFormsButton.setVisibility(View.VISIBLE);
        showNextButton();
    }

    private void handleIncorrectAnswer() {
        remainingTries--;
        updateEncouragementMessage();
        String feedbackMsg;
        playSound(failSoundId);

        if (speakFormsButton != null && remainingTries > 0) { speakFormsButton.setVisibility(View.GONE); }

        if (remainingTries > 0) {
            feedbackMsg = (remainingTries == 2) ? getString(R.string.feedback_incorrect_try1) : getString(R.string.feedback_incorrect_try2);
            if(feedbackTextView != null) feedbackTextView.setTextColor(Color.RED);
        } else {
            failedCount++; // Incrémenter ici, après le dernier essai
            feedbackMsg = getString(R.string.feedback_incorrect_try3_no_placeholder); // Utilise la string sans placeholders pour correction
            if(feedbackTextView != null) feedbackTextView.setTextColor(Color.RED);
            // Les corrections individuelles sont gérées par la mise en rouge dans validateAnswer
            showCorrection(); // N'est plus nécessaire pour afficher le texte, seulement pour la parole et désactiver
            setInputFieldsEnabled(false);
            speakCurrentVerbForms(); // Prononcer la correction
            showNextButton();
            if (speakFormsButton != null) speakFormsButton.setVisibility(View.VISIBLE);
        }
        if(feedbackTextView != null) feedbackTextView.setText(feedbackMsg);
        updateTriesSummaryDisplay(); // Mettre à jour et afficher après avoir potentiellement incrémenté failedCount
    }

    private void speakCurrentVerbForms() {
        if (currentVerb != null && ttsInitialized && tts != null) {
            String base = currentVerb.getBaseForm();
            String past = currentVerb.getPastSimple();
            String participle = currentVerb.getPastParticiple();
            String pastToSpeak = past.contains("/") ? past.split("/")[0].trim() : past;

            Log.d("TTS", "Speaker Icon or Correction: Attempting to speak: " + base + " / " + pastToSpeak + " / " + participle);

            tts.speak(base, TextToSpeech.QUEUE_FLUSH, null, "speakBase");
            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
            tts.speak(pastToSpeak, TextToSpeech.QUEUE_ADD, null, "speakPast");
            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
            tts.speak(participle, TextToSpeech.QUEUE_ADD, null, "speakParticiple");
        } else {
            if (currentVerb == null) Log.w("TTS", "Cannot speak, currentVerb is null");
            if (!ttsInitialized || tts == null) Log.w("TTS", "Cannot speak, TTS not ready");
        }
    }

    private boolean checkAlternativeForms(String answer, String correctForms) {
        if (correctForms == null || correctForms.isEmpty()) { return answer.isEmpty(); }
        String[] alternatives = correctForms.split("/");
        for (String alternative : alternatives) {
            if (answer.equalsIgnoreCase(alternative.trim())) { return true; }
        }
        return false;
    }

    private void updateTriesSummaryDisplay() {
        if (triesSummaryTitleTextView != null && triesSummaryDetailsTextView != null) {
            triesSummaryTitleTextView.setText(getString(R.string.quiz_summary_title_text));
            triesSummaryTitleTextView.setVisibility(View.VISIBLE);

            SpannableString s1 = new SpannableString(String.valueOf(successTry1Count));
            s1.setSpan(new StyleSpan(Typeface.BOLD), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableString s2 = new SpannableString(String.valueOf(successTry2Count));
            s2.setSpan(new StyleSpan(Typeface.BOLD), 0, s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableString s3 = new SpannableString(String.valueOf(successTry3Count));
            s3.setSpan(new StyleSpan(Typeface.BOLD), 0, s3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            SpannableString sf = new SpannableString(String.valueOf(failedCount));
            sf.setSpan(new StyleSpan(Typeface.BOLD), 0, sf.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            String format = getString(R.string.quiz_tries_summary_details_format); // "1er essai: %1$s | 2ème: %2$s | 3ème: %3$s | Échoué: %4$s"

            // Remplacement manuel des placeholders pour conserver le style gras
            String temp = format.replace("%1$s", "PLACEHOLDER1")
                    .replace("%2$s", "PLACEHOLDER2")
                    .replace("%3$s", "PLACEHOLDER3")
                    .replace("%4$s", "PLACEHOLDER4");

            String[] parts = temp.split("PLACEHOLDER[1-4]");
            // Logique pour reconstruire la chaîne avec les Spannables
            // S'assure qu'il y a assez de parties pour éviter IndexOutOfBounds
            if (parts.length > 0) builder.append(parts[0]);
            if (parts.length > 1) { builder.append(s1); builder.append(parts[1]); } else { builder.append(s1); }
            if (parts.length > 2) { builder.append(s2); builder.append(parts[2]); } else { builder.append(s2); }
            if (parts.length > 3) { builder.append(s3); builder.append(parts[3]); } else { builder.append(s3); }
            if (parts.length > 4) { builder.append(sf); builder.append(parts[4]); } else { builder.append(sf); }


            triesSummaryDetailsTextView.setText(builder);
            triesSummaryDetailsTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updateEncouragementMessage() {
        if(encouragementTextView != null) encouragementTextView.setText(String.format(getString(R.string.quiz_encouragement_template), userName, remainingTries));
    }

    private void updateScoreDisplay() {
        if(scoreTextView != null) scoreTextView.setText(getString(R.string.quiz_score_label).split(":")[0] + ": " + score);
    }

    private void clearInputFields() {
        if(baseFormEditText != null) baseFormEditText.setText("");
        if(pastSimpleEditText != null) pastSimpleEditText.setText("");
        if(pastParticipleEditText != null) pastParticipleEditText.setText("");
    }

    private void setInputFieldsEnabled(boolean enabled) {
        if(baseFormEditText != null) baseFormEditText.setEnabled(enabled);
        if(pastSimpleEditText != null) pastSimpleEditText.setEnabled(enabled);
        if(pastParticipleEditText != null) pastParticipleEditText.setEnabled(enabled);
    }
    private void showCorrection() {
        if (currentVerb == null) return;
        if(baseFormEditText != null) baseFormEditText.setText(currentVerb.getBaseForm());
        if(pastSimpleEditText != null) pastSimpleEditText.setText(currentVerb.getPastSimple());
        if(pastParticipleEditText != null) pastParticipleEditText.setText(currentVerb.getPastParticiple());
    resetEditTextColors();
    setInputFieldsEnabled(false);
    speakCurrentVerbForms();
    }

    private void showNextButton() {
        if(validateButton != null) validateButton.setVisibility(View.GONE);
        if(nextButton != null) nextButton.setVisibility(View.VISIBLE);
        setInputFieldsEnabled(false);
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d("TTS", "TextToSpeech arrêté et libéré.");
            tts = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundPoolLoaded = false;
            Log.d("SoundPool", "SoundPool libéré.");
        }
        super.onDestroy();
    }

    private void playSound(int soundId) {
        if (soundPoolLoaded && soundPool != null && soundId > 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            Log.d("SoundPool", "Playing sound ID: " + soundId);
        } else {
            Log.w("SoundPool", "SoundPool not ready or sound ID invalid (" + soundId + "), cannot play sound.");
        }
    }
}
package com.example.verbs1;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    // Nom du fichier de préférences (privé à l'application)
    private static final String PREF_FILE_NAME = "VerbsAppPreferences";

    // Clés pour les valeurs que nous allons sauvegarder
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_BEST_SCORE_USER_NAME = "best_score_user_name"; // Nom du joueur du meilleur score

    private SharedPreferences sharedPreferences;

    // Constructeur qui prend le contexte (nécessaire pour accéder aux SharedPreferences)
    public SharedPreferencesHelper(Context context) {
        // Initialise l'objet SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    // --- Méthodes pour le nom de l'utilisateur ---

    /**
     * Sauvegarde le nom de l'utilisateur.
     * @param name Le nom à sauvegarder.
     */
    public void saveBestScoreUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BEST_SCORE_USER_NAME, name);
        editor.apply();
    }
    public void saveUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit(); // Ouvre l'éditeur
        editor.putString(KEY_USER_NAME, name); // Met la valeur String
        editor.apply(); // Applique les changements (asynchrone)
    }

    /**
     * Récupère le nom de l'utilisateur sauvegardé.
     * @return Le nom sauvegardé, ou une chaîne vide ("") s'il n'y en a pas.
     */
    public String getBestScoreUserName() {
        return sharedPreferences.getString(KEY_BEST_SCORE_USER_NAME, "");
    }
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, ""); // Lit la valeur, "" par défaut
    }


    // --- Méthodes pour le meilleur score ---

    /**
     * Sauvegarde le meilleur score.
     * @param score Le score à sauvegarder.
     */
    public void saveBestScore(int score) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_BEST_SCORE, score); // Met la valeur Int
        editor.apply();
    }

    /**
     * Récupère le meilleur score sauvegardé.
     * @return Le meilleur score sauvegardé, ou 0 s'il n'y en a pas.
     */
    public int getBestScore() {
        return sharedPreferences.getInt(KEY_BEST_SCORE, 0); // Lit la valeur, 0 par défaut
    }

    // On pourrait ajouter d'autres méthodes ici si besoin (ex: sauvegarder le niveau choisi)
}
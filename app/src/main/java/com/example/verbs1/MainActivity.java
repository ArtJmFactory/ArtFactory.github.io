package com.example.verbs1;

// Imports nécessaires pour AppCompatActivity, Bundle et EdgeToEdge/Insets
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
// --- Import pour SplashScreen ---
import androidx.core.splashscreen.SplashScreen;
// --- Fin Import ---
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Imports nécessaires pour les Fragments
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Activation de l'affichage EdgeToEdge (plein écran)
        EdgeToEdge.enable(this);

        // Lie le fichier de layout activity_main.xml à cette activité
        setContentView(R.layout.activity_main);

        // Code pour gérer les Insets (espaces pour les barres système)
        // Applique un listener au layout racine (qui a l'ID 'main')
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Récupère les dimensions des barres système (status bar, navigation bar)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Applique un padding à la vue racine pour décaler le contenu
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Retourne les insets (nécessaire pour le listener)
            return insets;
        });

        // Charge le premier Fragment (LevelFragment) uniquement si l'activité
        // vient d'être créée (savedInstanceState est null).
        // Si l'activité est recréée (ex: rotation), le système restaure
        // automatiquement le dernier fragment affiché.
        if (savedInstanceState == null) {
            loadFragment(new LevelFragment(), false); // Charge LevelFragment sans l'ajouter au backstack
        }
    }
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Remplace le contenu de R.id.fragment_container par le nouveau fragment
        fragmentTransaction.replace(R.id.fragment_container, fragment);

        // Si demandé, ajoute cette transaction à la pile arrière,
        // permettant à l'utilisateur de revenir avec le bouton Retour.
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null); // Le 'null' ici est ok pour un nom de pile simple
        }

        fragmentTransaction.commit(); // Valide (exécute) la transaction
    }
    // --- NOUVELLE MÉTHODE pour naviguer vers le Quiz AVEC arguments ---
    public void navigateToQuizScreenWithArgs(Bundle args) {
        QuizFragment quizFragment = new QuizFragment();
        quizFragment.setArguments(args); // Attache le Bundle d'arguments au Fragment
        loadFragment(quizFragment, true); // Charge le fragment (le true ajoute au backstack)
    }

    public void navigateToVerbListScreen() {
        // Charge VerbListFragment et ajoute au backstack pour pouvoir revenir
        loadFragment(new VerbListFragment(), true);
    }
}
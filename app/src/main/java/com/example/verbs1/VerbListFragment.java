package com.example.verbs1;

import android.content.Context; // Ajout pour le contexte de l'appli
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText; // Pour le dialogue d'ajout
import android.widget.LinearLayout; // Pour le dialogue d'ajout
import android.widget.RadioButton; // Pour le dialogue d'ajout
import android.widget.RadioGroup; // Pour le dialogue d'ajout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Pour les dialogues de confirmation/ajout
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VerbListFragment extends Fragment implements VerbAdapter.OnVerbActionListener {

    private RecyclerView recyclerViewVerbs;
    private VerbAdapter verbAdapter;
    private List<IrregularVerb> verbList = new ArrayList<>();
    private Button backButton;
    // --- NOUVELLES VARIABLES POUR LES BOUTONS ---
    private Button addVerbButton;
    private Button resetListButton;
    // --- FIN NOUVELLES VARIABLES ---

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verb_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewVerbs = view.findViewById(R.id.recyclerview_verbs);
        backButton = view.findViewById(R.id.button_verb_list_back);
        // --- Initialisation des nouveaux boutons ---
        addVerbButton = view.findViewById(R.id.button_add_verb);
        resetListButton = view.findViewById(R.id.button_reset_verb_list);
        // --- Fin initialisation ---

        verbAdapter = new VerbAdapter(verbList, this);
        recyclerViewVerbs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewVerbs.setAdapter(verbAdapter);

        loadVerbsFromDatabase();

        // --- Listeners pour les nouveaux boutons ---
        addVerbButton.setOnClickListener(v -> showAddVerbDialog());
        resetListButton.setOnClickListener(v -> showResetListConfirmationDialog());
        // --- Fin listeners ---

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadVerbsFromDatabase() {
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            final List<IrregularVerb> loadedVerbs = verbDao.getAllVerbsSortedByFrench();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    verbList.clear(); // Vider la liste actuelle avant d'ajouter les nouvelles données
                    verbList.addAll(loadedVerbs); // Ajouter les nouvelles données
                    verbAdapter.notifyDataSetChanged(); // Notifier l'adapter du changement complet
                    Log.d("VerbListFragment", "Loaded verbs from DB: " + loadedVerbs.size());
                });
            }
        });
    }

    // --- Méthode pour afficher le dialogue d'ajout de verbe ---
    private void showAddVerbDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ajouter un nouveau verbe");

        // Créer le layout pour le dialogue dynamiquement
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int paddingInDp = 16; // Padding en dp
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPixels = (int) (paddingInDp * scale + 0.5f);
        layout.setPadding(paddingInPixels, paddingInPixels / 2, paddingInPixels, paddingInPixels / 2);


        final EditText frenchInput = new EditText(requireContext());
        frenchInput.setHint("Français");
        layout.addView(frenchInput);

        final EditText baseInput = new EditText(requireContext());
        baseInput.setHint("Base verbale (Anglais)");
        layout.addView(baseInput);

        final EditText pastInput = new EditText(requireContext());
        pastInput.setHint("Prétérit");
        layout.addView(pastInput);

        final EditText participleInput = new EditText(requireContext());
        participleInput.setHint("Participe passé");
        layout.addView(participleInput);

        // Choix de catégorie
        TextView categoryLabel = new TextView(requireContext());
        categoryLabel.setText("Catégorie :");
        categoryLabel.setPadding(0, paddingInPixels / 2, 0, paddingInPixels / 4);
        layout.addView(categoryLabel);

        final RadioGroup categoryGroup = new RadioGroup(requireContext());
        categoryGroup.setOrientation(RadioGroup.HORIZONTAL);

        final RadioButton catD = new RadioButton(requireContext()); catD.setText("D"); catD.setId(View.generateViewId());
        final RadioButton catC = new RadioButton(requireContext()); catC.setText("C"); catC.setId(View.generateViewId()); catC.setChecked(true); // C par défaut
        final RadioButton catE = new RadioButton(requireContext()); catE.setText("E"); catE.setId(View.generateViewId());

        categoryGroup.addView(catD);
        categoryGroup.addView(catC);
        categoryGroup.addView(catE);
        layout.addView(categoryGroup);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String french = frenchInput.getText().toString().trim();
            String base = baseInput.getText().toString().trim();
            String past = pastInput.getText().toString().trim();
            String participle = participleInput.getText().toString().trim();
            String category = "C"; // Valeur par défaut

            int checkedId = categoryGroup.getCheckedRadioButtonId();
            if (checkedId == catD.getId()) category = "D";
            else if (checkedId == catE.getId()) category = "E";
            // else C (déjà par défaut)

            if (french.isEmpty() || base.isEmpty() || past.isEmpty() || participle.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            IrregularVerb newVerb = new IrregularVerb(french, base, past, participle, category);
            VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
            AppDatabase.databaseWriteExecutor.execute(() -> {
                verbDao.insertVerb(newVerb);
                // Recharger la liste sur le thread UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::loadVerbsFromDatabase); // Recharge la liste depuis la BDD
                }
            });
            Toast.makeText(requireContext(), "Verbe '" + french + "' ajouté.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    // --- Méthode pour confirmer la réinitialisation ---
    private void showResetListConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Réinitialiser la liste")
                .setMessage("Êtes-vous sûr de vouloir supprimer toutes les modifications et restaurer la liste d'origine ?")
                .setPositiveButton("Oui, réinitialiser", (dialog, which) -> {
                    resetVerbListToInitialState();
                })
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // --- Méthode pour réinitialiser la liste ---
    private void resetVerbListToInitialState() {
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        Context appContext = requireContext().getApplicationContext(); // Contexte applicatif pour le peuplement

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Supprimer tous les verbes actuels
            verbDao.deleteAllVerbs();
            Log.d("VerbListFragment", "All verbs deleted from DB.");

            // 2. Rappeler la méthode de peuplement initial de AppDatabase
            AppDatabase.populateInitialData(appContext, verbDao); // Assure-toi que populateInitialData est static et public
            Log.d("VerbListFragment", "Initial data repopulated.");

            // 3. Recharger la liste dans le RecyclerView sur le thread UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadVerbsFromDatabase);
            }
        });
        Toast.makeText(requireContext(), "Liste réinitialisée.", Toast.LENGTH_SHORT).show();
    }


    // --- Implémentation des méthodes de l'interface OnVerbActionListener ---
    @Override
    public void onCategoryChange(IrregularVerb verb, String newCategory) {
        String message = "Catégorie de '" + verb.getBaseForm() + "' changée en " + newCategory;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        verb.setCategory(newCategory);
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            verbDao.updateVerb(verb);
            Log.d("VerbListFragment", "Updated category in DB for: " + verb.getBaseForm());
        });
    }

    @Override
    public void onDeleteVerb(IrregularVerb verb, int position) {
        Toast.makeText(requireContext(), "Verbe '" + verb.getBaseForm() + "' supprimé", Toast.LENGTH_SHORT).show();
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            verbDao.deleteVerb(verb);
            Log.d("VerbListFragment", "Deleted verb from DB: " + verb.getBaseForm());
        });

        if (verbList != null && position >= 0 && position < verbList.size()) {
            if (verbList.get(position).getId() == verb.getId()) {
                verbList.remove(position);
                verbAdapter.notifyItemRemoved(position);
                verbAdapter.notifyItemRangeChanged(position, verbList.size());
            } else {
                Log.w("VerbListFragment", "Delete position mismatch, reloading list from DB.");
                loadVerbsFromDatabase(); // Si incohérence, recharge tout pour être sûr
            }
        }
    }
    // --- Fin de l'implémentation de l'interface ---
}
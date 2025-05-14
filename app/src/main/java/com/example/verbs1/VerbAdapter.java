package com.example.verbs1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List; // Pour utiliser List

// L'Adapter doit hériter de RecyclerView.Adapter et spécifier un ViewHolder
public class VerbAdapter extends RecyclerView.Adapter<VerbAdapter.VerbViewHolder> {

    private List<IrregularVerb> verbList; // La liste des verbes à afficher

    // --- Interface pour gérer les clics (suppression, changement catégorie) ---
    public interface OnVerbActionListener {
        void onCategoryChange(IrregularVerb verb, String newCategory);
        void onDeleteVerb(IrregularVerb verb, int position);
    }
    private OnVerbActionListener actionListener;
    // --- Fin de l'interface ---


    // Constructeur de l'Adapter
    public VerbAdapter(List<IrregularVerb> verbList, OnVerbActionListener listener) {
        this.verbList = verbList;
        this.actionListener = listener;
    }

    // --- Méthodes REQUISES par RecyclerView.Adapter ---

    // 1. Crée une nouvelle vue (ViewHolder) quand le RecyclerView en a besoin
    @NonNull
    @Override
    public VerbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Charge (inflate) le layout XML d'une ligne (list_item_verb.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_verb, parent, false);
        // Crée et retourne un ViewHolder associé à cette vue
        return new VerbViewHolder(itemView);
    }

    // 2. Lie les données d'un verbe spécifique à une vue (ViewHolder)
    @Override
    public void onBindViewHolder(@NonNull VerbViewHolder holder, int position) {
        // Récupère le verbe correspondant à cette position dans la liste
        IrregularVerb currentVerb = verbList.get(position);
        // Appelle la méthode bind du ViewHolder pour afficher les données
        holder.bind(currentVerb, position, actionListener);
    }

    // 3. Retourne le nombre total d'éléments dans la liste
    @Override
    public int getItemCount() {
        return verbList.size();
    }

    // --- Fin des méthodes REQUISES ---


    // --- Le ViewHolder ---
    // Représente UNE SEULE ligne dans le RecyclerView.
    // Il contient les références aux vues de cette ligne (TextViews, Buttons...).
    public static class VerbViewHolder extends RecyclerView.ViewHolder {
        // Vues de la ligne (déclarées ici)
        TextView frenchTextView;
        TextView formsTextView;
        RadioGroup categoryRadioGroup;
        RadioButton catDRadio, catCRadio, catERadio; // Pour pouvoir les cocher/décocher
        ImageButton deleteButton;

        // Constructeur du ViewHolder
        public VerbViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialise les vues de la ligne en les trouvant par leur ID
            frenchTextView = itemView.findViewById(R.id.textview_verb_french);
            formsTextView = itemView.findViewById(R.id.textview_verb_forms);
            categoryRadioGroup = itemView.findViewById(R.id.radiogroup_verb_category);
            catDRadio = itemView.findViewById(R.id.radio_cat_d);
            catCRadio = itemView.findViewById(R.id.radio_cat_c);
            catERadio = itemView.findViewById(R.id.radio_cat_e);
            deleteButton = itemView.findViewById(R.id.button_verb_delete);
        }

        // Méthode pour lier les données d'un verbe à cette ligne spécifique
        public void bind(final IrregularVerb verb, final int position, final OnVerbActionListener listener) {
            // Met à jour le contenu des TextViews
            frenchTextView.setText(verb.getFrench());
            String forms = verb.getBaseForm() + " / " + verb.getPastSimple() + " / " + verb.getPastParticiple();
            formsTextView.setText(forms);

            // --- Gestion de la catégorie ---
            // Décoche tous les boutons d'abord pour éviter les problèmes de recyclage
            categoryRadioGroup.clearCheck();
            // Coche le bon bouton radio en fonction de la catégorie du verbe
            switch (verb.getCategory()) {
                case "D":
                    catDRadio.setChecked(true);
                    break;
                case "C":
                    catCRadio.setChecked(true);
                    break;
                case "E":
                    catERadio.setChecked(true);
                    break;
            }

            // Ajoute un listener pour détecter le changement de catégorie
            categoryRadioGroup.setOnCheckedChangeListener(null); // Enlève l'ancien listener d'abord !
            categoryRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String newCategory = "";
                if (checkedId == R.id.radio_cat_d) newCategory = "D";
                else if (checkedId == R.id.radio_cat_c) newCategory = "C";
                else if (checkedId == R.id.radio_cat_e) newCategory = "E";

                // Si une catégorie valide est sélectionnée et différente de l'ancienne
                // ET si l'action vient bien de l'utilisateur (le bouton est pressé)
                if (!newCategory.isEmpty() && !newCategory.equals(verb.getCategory()) && (catDRadio.isPressed() || catCRadio.isPressed() || catERadio.isPressed())) {
                    if (listener != null) {
                        listener.onCategoryChange(verb, newCategory);
                    }
                }
            });
            // --- Fin gestion catégorie ---


            // --- Gestion du bouton supprimer ---
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteVerb(verb, position);
                }
            });
            // --- Fin gestion supprimer ---
        }
    }
    // --- Fin du ViewHolder ---

    // Méthode pour mettre à jour la liste de verbes (utile après suppression/ajout)
    // Attention: pour de meilleures performances, il existe des méthodes plus avancées
    // comme notifyItemRemoved, notifyItemChanged... mais commençons simple.
    public void updateVerbs(List<IrregularVerb> newVerbList) {
        this.verbList = newVerbList;
        notifyDataSetChanged(); // Redessine toute la liste
    }
}
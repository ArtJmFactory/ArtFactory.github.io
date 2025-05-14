package com.example.verbs1;

// --- Imports nécessaires pour Room ---
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
// --- Fin des imports Room ---

// --- L'import pour Fragment n'a rien à faire ici, on le supprime ---
// import androidx.fragment.app.Fragment; // <-- SUPPRIMER CET IMPORT

// --- Annotations Room ---
@Entity(tableName = "irregular_verbs") // Définit le nom de la table SQL
public class IrregularVerb {

    // --- Clé primaire auto-générée ---
    @PrimaryKey(autoGenerate = true)
    private int id; // Champ ID ajouté

    // --- Autres champs avec @ColumnInfo (Optionnel mais bonne pratique) ---
    @ColumnInfo(name = "french_translation") // Nom de la colonne SQL
    private String french;

    @ColumnInfo(name = "base_form")
    private String baseForm;

    @ColumnInfo(name = "past_simple")
    private String pastSimple;

    @ColumnInfo(name = "past_participle")
    private String pastParticiple;

    @ColumnInfo(name = "category")
    private String category; // "D", "C", "E"
    // --- Fin des champs ---


    // --- Constructeur (utilisé par nous pour créer de nouveaux objets) ---
    // L'ID n'est pas inclus ici car auto-généré par Room lors de l'insertion
    public IrregularVerb(String french, String baseForm, String pastSimple, String pastParticiple, String category) {
        this.french = french;
        this.baseForm = baseForm;
        this.pastSimple = pastSimple;
        this.pastParticiple = pastParticiple;
        this.category = category;
    }

    // --- Getters (Requis par Room) ---
    public int getId() { return id; }
    public String getFrench() { return french; }
    public String getBaseForm() { return baseForm; }
    public String getPastSimple() { return pastSimple; }
    public String getPastParticiple() { return pastParticiple; }
    public String getCategory() { return category; }

    // --- Setters (Requis par Room et pour nos mises à jour) ---
    public void setId(int id) { this.id = id; } // Important pour Room
    public void setFrench(String french) { this.french = french; }
    public void setBaseForm(String baseForm) { this.baseForm = baseForm; }
    public void setPastSimple(String pastSimple) { this.pastSimple = pastSimple; }
    public void setPastParticiple(String pastParticiple) { this.pastParticiple = pastParticiple; }
    public void setCategory(String category) { this.category = category; }

}
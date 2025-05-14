package com.example.verbs1;

import androidx.room.Dao; // Annotation pour marquer comme DAO
import androidx.room.Delete; // Annotation pour la suppression
import androidx.room.Insert; // Annotation pour l'insertion
import androidx.room.OnConflictStrategy; // Pour gérer les conflits d'insertion
import androidx.room.Query; // Annotation pour les requêtes SQL personnalisées
import androidx.room.Update; // Annotation pour la mise à jour

import java.util.List; // Pour utiliser List

// Annotation @Dao pour indiquer à Room que c'est une interface DAO
@Dao
public interface VerbDao {

    // --- Méthodes d'Insertion ---

    // Insère un seul verbe. OnConflictStrategy.REPLACE signifie que si un verbe
    // avec le même ID existe déjà, il sera remplacé (utile si on importe/réimporte).
    // Pour l'auto-génération d'ID, IGNORE est souvent utilisé aussi.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVerb(IrregularVerb verb);

    // Insère une liste de verbes (pratique pour le peuplement initial)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllVerbs(List<IrregularVerb> verbs);


    // --- Méthodes de Mise à jour ---

    // Met à jour un verbe existant (basé sur sa clé primaire 'id')
    @Update
    void updateVerb(IrregularVerb verb);


    // --- Méthodes de Suppression ---

    // Supprime un verbe existant (basé sur sa clé primaire 'id')
    @Delete
    void deleteVerb(IrregularVerb verb);

    // Optionnel : Supprimer tous les verbes (attention !)
    @Query("DELETE FROM irregular_verbs")
    void deleteAllVerbs();


    // --- Méthodes de Lecture (Query) ---

    // Récupère tous les verbes, triés par la forme française
    @Query("SELECT * FROM irregular_verbs ORDER BY french_translation ASC")
    List<IrregularVerb> getAllVerbsSortedByFrench();

    // Récupère tous les verbes d'une catégorie spécifique, triés par français
    @Query("SELECT * FROM irregular_verbs WHERE category = :categoryName ORDER BY french_translation ASC")
    List<IrregularVerb> getVerbsByCategory(String categoryName);

    // Récupère tous les verbes des catégories D et C, triés par français
    @Query("SELECT * FROM irregular_verbs WHERE category = 'D' OR category = 'C' ORDER BY french_translation ASC")
    List<IrregularVerb> getVerbsForIntermediateLevel();

    // Récupère un verbe spécifique par son ID (peut être utile)
    @Query("SELECT * FROM irregular_verbs WHERE id = :verbId LIMIT 1")
    IrregularVerb getVerbById(int verbId);
// --- NOUVELLES MÉTHODES POUR COMPTER ---

    /**
     * Compte le nombre de verbes pour une catégorie donnée.
     * @param categoryName La catégorie ("D", "C", ou "E").
     * @return Le nombre de verbes dans cette catégorie.
     */
    @Query("SELECT COUNT(*) FROM irregular_verbs WHERE category = :categoryName")
    int countVerbsByCategory(String categoryName);

    /**
     * Compte le nombre total de verbes dans la base de données.
     * @return Le nombre total de verbes.
     */
    @Query("SELECT COUNT(*) FROM irregular_verbs")
    int countAllVerbs();

    // --- FIN NOUVELLES MÉTHODES ---


}
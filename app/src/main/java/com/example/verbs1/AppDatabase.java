package com.example.verbs1;

import android.content.Context;
import android.util.Log; // Pour les logs de debug

import androidx.annotation.NonNull; // Pour @NonNull
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase; // Pour le Callback

// Imports pour lire le JSON et exécuter en arrière-plan
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {IrregularVerb.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract VerbDao verbDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "verbs_database";
    private static final int NUMBER_OF_THREADS = 4; // Pour l'ExecutorService

    // ExecutorService pour exécuter les opérations BDD en arrière-plan
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            // Ajout du Callback pour le peuplement initial
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Exécute le peuplement en arrière-plan
                                    databaseWriteExecutor.execute(() -> {
                                        // On passe le contexte applicatif et le DAO
                                        populateInitialData(context.getApplicationContext(), INSTANCE.verbDao());
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Méthode pour peupler la BDD avec les données du JSON
    public static void populateInitialData(Context context, VerbDao dao) {
        try {
            Log.d("AppDatabase", "Populating initial data from JSON...");
            // 1. Lire le fichier JSON depuis les assets
            InputStream is = context.getAssets().open("initial_verbs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            // 2. Parser le JSON
            JSONArray jsonArray = new JSONArray(json);
            List<IrregularVerb> verbsToInsert = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject verbObject = jsonArray.getJSONObject(i);
                // Utilise les noms exacts des clés dans ton fichier JSON
                String french = verbObject.getString("french");
                String base = verbObject.getString("base");
                String past = verbObject.getString("past");
                String participle = verbObject.getString("participle");
                String category = verbObject.getString("category");

                IrregularVerb verb = new IrregularVerb(french, base, past, participle, category);
                verbsToInsert.add(verb);
            }

            // 3. Insérer les verbes dans la BDD via le DAO
            if (!verbsToInsert.isEmpty()) {
                dao.insertAllVerbs(verbsToInsert);
                Log.d("AppDatabase", "Initial verbs inserted: " + verbsToInsert.size());
            } else {
                Log.d("AppDatabase", "No initial verbs found in JSON or JSON parsing failed.");
            }

        } catch (Exception e) {
            // Gérer les erreurs (lecture fichier, parsing JSON)
            Log.e("AppDatabase", "Error populating initial data", e);
        }
    }
}
package com.example.verbs1;

// Imports Android Framework
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
// Pas besoin de Build si on n'utilise plus Html.fromHtml avec les conditions N
// import android.os.Build;
import android.os.Bundle;
// Pas besoin de Html si on n'utilise plus Html.fromHtml
// import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

// Imports AndroidX (Support Library / Jetpack)
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LevelFragment extends Fragment {

    // --- Vues du layout ---
    private RadioGroup levelRadioGroup;
    private EditText userNameEditText;
    private TextView bestScoreLabelTextView;
    private TextView bestScoreValueTextView;
    private TextView bestScoreUserNameTextView;
    private Button customizeButton;
    private Button startTrainingButton;
    private RadioButton beginnerRadioButton;
    private RadioButton intermediateRadioButton;
    private RadioButton expertRadioButton;
    private TextView footerInfoTextView;

    // Helper pour les SharedPreferences
    private SharedPreferencesHelper prefsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new SharedPreferencesHelper(requireContext());

        // Initialisation des vues
        levelRadioGroup = view.findViewById(R.id.radiogroup_level_choice);
        userNameEditText = view.findViewById(R.id.edittext_user_name);
        bestScoreLabelTextView = view.findViewById(R.id.textview_level_best_score_label);
        bestScoreValueTextView = view.findViewById(R.id.textview_level_best_score_value);
        bestScoreUserNameTextView = view.findViewById(R.id.textview_level_best_score_user_name);
        customizeButton = view.findViewById(R.id.button_level_customize);
        startTrainingButton = view.findViewById(R.id.button_level_start_training);
        beginnerRadioButton = view.findViewById(R.id.radiobutton_level_beginner);
        intermediateRadioButton = view.findViewById(R.id.radiobutton_level_intermediate);
        expertRadioButton = view.findViewById(R.id.radiobutton_level_expert);
        footerInfoTextView = view.findViewById(R.id.textview_footer_info);

        levelRadioGroup.check(R.id.radiobutton_level_beginner);

        String savedUserName = prefsHelper.getUserName();
        if (!TextUtils.isEmpty(savedUserName)) {
            userNameEditText.setText(savedUserName);
        }

        updateBestScoreDisplay();
        loadAndDisplayVerbCounts();

        // Configuration du texte du Footer
        setupFooterText();

        // Listeners boutons
        customizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToVerbListScreen();
                }
            }
        });

        startTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEditText.getText().toString().trim();
                int selectedLevelId = levelRadioGroup.getCheckedRadioButtonId();
                String selectedLevelCode = "ALL";

                if (userName.isEmpty()) {
                    userNameEditText.setError("Veuillez entrer votre prénom");
                    return;
                }

                if (selectedLevelId == beginnerRadioButton.getId()) {
                    selectedLevelCode = "D";
                } else if (selectedLevelId == intermediateRadioButton.getId()) {
                    selectedLevelCode = "C+D";
                } else if (selectedLevelId == expertRadioButton.getId()) {
                    selectedLevelCode = "ALL";
                } else {
                    Toast.makeText(requireContext(), "Veuillez choisir un niveau", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefsHelper.saveUserName(userName);

                if (getActivity() instanceof MainActivity) {
                    Bundle args = new Bundle();
                    args.putString("USER_LEVEL_CODE", selectedLevelCode);
                    ((MainActivity) getActivity()).navigateToQuizScreenWithArgs(args);
                }
            }
        });
    }

    private void setupFooterText() {
        if (footerInfoTextView == null) {
            Log.e("LevelFragment", "footerInfoTextView is null in setupFooterText!");
            return;
        }

        String versionName = "";
        try {
            PackageInfo pInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("LevelFragment", "Error getting package info", e);
            versionName = getString(R.string.app_version_placeholder);
        }

        final String linkText = "En savoir plus";
        // Assure-toi que R.string.footer_template est : "Art@Factory / Version %1$s / En savoir plus"
        String fullText = getString(R.string.footer_template, versionName);

        SpannableString spannableString = new SpannableString(fullText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://artjmfactory.github.io/en_savoir_plus.html"));
                try {
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Impossible d'ouvrir le lien.", Toast.LENGTH_SHORT).show();
                    Log.e("LevelFragment", "Error opening URL: " + e.getMessage());
                }
            }
            // Optionnel: pour styliser le lien (couleur, soulignement)
            // @Override
            // public void updateDrawState(@NonNull android.text.TextPaint ds) {
            //     super.updateDrawState(ds);
            //     ds.setUnderlineText(true);
            //     // ds.setColor(ContextCompat.getColor(requireContext(), R.color.your_link_color)); // Nécessiterait ContextCompat
            // }
        };

        int startIndexOfLink = fullText.indexOf(linkText);
        if (startIndexOfLink != -1) {
            int endIndexOfLink = startIndexOfLink + linkText.length();
            spannableString.setSpan(clickableSpan, startIndexOfLink, endIndexOfLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.d("LevelFragment", "ClickableSpan applied manually to '" + linkText + "' at [" + startIndexOfLink + "-" + endIndexOfLink + "]");
        } else {
            Log.e("LevelFragment", "Link text '" + linkText + "' not found in full string: '" + fullText + "'");
        }

        footerInfoTextView.setText(spannableString);
        footerInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
        footerInfoTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private void loadAndDisplayVerbCounts() {
        VerbDao verbDao = AppDatabase.getDatabase(requireContext()).verbDao();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            final int countD = verbDao.countVerbsByCategory("D");
            final int countC = verbDao.countVerbsByCategory("C");
            final int countAll = verbDao.countAllVerbs();
            final int countIntermediate = countD + countC;

            Log.d("LevelFragment", "Counts - D: " + countD + ", C: " + countC + ", All: " + countAll);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String beginnerBaseText = getString(R.string.level_choice_beginner_base);
                    String intermediateBaseText = getString(R.string.level_choice_intermediate_base);
                    String expertBaseText = getString(R.string.level_choice_expert_base);
                    String format = getString(R.string.level_choice_format);

                    if (beginnerRadioButton != null) {
                        beginnerRadioButton.setText(String.format(format, beginnerBaseText, countD));
                    }
                    if (intermediateRadioButton != null) {
                        intermediateRadioButton.setText(String.format(format, intermediateBaseText, countIntermediate));
                    }
                    if (expertRadioButton != null) {
                        expertRadioButton.setText(String.format(format, expertBaseText, countAll));
                    }
                });
            }
        });
    }

    private void updateBestScoreDisplay() {
        if (prefsHelper != null && bestScoreValueTextView != null && bestScoreUserNameTextView != null) {
            int bestScore = prefsHelper.getBestScore();
            String bestScoreUser = prefsHelper.getBestScoreUserName();

            bestScoreValueTextView.setText(String.valueOf(bestScore));

            if (!TextUtils.isEmpty(bestScoreUser)) {
                bestScoreUserNameTextView.setText(" (" + bestScoreUser + ")");
                bestScoreUserNameTextView.setVisibility(View.VISIBLE);
            } else {
                bestScoreUserNameTextView.setText("");
                bestScoreUserNameTextView.setVisibility(View.GONE);
            }
        } else {
            Log.w("LevelFragment", "One of the best score TextViews or prefsHelper is null in updateBestScoreDisplay.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBestScoreDisplay();
        loadAndDisplayVerbCounts();
    }
}
package com.example.quiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class ResultActivity extends AppCompatActivity {

    public static final String DATA = "data";
    private ConstraintLayout main;
    private String results = "", email;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        main = findViewById(R.id.resultMain);

        displayResults();
    }

    private void displayResults() {
        SharedPreferences preferences = getSharedPreferences(DATA, 0);
        Resources resources = getResources();
        TypedArray questions = resources.obtainTypedArray(R.array.questions);

        double total = questions.length();
        double score = 0;

        LinearLayout linearLayout = findViewById(R.id.resultsLayout);

        for (int i = 0; i < total; ++i){
            String answered = preferences.getString(String.format(Locale.getDefault(), "q%d", i+1), "opt0");
            String[] question = resources.getStringArray(questions.getResourceId(i, -1));

            // mitmes küsimus
            TextView title = new TextView(this); // loon textview linear layouti
            title.setPadding(0, 20, 0, 10);
            title.setText(String.format(getString(R.string.questionNumber), i + 1));
            title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            title.setTextSize(18);
            linearLayout.addView(title);

            // küsimus
            TextView txtQuestion = new TextView(this);
            title.setText(question[0]);
            linearLayout.addView(txtQuestion);

            // kasutaja vastus
            TextView txtUserAnswer = new TextView(this);
            int index = Integer.parseInt(answered.substring(3));
            String answeredText;
            if(index == 0){
                answeredText = getString(R.string.dontknow);
            } else {
                answeredText = question[index];
            }
            txtUserAnswer.setText(String.format(getString(R.string.userAnswer), answeredText));
            txtUserAnswer.setTextColor(Color.BLACK);
            linearLayout.addView(txtUserAnswer);

            results += (i+1) + ". " + answeredText + "\n";

            // kas vastati õigesti või valesti, vastuse kontroll
            // õige korral punkt, valel korral õige vastus
            if(Objects.equals(question[5], answered)){
                TextView correct = new TextView(this);
                correct.setText(getString(R.string.correct));
                correct.setTextColor(Color.GREEN);
                linearLayout.addView(correct);
                ++score;
            } else {
                TextView incorrect = new TextView(this);
                index = Integer.parseInt(question[5].substring(3));
                incorrect.setText(String.format(getString(R.string.incorrect), question[index]));
                incorrect.setTextColor(Color.RED);
                linearLayout.addView(incorrect);
            }
        } // for tsükli lõpp

        // teisendan skoori protsentidesse
        score = (score/total)*100;

        TextView txtScore = findViewById(R.id.txtResults);
        txtScore.setText(String.format(getString(R.string.resultsTitle), score));

        // salvestad skoori
        if(score > preferences.getFloat("highscore", 0)){
            TextView highscoreText = new TextView(this);
            highscoreText.setPadding(0, 20, 0, 10);
            highscoreText.setText(getString(R.string.newHighScore));
            highscoreText.setTextColor(Color.MAGENTA);
            highscoreText.setTextSize(24);
            highscoreText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            linearLayout.addView(highscoreText, 0);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("highscore", (float) score);
            editor.apply();
        }
        questions.recycle();
    }

    // back nupu üle kirjutamine, viib tagasi pealehele
    @Override
    public void onBackPressed() {
        finish(); // sulgeb activity asjad, vabastab ressursid
        onHome(null); // viib tagasi
    }

    // viib kasutaja tagasi pealehele
    public void onHome(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    public void onShare(View view) {
        internalSave();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert e-mail:");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                email = input.getText().toString();
                sendEmail(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendEmail (String mail) {
        Uri uri = FileProvider.getUriForFile (ResultActivity.this, "com.example.fileprovider", file);
        Intent emailIntent = new Intent (Intent.ACTION_SEND);
        // The intent does not have a URI, so declare the "text/plain" MIME type
        emailIntent.setType ("message/RFC822");
        emailIntent.putExtra (Intent.EXTRA_EMAIL, new String[]{mail}); // recipients
        emailIntent.putExtra (Intent.EXTRA_SUBJECT, "Android quiz answers");
        emailIntent.putExtra (Intent.EXTRA_TEXT, "In the attachment you'll find the answers you gave in the Android quiz.\n\nWith regards,\nAndroid quizmaster");
        emailIntent.putExtra (Intent.EXTRA_STREAM, uri);
        // You can also attach multiple items by passing an ArrayList of Uris

        try {
            startActivity (Intent.createChooser (emailIntent, "Send mail..."));
            finish ();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText (ResultActivity.this, "No email client installed.", Toast.LENGTH_SHORT).show ();
        }
    }

    private void internalSave () {
        String FILE_NAME = "quiz_answers.txt";

        try {
            file = new File (getFilesDir (), FILE_NAME);
            FileWriter fileWriter = new FileWriter (file, false);
            fileWriter.append (results);
            fileWriter.flush ();
            fileWriter.close ();
        } catch (IOException ioException) {
            ioException.printStackTrace ();
        }
    }
}


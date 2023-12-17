package com.example.quiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String DATA = "data";
    private TextView highscore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        highscore = findViewById(R.id.txtHighscore);
        mainScore();
    }

    // Method that shows the score
    private void mainScore(){
        SharedPreferences preferences = getSharedPreferences(DATA, 0);
        double hscore = preferences.getFloat("highscore", 0);
        String txtScore = String.format(getString(R.string.highscore), hscore);
        highscore.setText(txtScore);
    }

    // Implement menu in the activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Käivitab quizi, bundle saadab anded järgmisesse activitysse -> küsimuse number
    public void onStart(View view) {
        Bundle data = new Bundle();
        data.putInt("questionNumber", 1);
        // From getApplicationContext() to QuestionActivity.class
        Intent quiz = new Intent(getApplicationContext(), QuestionActivity.class);
        quiz.putExtras(data);
        startActivity(quiz);


    }

    public void onResetScore(MenuItem item) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.resetScore))
                .setMessage(getString(R.string.resetHighMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = getSharedPreferences(DATA, 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putFloat("highscore", 0);
                        editor.apply();
                        mainScore();
                    }
                })
                .setNegativeButton(android.R.string.no, null) // null = no event
                .show();
    }

    public void onResetAnswers(MenuItem item) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.resetScore))
                .setMessage(getString(R.string.resetHighMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = getSharedPreferences(DATA, 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        // Saame küsimused string.xml failist, kus need on massiivina
                        TypedArray typedArray = getResources().obtainTypedArray(R.array.questions);
                        int total = typedArray.length();
                        typedArray.recycle();
                        for (int i = 1; i <= total; ++i){
                            editor.remove(String.format(Locale.getDefault(), "q%d", i));
                        }
                        editor.apply();

                    }
                })
                .setNegativeButton(android.R.string.no, null) // null = no event
                .show();
    }

    // kirjutame üle back nupu tegevuse, et liikudes main lehele tagasi ei tuleks veidraid tulemusi
    @Override
    public void onBackPressed() {
        // saab aru et tegu on koduga ja ei tee mingit pulli
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}
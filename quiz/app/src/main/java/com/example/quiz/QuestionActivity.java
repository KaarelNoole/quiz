package com.example.quiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionActivity extends AppCompatActivity {

    // Saame andmed
    public static final String DATA = "data";
    int questionNumber;
    // First in last out
    Stack<Integer> previousQuestions = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // Kasutame bundle'it, et saada info eelmisest activity'st
        Bundle data = this.getIntent().getExtras();
        if(data != null){
            displayQuestions(data.getInt("questionNumber"));
        }
        // See lisab back nupu ülesse menüüribale
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    // Sulgeb kõik eelnevad tegevused enne activity vahetust
    @Override
    public boolean onSupportNavigateUp() {
        // Lõpetab üldised tegevused
        finish();
        return true;
    }

    // Meetod kuvab küsimused, aluseks on küsimuste number -> questionNumber
    private void displayQuestions(int number) {
        questionNumber = number;
        Resources resources = getResources();
        TypedArray questions = resources.obtainTypedArray(R.array.questions);
        String[] question = resources.getStringArray(questions.getResourceId(number-1, -1));

        // kui kasutaja jõuab viimase küsimuseni, muudame Next nupu teksti submit'iks
        if (number == questions.length()){
            Button next = findViewById(R.id.btnNext);
            next.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    submit();
                }
            });
            next.setText(getString(R.string.submit)); // muudan nupu teksti
        } else {
            Button next = findViewById(R.id.btnNext);
            next.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    onNext(view);
                }
            });
            next.setText(getString(R.string.nextquestion));
        }

        // kui esimene küsimus, siis previous nupp eie tööta
        if(number == 1){
            findViewById(R.id.btnPrevious).setEnabled(false);
        } else {
            findViewById(R.id.btnPrevious).setEnabled(true);
        }

        // küsimuse numbri kuvamine
        TextView txtQuestionNumber = findViewById(R.id.txtQuestionNumber);
        txtQuestionNumber.setText(String.format(getString(R.string.questionNumberDouble), number, questions.length()));
        ((TextView)findViewById(R.id.txtQuestion)).setText(question[0]);

        // kuvan vastuse variante Radiobuttonites
        for(int i = 1; i <= 4; ++i){
            // ütlen et default id mis olen radiobuttnites kasutanud (opt0, 1, 2, etc.), asendab numberi peae opti
            String optId = String.format(Locale.getDefault(), "opt%d", i);
            // leiab rbtnid üles, võta ressursdest ja kasuta id'd,
            RadioButton rbtn = findViewById(getResources().getIdentifier(optId, "id", this.getPackageName()));
            rbtn.setText(question[i]);
            rbtn.setChecked(false);
        }

        // kontrollin kas on eelnevalt salvestatud vastuseid
        SharedPreferences preferences = getSharedPreferences(DATA, 0); // salvestatud andmed
        RadioButton rbtn = findViewById(getResources().getIdentifier(preferences.getString(String.format(Locale.getDefault(), "q%d", number),"opt0"), "id", this.getPackageName()));
        rbtn.setChecked(true);
        questions.recycle(); // typedarray jaoks, kui enam ei kasuta siis (vabastab ressursi kui tegevus on lõpetatud)
    }

    // esitad vastused hindamiseks
    private void submit() {
        saveAnswers(questionNumber);
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.submitTitle))
                .setMessage(getString(R.string.submitMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // liigun uude activity'sse
                        startActivity(new Intent(getApplicationContext(), ResultActivity.class));
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    // salvestame valitud vastused küsimuse numbri põhjal
    private void saveAnswers(int number) {
        SharedPreferences preferences = getSharedPreferences(DATA, 0);
        SharedPreferences.Editor editor = preferences.edit();
        RadioGroup radioGroup = findViewById(R.id.options);
        editor.putString(String.format(Locale.getDefault(), "q%d", number), getResources().getResourceEntryName(radioGroup.getCheckedRadioButtonId()));
        editor.apply();
    }

    public void onPrevious(View view) {
        previousQuestions.push(questionNumber);
        saveAnswers(questionNumber);
        displayQuestions(questionNumber-1);
    }

    public void onNext(View view) {
        previousQuestions.push(questionNumber);
        saveAnswers(questionNumber);
        displayQuestions(questionNumber+1);
    }

    // loob menüü
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.question_menu, menu);
        return true;
    }

    // Menüü nupu tegevus
    public void onMenuSubmit(MenuItem item) {
        submit();
    }

    @Override
    public void onBackPressed() {
        if (!previousQuestions.empty()){
            int previous = previousQuestions.pop();
            saveAnswers(questionNumber);
            displayQuestions(previous);
        }
    }
}
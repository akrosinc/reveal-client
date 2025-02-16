package org.smartregister.reveal.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.smartregister.reveal.R;
import org.smartregister.view.activity.MultiLanguageActivity;

/**
 * Created by Richard Kareko on 9/22/20.
 */

public class CaptureIndexCaseActivity extends MultiLanguageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_case_missing_capture);  // Set the layout for this activity

        // Get references to the EditText fields and Save button
        EditText editText1 = findViewById(R.id.compoundText);
        EditText editText2 = findViewById(R.id.householdText);
        Button saveButton = findViewById(R.id.saveButton);

        // Set a listener for the Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text entered in the EditText fields
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();

                // Perform any action with the input text, such as saving or displaying it
                // For now, just display the input in a Toast
                Toast.makeText(CaptureIndexCaseActivity.this, "Saved: " + text1 + " and " + text2, Toast.LENGTH_SHORT).show();

                // Optionally, close the activity after saving
                finish();  // Close the activity
            }
        });
    }

}

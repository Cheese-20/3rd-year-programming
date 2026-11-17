package com.example.as1t2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.as1t2.model.Token;

import java.util.ArrayList;

public class PlayerNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceStance){
        super.onCreate(savedInstanceStance);
        setContentView(R.layout.activity_names);

        int pCount = getIntent().getIntExtra("PLAYER_COUNT", 2);
        Button btnStart = findViewById(R.id.btnNameStart);

        Spinner[] spinners = new Spinner[4];
        spinners[0] = findViewById(R.id.spnP1Color);
        spinners[1] = findViewById(R.id.spnP2Color);
        spinners[2] = findViewById(R.id.spnP3Color);
        spinners[3] = findViewById(R.id.spnP4Color);

        String[] colors = {"RED", "BLUE", "GREEN", "PURPLE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 0; i < 4; i++) {
            spinners[i].setAdapter(adapter);
            spinners[i].setSelection(i % 4);
        }

        if(pCount > 2){
            findViewById(R.id.p3Container).setVisibility(View.VISIBLE);
        }
        if(pCount > 3){
            findViewById(R.id.p4Container).setVisibility(View.VISIBLE);
        }

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ArrayList<String> pNames = new ArrayList<>();
                ArrayList<String> pColors = new ArrayList<>();
                
                pNames.add(getTextFromEditText(R.id.edtP1));
                pColors.add(spinners[0].getSelectedItem().toString());
                
                pNames.add(getTextFromEditText(R.id.edtP2));
                pColors.add(spinners[1].getSelectedItem().toString());

                if(pCount > 2){
                    pNames.add(getTextFromEditText(R.id.player3Name));
                    pColors.add(spinners[2].getSelectedItem().toString());
                }
                if(pCount > 3){
                    pNames.add(getTextFromEditText(R.id.player4Name));
                    pColors.add(spinners[3].getSelectedItem().toString());
                }

                boolean validNames = true;
                for(String name : pNames){
                    if(name.trim().isEmpty()){
                        validNames = false;
                        break;
                    }
                }

                if(!validNames){
                    Toast.makeText(PlayerNameActivity.this,
                            "Please enter all the player names",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(PlayerNameActivity.this, MainActivity.class);
                intent.putStringArrayListExtra("PLAYER_NAMES", pNames);
                intent.putStringArrayListExtra("PLAYER_COLORS", pColors);
                startActivity(intent);
            }
        });
    }

    private String getTextFromEditText(int id){
        EditText editText = findViewById(id);
        return editText.getText().toString().trim();
    }
}

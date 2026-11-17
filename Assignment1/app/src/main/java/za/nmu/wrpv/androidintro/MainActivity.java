package za.nmu.wrpv.androidintro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSayHello = findViewById(R.id.btnSay);
        btnSayHello.setOnClickListener(view -> {
            EditText edtName = findViewById(R.id.txtName);
            String name =  edtName.getText().toString();

            Toast.makeText(this, "Hello, " + name + ", welcome to the app", Toast.LENGTH_LONG).show();
        });
    }

    @SuppressLint("SetTextI18n")
    public void onHelloClicked(View view) {
       TextView txtMsg= findViewById(R.id.txtOut);
       txtMsg.setText("Hello twin!");

    }

    //intent to do but android will handle the intent/dialing of the number
    public void onDialClicked(View view) {
        Uri helplineNum = Uri.parse("tel:012345678");
        Intent dial = new Intent(Intent.ACTION_DIAL, helplineNum);
        startActivity(dial);
    }

    //intention to show a specific activity
    public void onShowClicked(View view) {
        Intent showIntent = new Intent(this, AnotherActivity.class);
        startActivity(showIntent);
    }
}
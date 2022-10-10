package whatsappclone.proyecto_javier_juan_uceda.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnDriver, btnCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
    }

    private void setUI() {
        btnCustomer = findViewById(R.id.customer);
        btnDriver = findViewById(R.id.driver);

        btnCustomer.setOnClickListener(this);
        btnDriver.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.customer:
                goToScreen(MainActivity.this,CustomerLoginActivity.class);
                break;
            case R.id.driver:
                goToScreen(MainActivity.this,DriverLoginActivity.class);
                break;
        }
    }

    private void goToScreen(android.content.Context activity,  Class<?> destinatation) {
        Intent intent = new Intent(activity, destinatation);
        startActivity(intent);
        finish();
    }
}
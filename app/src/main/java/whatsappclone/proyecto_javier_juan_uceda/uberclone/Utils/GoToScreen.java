package whatsappclone.proyecto_javier_juan_uceda.uberclone.Utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

public abstract class GoToScreen extends AppCompatActivity{
    public void goToScreen(android.content.Context activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        startActivity(intent);
        finish();
    }
}

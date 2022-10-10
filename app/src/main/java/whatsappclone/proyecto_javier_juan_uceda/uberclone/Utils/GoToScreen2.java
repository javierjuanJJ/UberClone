package whatsappclone.proyecto_javier_juan_uceda.uberclone.Utils;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

public abstract class GoToScreen2 extends FragmentActivity {
    public void goToScreen(android.content.Context activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        startActivity(intent);
        finish();
    }
}

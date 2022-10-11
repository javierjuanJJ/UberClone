package whatsappclone.proyecto_javier_juan_uceda.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CostumerSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnConfirm, btnBack;
    private EditText etName, etPhone;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerReference;

    private String userId, mName, mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costumer_settings);
        setUI();
    }

    private void setUI() {
        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);

        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCustomerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);

        getUserInfo();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnConfirm:
                saveUserInformation();
                break;
        }
    }

    private void getUserInfo(){
        mCustomerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("firebase", String.valueOf(snapshot.exists()));
                Log.i("firebase", String.valueOf(snapshot.getChildrenCount() > 0));
                if (snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String,Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        etName.setText(mName);
                    }

                    if (map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        etPhone.setText(mPhone);
                    }
                    Log.i("firebase", "Done");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveUserInformation() {
        mName = etName.getText().toString();
        mPhone = etPhone.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);

        mCustomerReference.updateChildren(userInfo);

        finish();

    }
}
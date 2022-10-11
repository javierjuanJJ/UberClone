package whatsappclone.proyecto_javier_juan_uceda.uberclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CostumerSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnConfirm, btnBack;
    private EditText etName, etPhone;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerReference;

    private String userId, mName, mPhone, mProfilePicture;

    private ImageView ivProfilePhoto;
    private Uri resultUri;

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

        ivProfilePhoto = findViewById(R.id.profilePhoto);

        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        ivProfilePhoto.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mCustomerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);

        getUserInfo();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            resultUri = uri;
            ivProfilePhoto.setImageURI(resultUri);
        }
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
            case R.id.profilePhoto:
                Intent image = new Intent(Intent.ACTION_PICK);
                image.setType("image/*");
                startActivityForResult(image,1);
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

                    if (map.get("profileImageUrl") != null){
                        mProfilePicture = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfilePicture).apply(new RequestOptions().override(10, 10)).into(ivProfilePhoto);

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

        if (resultUri != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_pictures").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),resultUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());

                    Uri downloadUrl = urlTask.getResult();
                    HashMap map = new HashMap();
                    map.put("profileImageUrl",downloadUrl.toString());
                    mCustomerReference.updateChildren(map);
                    finish();
                }
            });


        }
        else {
            finish();
        }



    }
}
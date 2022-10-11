package whatsappclone.proyecto_javier_juan_uceda.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import whatsappclone.proyecto_javier_juan_uceda.uberclone.Utils.GoToScreen2;
import whatsappclone.proyecto_javier_juan_uceda.uberclone.databinding.ActivityDriverMapBinding;

public class DriverMapActivity extends GoToScreen2 implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityDriverMapBinding binding;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Button btnLogout;
    private String customerId = "";
    private boolean isLoggingOut = false;
    private LinearLayout customerLinearLayout;
    private TextView customerName, customerPhone;
    private ImageView customerProfilePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUI();
    }

    private void setUI() {
        isLoggingOut = true;

        customerLinearLayout = findViewById(R.id.customerLinearLayout);
        customerName = findViewById(R.id.customerName);
        customerPhone = findViewById(R.id.customerPhone);
        customerProfilePicture = findViewById(R.id.customerProfilePicture);

        btnLogout = findViewById(R.id.logoout);
        btnLogout.setOnClickListener(this);

        getAssignedCustomer();
    }

    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(driverId).child("customerRiderId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    customerId = snapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerInfo();

                }
                else{
                    customerId = "";
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }
                    if (assignedCustomerPickupLocationRef != null && assignedCustomerPickupLocationRefListener != null) {
                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }
                    customerLinearLayout.setVisibility(View.GONE);
                    customerName.setText("");
                    customerPhone.setText("");
                    customerProfilePicture.setImageResource(R.mipmap.ic_launcher);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerInfo() {

        customerLinearLayout.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mCustomerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("firebase", String.valueOf(snapshot.exists()));
                Log.i("firebase", String.valueOf(snapshot.getChildrenCount() > 0));
                if (snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String,Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null){
                        customerName.setText(map.get("name").toString());
                    }

                    if (map.get("phone") != null){
                        customerPhone.setText(map.get("phone").toString());
                    }

                    if (map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).apply(new RequestOptions().override(10, 10)).into(customerProfilePicture);

                    }
                    Log.i("firebase", "Done");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference("customerRequest").child(driverId).child("1");

        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> map = (List<Object>) snapshot.getValue();


                    double locationLat = 0.0;
                    double locationLong = 0.0;

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLong = new LatLng(locationLat, locationLong);

                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)).position(driverLatLong).title(getString(R.string.yourDrive)));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        buildGoogleApiClient();

        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getApplicationContext() != null) {
            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);

                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                GeoFire geoFireWorking = new GeoFire(refWorking);

                switch (customerId){
                    case "":
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;
                    default:
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;
                }
            }

        }


    }

    private void disconnect(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
            GeoFire geoFire = new GeoFire(ref);

            geoFire.removeLocation(userId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isLoggingOut) {
            disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, DriverMapActivity.this);

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
//
//    public void goToScreen(android.content.Context activity, Class<?> destination) {
//        Intent intent = new Intent(activity, destination);
//        startActivity(intent);
//        finish();
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.logoout:
                FirebaseAuth.getInstance().signOut();
                disconnect();
                goToScreen(DriverMapActivity.this, MainActivity.class);
                break;
        }
    }
}
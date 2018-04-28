package com.aly.bikenow;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import java.util.HashMap;
import java.util.List;


public class Customer extends  AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleApiClient;
    Location mlastlocation;
    LocationRequest mlocatonRequest;
    String userId;
    Button pick;
    private LatLng pickuprequet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pick = findViewById(R.id.button3);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildgoogleapiclient();
        mMap.setMyLocationEnabled(true);

    }
    protected synchronized void buildgoogleapiclient(){
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));





    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocatonRequest = new LocationRequest();
        mlocatonRequest.setInterval(1000);
        mlocatonRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocatonRequest.setFastestInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocatonRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void out(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent2 = new Intent(this, MainActivity.class);
        startActivity(intent2);
        finish();
    }
    private Boolean requestBol=false;
    private Marker picupMarker;
    public void request(View view) {
        if (requestBol)
        {
            requestBol=false;
            geoQuery.removeAllListeners();
            driverlocatioref.removeEventListener(driverLocationListner);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequests");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId, new
                    GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            //Do some stuff if you want to
                        }
                    });

            if(driverFound!= null){
                DatabaseReference drivred =FirebaseDatabase.getInstance().getReference().child("drivers").child(driverfoundID);
                drivred.setValue(true);
                driverfoundID = null;
            }
            driverFound=false;
            r=1;
            if(picupMarker!=null){
                picupMarker.remove();

            }
            pick.setText("Request");

        }
        else {
            requestBol=true;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequests");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId, new GeoLocation(mlastlocation.getLatitude(), mlastlocation.getLongitude()), new
                    GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            //Do some stuff if you want to
                        }
                    });
            pickuprequet = new LatLng(mlastlocation.getLatitude(), mlastlocation.getLongitude());
            picupMarker=mMap.addMarker(new MarkerOptions().position(pickuprequet).title("Bike Me Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.biker)));
            pick.setText("finding Bike");

            getClosestDriver();
        }
    }

    private int r=1;
    private Boolean driverFound=false;
    private String driverfoundID;
    GeoQuery geoQuery;
    private void getClosestDriver(){


        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
         geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuprequet.latitude,pickuprequet.longitude),r);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound&&requestBol) {
                    driverFound = true;
                    driverfoundID=key;
                    System.out.println(key);

                    DatabaseReference drivred =FirebaseDatabase.getInstance().getReference().child("drivers").child(driverfoundID);
                    String customerID= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map= new HashMap();
                    map.put("customerRideID",customerID);
                    drivred.updateChildren(map);

                    getDriverLocation();
                    pick.setText("get driver location..");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    r++;
                    System.out.println(r);
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private Marker driverMarker;
    DatabaseReference driverlocatioref;
    private ValueEventListener driverLocationListner;
    private void getDriverLocation(){
         driverlocatioref =FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverfoundID).child("l");
        driverLocationListner= driverlocatioref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&requestBol){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng =0;
                    pick.setText("Driver found");

                    if(map.get(0)!=null){
                        locationlat=Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1)!=null){
                        locationlng=Double.parseDouble(map.get(1).toString());

                    }

                    LatLng driverlatlng= new LatLng(locationlat,locationlng);
                    if(driverMarker !=null){
                        driverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickuprequet.latitude);
                    loc1.setLatitude(pickuprequet.longitude);
                    Location loc2 = new Location("");
                    loc2.setLatitude(driverlatlng.latitude);
                    loc2.setLatitude(driverlatlng.longitude);
                    float distance = loc1.distanceTo(loc2);
                    pick.setText("Driver "+String.valueOf(distance) +" away");
                    driverMarker= mMap.addMarker(new MarkerOptions().position(driverlatlng).title("your Bike").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike)));


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}





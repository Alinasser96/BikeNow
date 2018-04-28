package com.aly.bikenow;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class Driver extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleApiClient;
    Location mlastlocation;
    LocationRequest mlocatonRequest;
    String userId,customerID="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
         userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

         getAssignedCustomer();

    }
    private void getAssignedCustomer(){
        String driverID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigned =FirebaseDatabase.getInstance().getReference().child("drivers").child(driverID).child("customerRideID");
        assigned.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                            customerID=dataSnapshot.getValue().toString();
                            getAssignedCustomerPickUpLocation();


                }
                else{
                    customerID="";
                    if(pickupmarker!=null){
                    pickupmarker.remove();}
                    if(assignedvaluelitner!=null){
                    assignedPickUpRef.removeEventListener(assignedvaluelitner);
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private Marker pickupmarker;
    DatabaseReference assignedPickUpRef;
    private ValueEventListener assignedvaluelitner;
    private void getAssignedCustomerPickUpLocation(){
         assignedPickUpRef =FirebaseDatabase.getInstance().getReference().child("customerRequests").child(customerID).child("l");
        assignedvaluelitner=assignedPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& !customerID.equals("")) {
                    List<Object> map = (List< Object>) dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng =0;


                    if(map.get(0)!=null){
                        locationlat=Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1)!=null){
                        locationlng=Double.parseDouble(map.get(1).toString());

                    }

                    LatLng driver= new LatLng(locationlat,locationlng);
                    pickupmarker= mMap.addMarker(new MarkerOptions().position(driver).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.biker)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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



        //database

        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driverWorking");

        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireWorking = new GeoFire(refWorking);

        switch (customerID){
            case "":
                geoFireWorking.removeLocation(userId,new
                        GeoFire.CompletionListener(){
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                //Do some stuff if you want to
                            }
                        });
                geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(), location.getLongitude()),new
                        GeoFire.CompletionListener(){
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                //Do some stuff if you want to
                            }
                        });


                break;
            default:
                geoFireAvailable.removeLocation(userId,new
                        GeoFire.CompletionListener(){
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                //Do some stuff if you want to
                            }
                        });
                geoFireWorking.setLocation(userId,new GeoLocation(location.getLatitude(), location.getLongitude()),new
                        GeoFire.CompletionListener(){
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                //Do some stuff if you want to
                            }
                        });
                break;

        }



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
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId,new
                GeoFire.CompletionListener(){
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Do some stuff if you want to
                    }
                });
    }

    public void out(View view) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId,new
                GeoFire.CompletionListener(){
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Do some stuff if you want to
                    }
                });
        FirebaseAuth.getInstance().signOut();
        Intent intent2 = new Intent(this, MainActivity.class);
        startActivity(intent2);
        finish();
    }


}



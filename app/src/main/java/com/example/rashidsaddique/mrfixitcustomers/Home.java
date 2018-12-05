package com.example.rashidsaddique.mrfixitcustomers;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.rashidsaddique.mrfixitcustomers.Common.Common;
import com.example.rashidsaddique.mrfixitcustomers.Helper.CustomerInfoWindow;
import com.example.rashidsaddique.mrfixitcustomers.Model.Customers;
import com.example.rashidsaddique.mrfixitcustomers.Model.DataMessage;
import com.example.rashidsaddique.mrfixitcustomers.Model.FCMResponse;
import com.example.rashidsaddique.mrfixitcustomers.Model.Token;
import com.example.rashidsaddique.mrfixitcustomers.Remote.IFCMService;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    SupportMapFragment mapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //Location

    private GoogleMap mMap;


    //Play Services

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mUserMarker, markerWorkLocation;

    //BottomSheet
    ImageView imgExpandable;
    BottomSheetCustomerFragrment mBottomSheet;
    Button btnWorkRequest;


    int radius = 1; //1km
    int distance = 1;
    private static final int LIMIT = 3;

    //Send Alert
    IFCMService mService;

    //Presence System
    DatabaseReference employeesAvailable;
    PlaceAutocompleteFragment place_location, place_workLoation;
    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceWorkLocation;

    //New Update
    CircleImageView imageAvtar;
    TextView txtCustomerName, txtStars;

    //Declare firestorage
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMServices();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //init staorage

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //add findview for image etc..
        View navigationHeaderView = navigationView.getHeaderView(0);
        txtCustomerName = navigationHeaderView.findViewById(R.id.txtCustomerName);
        txtCustomerName.setText(String.format("%s", Common.currentUser.getName()));
        txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s", Common.currentUser.getRates()));
        imageAvtar = navigationHeaderView.findViewById(R.id.imgAvatar);


        //Load Avatar

        if (Common.currentUser.getAvtarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvtarUrl())) {
            Picasso.with(this).load(Common.currentUser.getAvtarUrl()).into(imageAvtar);
        }

        //Maps

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //init view
        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);

        btnWorkRequest = (Button) findViewById(R.id.btnWorkRequest);
        btnWorkRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Common.isEmployeeFound) {
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            requestWorkHere(account.getId());
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                } else sendRequestToEmployee(Common.employeeId);

            }
        });

        place_workLoation = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_workLocation);
        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setTypeFilter(3).build();

        //Event
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();
                mMap.clear();

                //Add marker  at new location
                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.defaultMarker()).title("Customer Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

            }

            @Override
            public void onError(Status status) {

            }
        });
        place_workLoation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceWorkLocation = place.getAddress().toString();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                //Show Information in bottom
                BottomSheetCustomerFragrment mBottomSheet = BottomSheetCustomerFragrment.newInstance(mPlaceLocation, mPlaceWorkLocation, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }

            @Override
            public void onError(Status status) {

            }
        });
//        ref = FirebaseDatabase.getInstance().getReference(Common.customer_location_tbl);
//        geoFire = new GeoFire(ref);

        setUpLocation();

        updateFirebaseToken();


    }

    private void updateFirebaseToken() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference tokens = db.getReference(Common.token_tbl);

                Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId());
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    private void sendRequestToEmployee(String employeeId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(employeeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token token = postSnapShot.getValue(Token.class); //Get Token object from database with key

                    //Make Raw playload and convert latlng to json
                    //String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    String customerToken = FirebaseInstanceId.getInstance().getToken();
//                    Notification data = new Notification(customerToken, json_lat_lng); // send it to employee app
//                    Sender content = new Sender(token.getToken(), data); //Send this data to token

                    Map<String,String> content = new HashMap<>();
                    content.put("customer",customerToken);
                    content.put("lat",String.valueOf(Common.mLastLocation.getLatitude()));
                    content.put("lng",String.valueOf(Common.mLastLocation.getLongitude()));
                    DataMessage dataMessage = new DataMessage(token.getToken(),content);

                    mService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1)
                                Toast.makeText(Home.this, "Request sent", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(Home.this, "Failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("Fix_it_ERROR", t.getMessage());

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestWorkHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.work_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

        if (mUserMarker.isVisible()) mUserMarker.remove();

        //Add new marker

        mUserMarker = mMap.addMarker(new MarkerOptions().title("Work Here").snippet("").position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();

        btnWorkRequest.setText("Getting Employee For You...");

        findEmployee();

    }

    private void findEmployee() {
        DatabaseReference employees = FirebaseDatabase.getInstance().getReference(Common.employees_location_tbl);
        GeoFire gfEmployees = new GeoFire(employees);
        GeoQuery geoQuery = gfEmployees.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found

                if (!Common.isEmployeeFound) {
                    Common.isEmployeeFound = true;
                    Common.employeeId = key;
                    btnWorkRequest.setText("CALL EMPLOYEE");
                    //Toast.makeText(Home.this, "" + key, Toast.LENGTH_SHORT).show();
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

                //if employee still not found increase distance
                if (!Common.isEmployeeFound && radius < LIMIT) {
                    radius++;
                    findEmployee();
                } else {
                    if (!Common.isEmployeeFound) {
                        Toast.makeText(Home.this, "No Employee Available Near Your Location", Toast.LENGTH_SHORT).show();
                        btnWorkRequest.setText("SEND REQUEST");
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (buildLocationCallBack();
//                        createLocationRequest();
//                        displayLocation(); }
//
//                }
//                break;
//        }


    private void setUpLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION }, MY_PERMISSION_REQUEST_CODE);
        } else {
                buildLocationCallBack();
                createLocationRequest();
                displayLocation();
            }
        }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1); //last location
                displayLocation();

            }
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {


                    //Create LatLng from mLastLocation and this is center point
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                    //distance in metters
                    //heading 0 is north side, 90 is east , 180 is south and 270 is west
                    LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);


                    LatLngBounds bounds = LatLngBounds.builder().include(northSide).include(southSide).build();

                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);

                    place_workLoation.setBoundsBias(bounds);
                    place_workLoation.setFilter(typeFilter);


                    //Presence System
                    employeesAvailable = FirebaseDatabase.getInstance().getReference(Common.employees_location_tbl);
                    employeesAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //change in employee list reloaad all
                            loadAllAvailableEmployees(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();


                    //Update to FireBase

                    // geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
//                @Override
//                public void onComplete(String key, DatabaseError error) {

                    loadAllAvailableEmployees(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));

                    Log.d("Mr_Fix_It", String.format("Your location was changed: @f/@f", latitude, longitude));

//        }
//        });
                } else Log.d("Mr_Fix_It", "Cannot get your location");

            }
        });


    }

    private void loadAllAvailableEmployees(final LatLng location) {

        //Add marker
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title(("You")));
        //Move Camera to This position

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        DatabaseReference employeeLocation = FirebaseDatabase.getInstance().getReference(Common.employees_location_tbl);
        GeoFire gf = new GeoFire(employeeLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                //Use key to get email from database table user

                FirebaseDatabase.getInstance().getReference(Common.employees_tbl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Customers customers = dataSnapshot.getValue(Customers.class);

                        //Add Employee on Map

                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                .flat(true)
                                .title(customers.getName())
                                .snippet("Employee ID: " + dataSnapshot.getKey())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) // Employee Available on 3km distance
                {
                    distance++;
                    loadAllAvailableEmployees(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signout) {

            signOut();

        } else if (id == R.id.nav_UpdateInformation) {

            showUpdateInformationDialog();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUpdateInformationDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
        dialog.setTitle("Update Information");
        dialog.setMessage("Please Use Email to Register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View update_info_layout = inflater.inflate(R.layout.layout_update_information, null);

        final MaterialEditText edtName = update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = update_info_layout.findViewById(R.id.edtPhone);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);

        dialog.setView(update_info_layout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageAndUpload();
            }
        });
        dialog.setView(update_info_layout);

        //set Button
        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                        waitingDialog.show();

                        String name = edtName.getText().toString();
                        String phone = edtPhone.getText().toString();

                        Map<String, Object> update = new HashMap<>();
                        if (!TextUtils.isEmpty(name)) update.put("name", name);
                        if (!TextUtils.isEmpty(phone)) update.put("phone", phone);

                        //update
                        DatabaseReference customerInformation = FirebaseDatabase.getInstance().getReference(Common.customer_tbl);

                        customerInformation.child(account.getId()).updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();

                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Information Updated", Toast.LENGTH_SHORT).show();
                                else Toast.makeText(Home.this, "Information wasn't Update", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }

    private void chooseImageAndUpload() {
        //Strat intent to chose image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();


                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        //save Url to user info table
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("avatarUrl", uri.toString());

                                        //Update

                                        DatabaseReference customerInformation = FirebaseDatabase.getInstance().getReference(Common.customer_tbl);

                                        customerInformation.child(account.getId()).updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) Toast.makeText(Home.this, "Image was Uploaded", Toast.LENGTH_SHORT).show();
                                                else Toast.makeText(Home.this, "Image wasn't Update", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });

                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded" + progress + "@");
                    }
                });
            }
        }
    }

    private void signOut() {
        AlertDialog.Builder builder;

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to logout ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AccountKit.logOut();
                        Intent intent = new Intent(Home.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map));
            if (!isSuccess) Log.e("ERROR", "Map style load failed..");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomerInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerWorkLocation != null) markerWorkLocation.remove();
                markerWorkLocation = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(latLng).title("Work Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                //Show bottom Sheet
                BottomSheetCustomerFragrment mBottomSheet = BottomSheetCustomerFragrment.newInstance(String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                        String.format("%f , %f", latLng.latitude, latLng.longitude), true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());


            }
        });

        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

    }


}

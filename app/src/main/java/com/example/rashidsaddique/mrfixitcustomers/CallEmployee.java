package com.example.rashidsaddique.mrfixitcustomers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rashidsaddique.mrfixitcustomers.Common.Common;
import com.example.rashidsaddique.mrfixitcustomers.Model.Customers;
import com.example.rashidsaddique.mrfixitcustomers.Remote.IFCMService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallEmployee extends AppCompatActivity {

    CircleImageView avatar_image;
    TextView txt_name, txt_phone, txt_rate;
    Button btn_call_employee, btn_call_employee_phone;

    String employeeId;
    Location mLastLocation;

    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_employee);

        mService = Common.getFCMServices();

        avatar_image = (CircleImageView) findViewById(R.id.avatar_image);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_rate = (TextView) findViewById(R.id.txt_rate);

        btn_call_employee = (Button) findViewById(R.id.btn_call_employee);
        btn_call_employee_phone = (Button) findViewById(R.id.btn_call_employee_Phone);

        btn_call_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (employeeId != null && !employeeId.isEmpty())
                    Common.sendRequestToEmployee(employeeId, mService, getBaseContext(), mLastLocation);

            }
        });

        btn_call_employee_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + txt_phone.getText().toString()));
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });
        //get Intent
        if (getIntent() != null)
        {
            employeeId = getIntent().getStringExtra("employeeId");
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);


            mLoadEmployeeInfo(employeeId);

        }

    }

    private void mLoadEmployeeInfo(final String employeeId) {
        FirebaseDatabase.getInstance().getReference(Common.employees_tbl)
                .child(employeeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Customers employeeUser = dataSnapshot.getValue(Customers.class);

                        if(!employeeUser.getAvtarUrl().isEmpty())
                        {
                            Picasso.with(getBaseContext())
                                    .load(employeeUser.getAvtarUrl())
                                    .into(avatar_image);
                        }
                        txt_name.setText(employeeUser.getName());
                        txt_phone.setText(employeeUser.getPhone());
                        txt_rate.setText(employeeUser.getRates());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}

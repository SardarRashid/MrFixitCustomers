package com.example.rashidsaddique.mrfixitcustomers;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.rashidsaddique.mrfixitcustomers.Common.Common;
import com.example.rashidsaddique.mrfixitcustomers.Model.Rate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit;
    MaterialRatingBar ratingBar;
    MaterialEditText edtComments;


    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference employeeInformationRef;


    double ratingStars = 0.0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //init firebase
        database = FirebaseDatabase.getInstance();
        rateDetailRef = database.getReference(Common.rate_detail_table);
        employeeInformationRef = database.getReference(Common.employees_tbl);

        //init view
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        ratingBar = (MaterialRatingBar)findViewById(R.id.ratingBar);
        edtComments = (MaterialEditText)findViewById(R.id.edtComment);


        //Event
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars = rating;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRateDetails(Common.employeeId);
            }
        });

    }

    private void submitRateDetails(final String employeeId) {
        final android.app.AlertDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();


       Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(edtComments.getText().toString());

        //update to firebase
        rateDetailRef.child(employeeId)
                .push() //generate unique key
                 .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //After successful update on firebase , calculate average and update to employee info table
                        rateDetailRef.child(employeeId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        double averageStars = 0.0;
                                        int count = 0;
                                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                                        {
                                            Rate rate = postSnapshot.getValue(Rate.class);
                                            averageStars+= Double.parseDouble(rate.getRates());
                                            count++;
                                        }
                                        double finalAverage = averageStars/count;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);


                                        //Create object update
                                        Map<String,Object> employeeUpdateRate = new HashMap<>();
                                        employeeUpdateRate.put("rates",valueUpdate);


                                        employeeInformationRef.child(Common.employees_tbl)
                                                .updateChildren(employeeUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Thanks For Your Feedback", Toast.LENGTH_SHORT).show();
                                                        finish();

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Rate Updated but can't update to Employee Table", Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                Toast.makeText(RateActivity.this, "Rate Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

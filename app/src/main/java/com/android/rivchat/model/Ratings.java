package com.android.rivchat.model;

import android.support.annotation.NonNull;

import com.android.rivchat.data.StaticConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Ratings {

    private HashMap ensinar;
    private int aprender;

    public Ratings(){
        this.setValues();
    }

    private void setValues() {
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap map = (HashMap) dataSnapshot.getValue();
                if(map!=null){
                    aprender = (Integer) map.get("aprenderRatings");
                    ensinar = (HashMap) map.get("ensinarRatings");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Integer getAprender() {
        return aprender;
    }

    public HashMap getEnsinar() {
        return ensinar;
    }

}

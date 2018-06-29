package com.android.rivchat.model;

import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;

public class Materias {
    private HashMap<String,String> addMaterias;
    private HashMap materiasMap;
    private String[] materias;
    private int i = 0;

    public Materias(){}

    public void setAddMaterias(HashMap<String,String> addMaterias) {
        this.addMaterias = addMaterias;
    }

    public void setMaterias(HashMap<String,String> materias){
        this.materiasMap = materias;
    }

    public HashMap<String,String> getAddMaterias() {
        return addMaterias;
    }

}

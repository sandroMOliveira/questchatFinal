package com.android.rivchat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rivchat.R;
import com.android.rivchat.data.StaticConfig;
import com.android.rivchat.model.Materias;
import com.android.rivchat.service.ServiceUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;

public class Choices extends AppCompatActivity{

    private GridView gridView;
    private GridView gridAprender;
    private View btnGo;
    private LovelyProgressDialog lovelyProgressDialog;
    private ArrayList<String> selected;
    private ArrayList<String> selectAprender;
    private String[] materias;
    private String aprenderDB;
    private String ensinarDB;
    private int i = 0;
    private static final String[] numbers = new String[]{
      "Portugês", "Matemática","Química","Física","História","Geografia"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choices);

        gridView = (GridView) findViewById(R.id.grid);
        gridAprender = (GridView) findViewById(R.id.grid2);
        btnGo = findViewById(R.id.button);

        selected = new ArrayList<>();
        selectAprender = new ArrayList<>();
        lovelyProgressDialog = new LovelyProgressDialog(this);
        if (materias == null){
            lovelyProgressDialog.setCancelable(true)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Carregando informações")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            getListMateriaUId();
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lovelyProgressDialog.dismiss();
                initGrides();
            }
        },3000);
        //set listener for Button event
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensinarDB = null;
                aprenderDB = null;
                //Intent intent = new Intent(this, SelectedItemsActivity.class);
                //intent.putStringArrayListExtra("SELECTED_LETTER", selectedStrings);
                //startActivity(intent);
                if(!selected.isEmpty() && !selectAprender.isEmpty()){
                    for(String string : selected){
                        ensinarDB = string + ";" + ensinarDB;
                        System.out.println("Ensinar: " + ensinarDB);
                    }
                    for(String s : selectAprender){
                        aprenderDB = s + ";" + aprenderDB;
                        System.out.println("Aprender: " + aprenderDB);
                    }
                    ServiceUtils.saveMaterias(Choices.this, ensinarDB, aprenderDB);
                }else{
                    Snackbar.make(v, "Selecione pelo menos uma matéria para continuar!",
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

    }

    private void initGrides(){
        final GridViewAdapter adapter = new GridViewAdapter(materias, this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.sList.indexOf(position);
                if (selectedIndex > -1) {
                    adapter.sList.remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    selected.remove((String) parent.getItemAtPosition(position));
                } else {
                    adapter.sList.add(position);
                    ((GridItemView) v).display(true);
                    selected.add((String) parent.getItemAtPosition(position));
                }
            }
        });

        final GridViewAdapter adapter2 = new GridViewAdapter(materias,this);
        gridAprender.setAdapter(adapter2);
        gridAprender.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter2.sList.indexOf(position);
                if (selectedIndex > -1) {
                    adapter2.sList.remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    selectAprender.remove((String) parent.getItemAtPosition(position));
                } else {
                    adapter2.sList.add(position);
                    ((GridItemView) v).display(true);
                    selectAprender.add((String) parent.getItemAtPosition(position));
                }
            }
        });
    }

    private void getListMateriaUId() {
        FirebaseDatabase.getInstance().getReference().child("materias").child("addMaterias").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    materias = new String[mapRecord.size()];
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        materias[i] = mapRecord.get(key).toString();
                        i++;
                    }
                    //getAllFriendInfo(0);
                } else {
                    lovelyProgressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}

class GridItemView extends FrameLayout{
    private TextView textView;

    public GridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.activity_your_choises, this);
        textView = getRootView().findViewById(R.id.textGrid);
    }

    public void display(String text, boolean isSelected) {
        textView.setText(text);
        textView.setTextSize(10);
        //textView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        display(isSelected);
    }

    public void display(boolean isSelected) {
        textView.setBackgroundResource(isSelected ? R.drawable.green_square : R.drawable.gray_square);
    }
}

class GridViewAdapter extends BaseAdapter{
    private Activity activity;
    private String[] strings;
    public List sList;

    public GridViewAdapter(String[] strings, Activity activity){
        this.strings = strings;
        this.activity = activity;
        sList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return strings.length;
    }

    @Override
    public Object getItem(int position) {
        return strings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItemView customView = (convertView == null) ? new GridItemView(activity) : (GridItemView) convertView;
        customView.display(strings[position], sList.contains(position));

        return customView;
    }
}
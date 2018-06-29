package com.android.rivchat.service;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.renderscript.Sampler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.rivchat.R;
import com.android.rivchat.model.Materias;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.android.rivchat.data.SharedPreferenceHelper;
import com.android.rivchat.data.StaticConfig;
import com.android.rivchat.model.Friend;
import com.android.rivchat.model.ListFriend;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.CheckedOutputStream;


public class ServiceUtils {

    private static ServiceConnection connectionServiceFriendChatForStart = null;
    private static ServiceConnection connectionServiceFriendChatForDestroy = null;
    private static boolean firstAccess;
    private static int i = 0;
    private static Materias m;
    private static String[] materias;
    private static String aprenderAux, enisinarAux;

    public static boolean isServiceFriendChatRunning(Context context) {
        Class<?> serviceClass = FriendChatService.class;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void stopServiceFriendChat(Context context, final boolean kill) {
        if (isServiceFriendChatRunning(context)) {
            Intent intent = new Intent(context, FriendChatService.class);
            if (connectionServiceFriendChatForDestroy != null) {
                context.unbindService(connectionServiceFriendChatForDestroy);
            }
            connectionServiceFriendChatForDestroy = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    binder.getService().stopSelf();
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                }
            };
            context.bindService(intent, connectionServiceFriendChatForDestroy, Context.BIND_NOT_FOREGROUND);
        }
    }

    public static void stopRoom(Context context, final String idRoom) {
        if (isServiceFriendChatRunning(context)) {
            Intent intent = new Intent(context, FriendChatService.class);
            if (connectionServiceFriendChatForDestroy != null) {
                context.unbindService(connectionServiceFriendChatForDestroy);
                connectionServiceFriendChatForDestroy = null;
            }
            connectionServiceFriendChatForDestroy = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    binder.getService().stopNotify(idRoom);
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                }
            };
            context.bindService(intent, connectionServiceFriendChatForDestroy, Context.BIND_NOT_FOREGROUND);
        }
    }

    public static void startServiceFriendChat(Context context) {
        if (!isServiceFriendChatRunning(context)) {
            Intent myIntent = new Intent(context, FriendChatService.class);
            context.startService(myIntent);
        } else {
            if (connectionServiceFriendChatForStart != null) {
                context.unbindService(connectionServiceFriendChatForStart);
            }
            connectionServiceFriendChatForStart = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    for (Friend friend : binder.getService().listFriend.getListFriend()) {
                        binder.getService().mapMark.put(friend.idRoom, true);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                }
            };
            Intent intent = new Intent(context, FriendChatService.class);
            context.bindService(intent, connectionServiceFriendChatForStart, Context.BIND_NOT_FOREGROUND);
        }
    }

    public static void updateUserStatus(Context context){
        if(isNetworkConnected(context)) {
            String uid = SharedPreferenceHelper.getInstance(context).getUID();
            if (!uid.equals("")) {
                FirebaseDatabase.getInstance().getReference().child("user/" + uid + "/status/isOnline").setValue(true);
                FirebaseDatabase.getInstance().getReference().child("user/" + uid + "/status/timestamp").setValue(System.currentTimeMillis());
            }
        }
    }

    public static void updateFriendStatus(Context context, ListFriend listFriend){
        if(isNetworkConnected(context)) {
            for (Friend friend : listFriend.getListFriend()) {
                final String fid = friend.id;
                FirebaseDatabase.getInstance().getReference().child("user/" + fid + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            HashMap mapStatus = (HashMap) dataSnapshot.getValue();
                            if ((boolean) mapStatus.get("isOnline") && (System.currentTimeMillis() - (long) mapStatus.get("timestamp")) > StaticConfig.TIME_TO_OFFLINE) {
                                FirebaseDatabase.getInstance().getReference().child("user/" + fid + "/status/isOnline").setValue(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public static void insertMaterias(Materias materias){
        FirebaseDatabase.getInstance().getReference().child("materias").setValue(materias);
    }

    @Deprecated
    public static void verifyFirst(final Context context, String id){
        FirebaseDatabase.getInstance().getReference().child("user/" + id + "/firstAccess")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        assert dataSnapshot.getValue() != null;
                        String access = (String) dataSnapshot.getValue();
                        if(access.equals("true")){
                            StaticConfig.FIRST_ACCESS = "true";
                            Toast.makeText(context,"Teste: " + true, Toast.LENGTH_LONG).show();
                        }else StaticConfig.FIRST_ACCESS = "false";
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static boolean isNetworkConnected(Context context) {
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        }catch (Exception e){
            return true;
        }
    }

    public static void saveMaterias(Context context, String ensinar, String aprender){
        String[] ensinarAux = ensinar.substring(0, ensinar.length() - 5)
                .split(";");
        HashMap<String, Integer> ratingsEnsinar = new HashMap<>();
        for(String s : ensinarAux){
            ratingsEnsinar.put(s, 0);
        }

        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID +
                "/aprenderRatings").setValue(0);
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID + "/aprender")
        .setValue(aprender.substring(0, aprender.length() - 5));


        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID +
                "/ensinarRatings").setValue(ratingsEnsinar);
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID + "/ensinar")
                .setValue(ensinar.substring(0, ensinar.length() - 5));
    }

    private static void dialogMulti(final Context context, String[] materias, final String tipo){
        String[] testando = materias;
        Arrays.sort(testando);
        new LovelyChoiceDialog(context)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle("Escolha as matérias que você quer " + tipo)
                .setIcon(R.drawable.ic_search)
                .setMessage("Escolha a seguir:")
                .setItemsMultiChoice(testando, new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                    @Override
                    public void onItemsSelected(List<Integer> positions, List<String> items) {
                        if(tipo.equals("aprender")){
                            aprenderAux = TextUtils.join(";", items);
                        }else {
                            enisinarAux = TextUtils.join(";", items);
                            saveMaterias(context, enisinarAux, aprenderAux);
                        }
                        Toast.makeText(context, TextUtils.join(";", items),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setConfirmButtonText("Selecionar")
                .setConfirmButtonColor(R.color.colorPrimary)
                .show();
    }

    public static void getListMateriaUId(final Context context) {
        i = 0;
        FirebaseDatabase.getInstance().getReference().child("materias").child("addMaterias")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                    dialogMulti(context, materias, "aprender");
                    dialogMulti(context, materias, "ensinar");
                    //getAllFriendInfo(0);
                } else {
                    Log.d("Error", "ERROR");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    /*public void permissionsUser(int type, Context context){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }*/
}

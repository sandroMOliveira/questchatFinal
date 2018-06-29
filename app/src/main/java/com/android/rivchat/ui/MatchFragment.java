package com.android.rivchat.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rivchat.MainActivity;
import com.android.rivchat.R;
import com.android.rivchat.data.FriendDB;
import com.android.rivchat.data.StaticConfig;
import com.android.rivchat.model.Friend;
import com.android.rivchat.model.ListFriend;
import com.android.rivchat.model.ListMatch;
import com.android.rivchat.model.Matchs;
import com.android.rivchat.service.ServiceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MatchFragment extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerListMatch;
    private ListMatchAdapter adapter;
    public MatchFragment.FragMatchClickFloatButton onClickFloatButton;
    private ListMatch dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private LovelyProgressDialog matchsUsers;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Switch decision;
    private String switchDecision;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;
    public static String APRENDER = "aprender";
    public static String ENSINAR = "ensinar";

    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";
    private BroadcastReceiver deleteFriendReceiver;

    public MatchFragment() {
        onClickFloatButton = new FragMatchClickFloatButton();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                //ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                //ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };


        View layout = inflater.inflate(R.layout.fragment_match, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListMatch = layout.findViewById(R.id.recycleListMatch);
        decision = layout.findViewById(R.id.switchDecision);
        initSwitch();
        recyclerListMatch.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayoutMatch);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ListMatchAdapter(getContext(), null, this);
        recyclerListMatch.setAdapter(adapter);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());

        dataListFriend = new ListMatch();

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Matchs matchs : dataListFriend.getListMatchs()) {
                    assert idDeleted != null;
                    if (idDeleted.equals(matchs.getIdFriend())) {
                        ArrayList<Matchs> matchsArray = dataListFriend.getListMatchs();
                        matchsArray.remove(matchsArray);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);


        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter.mapMark != null) {
            ListFriendsAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        //listFriendID.clear();
        dataListFriend.getListMatchs().clear();
        adapter.notifyDataSetChanged();
        FriendDB.getInstance(getContext()).dropDB();
        //detectFriendOnline.cancel();

    }

    public void saveinfoUsers() {
        ServiceUtils.verifyFirst(getContext(), StaticConfig.UID);
    }


    private void initSwitch() {
        decision.setChecked(true);
        decision.setText("Aprender");
        decision.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    decision.setText("Aprender");
                } else {
                    decision.setText("Ensinar");
                }
            }
        });
    }

    public boolean getDecision() {
        return decision.isChecked();
    }


    public class FragMatchClickFloatButton implements View.OnClickListener {
        Context context;
        LovelyProgressDialog dialogWait;
        String msgDialog;
        String materias;
        String matchUser;
        LinkedHashSet<String> matUser;
        LinkedHashSet<String> matFriend;
        LinkedHashSet<String> matchArray = new LinkedHashSet<>();
        LovelyChoiceDialog choiceDialog;

        public FragMatchClickFloatButton() {
        }

        public MatchFragment.FragMatchClickFloatButton getInstance(Context context) {
            this.context = context;
            dialogWait = new LovelyProgressDialog(context);
            return this;
        }

        @Override
        public void onClick(final View view) {
            int tipo = 0;
            dataListFriend.getListMatchs().clear();
            if (getDecision()) {
                msgDialog = "Procurando pessoas com quem você possa aprender!";
                tipo = 1;
            } else {
                msgDialog = "Procurando pessoas que você pode ensinar!";
                tipo = 2;
            }
            matchsUsers = new LovelyProgressDialog(getContext()).setCancelable(true);
            matchsUsers.setIcon(R.drawable.ic_search)
                    .setTitle(msgDialog)
                    .setTopColorRes(R.color.colorPrimary)
                    .show();

            matchs(tipo, view);
            //ArrayAdapter<String> adapter = new DonationAdapter(this, loadDonationOptions());
        }

        private void matchs(int type, View view) {

            if (type == 1) {
                queryUser(APRENDER);
            } else {
                queryUser(ENSINAR);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    matchsUsers.dismiss();
                    Toast.makeText(getContext(), matchUser, Toast.LENGTH_LONG).show();
                }
            }, 3000);
        }

        /**
         * Consulta que pega os seus dados de matéria
         * @param choice
         *
         */
        private void queryUser(final String choice) {
            materias = "";
            FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap mapUser = (HashMap) dataSnapshot.getValue();
                            String[] parts;
                            assert mapUser != null;
                            if (choice.equals("aprender")) {
                                parts = String.valueOf(mapUser.get(choice)).split(";");
                                dialogChoice(parts, choice);
                            } else parts = String.valueOf(mapUser.get(choice)).split(";");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("Query", "Houve um erro");
                        }
                    });
        }

        private void dialogChoice(String[] materias, final String choice){
            String[] testando = materias;
            matUser = new LinkedHashSet<>();
            matchsUsers.dismiss();
            new LovelyChoiceDialog(getContext())
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Teste")
                    .setIcon(R.drawable.ic_search)
                    .setMessage("Escolha a seguir")
                    .setItems(testando, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                        @Override
                        public void onItemSelected(int position, String item) {
                            matUser.add(item);
                            queryFriends(choice);
                            Toast.makeText(getContext(), item,
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            if (matUser.isEmpty()) {
                Log.d("SetLinked", "Vazio");
            } else {

                Log.d("SetLinked", "Preechido");
            }
        }



        /**
         * Consulta que procura as matérias no amigo
         *
         * @param choices
         */
        private void queryFriends(final String choices) {
            //Lógica para match com outros usuários
            final String choicesAux;
            matchsUsers = new LovelyProgressDialog(getContext()).setCancelable(false);
            matchsUsers.setIcon(R.drawable.ic_add_friend)
                    .setTitle("Procurando amigos....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            if (choices.equals("aprender")) {
                choicesAux = "ensinar";
            } else choicesAux = "aprender";
            FirebaseDatabase.getInstance().getReference().child("user").orderByChild(choicesAux)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.getValue() != null) {
                                String id = dataSnapshot.getKey();
                                String materiasMatch = "";
                                Log.d("Id Friend", id);
                                if (!id.equals(StaticConfig.UID)) {
                                    HashMap map = (HashMap) dataSnapshot.getValue();
                                    assert map != null;
                                    String[] materias = String.valueOf(map.get(choicesAux))
                                            .split(";");
                                    Log.d("Materias", String.valueOf(map.get(choicesAux)));
                                    matFriend = new LinkedHashSet<>();
                                    matFriend.addAll(Arrays.asList(materias));
                                    if (!matFriend.isEmpty()) {
                                        Iterator<String> i = matFriend.iterator();
                                        while (i.hasNext()) {
                                            String materiasAux = i.next();
                                            if (matUser.contains(materiasAux)) {
                                                materiasMatch = materiasAux;
                                                Log.d("Contem", materiasAux);
                                            } else Log.d("Não contem", materiasAux);
                                        }
                                        if(!materiasMatch.isEmpty()) {
                                            endMatch(id, map, materiasMatch, choicesAux);
                                        }
                                    } else Log.d("Teste Linked", "Vazio");

                                }
                            } else Log.d("queryData", "Vazia");
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        /**
         * fim do match (Graças ao bom Deus)
         * @param id
         * @param map
         * @param materias
         */
        private void endMatch(String id,HashMap map, String materias, String choices){
            HashMap mapNota = (HashMap) map.get("ensinarRatings");
            Matchs matchs = new Matchs();
            matchs.setIdFriend(id);
            matchs.setName((String) map.get("name"));
            matchs.setAvata((String) map.get("avata"));
            if(choices.equals("aprender")){
                matchs.setAprender(materias);
                matchs.setEnsinar("");
            }else{
                matchs.setEnsinar(materias);
                matchs.setAprender("");
            }
            matchs.setEmailFriend((String) map.get("email"));
            matchs.setNota((Long) mapNota.get(materias));
            FirebaseDatabase.getInstance().getReference().child("matchs/" + StaticConfig.UID
                    + "/" + id + materias).setValue(matchs);

            matchsUsers.dismiss();
            findMatch(materias);
        }

        /**
         * Procura o match no Firebase
         *
         * @param materia
         */
        private void findMatch(String materia) {
            dialogWait.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Adicionando a lista....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            FirebaseDatabase.getInstance().getReference().child("matchs/" + StaticConfig.UID).orderByKey()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dialogWait.dismiss();
                            if (dataSnapshot.getValue() == null) {
                                //email not found
                                new LovelyInfoDialog(context)
                                        .setTopColorRes(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_add_friend)
                                        .setTitle("Ops :(")
                                        .setMessage("Não foi possível achar nenhum amigo!")
                                        .show();
                            } else {
                                String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                                if (id.equals(StaticConfig.UID)) {
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("Fail")
                                            .setMessage("Email not valid")
                                            .show();
                                } else {
                                    HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                                    Matchs user = new Matchs();
                                    user.setName((String) userMap.get("name"));
                                    user.setEmailFriend((String) userMap.get("emailFriend"));
                                    user.setAvata((String) userMap.get("avata"));
                                    user.setIdFriend((String) userMap.get("idFriend"));
                                    user.setAprender("ensinar");
                                    user.setEnsinar("aprender");
                                    dataListFriend.getListMatchs().add(user);
                                    //user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                                    //checkBeforAddFriend(id, user);
                                }
                                if(!dataListFriend.getListMatchs().isEmpty()) {
                                    adapter.setListMatch(dataListFriend);
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorPrimary)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("Success")
                                            .setMessage("Add friend success")
                                            .show();
                                    adapter.notifyDataSetChanged();
                                }else Log.d("datalist", "Vazio");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        /**
         * Verifica se o seu amigo já está na aba de conversas
         */
        private void checkBeforAddFriend(final String idFriend, Matchs userInfo) {
            dialogWait.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Adicionando amigo....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();

            //Check xem da ton tai id trong danh sach id chua
            if (listFriendID.contains(idFriend)) {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("Match")
                        .setMessage("Usuário " + userInfo.getEmailFriend() + " já é seu Match!")
                        .show();
            } else {
                addFriend(idFriend, true);
                listFriendID.add(idFriend);
                dataListFriend.getListMatchs().add(userInfo);
                FriendDB.getInstance(getContext()).addMatch(userInfo);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * Add amigo
         *
         * @param idFriend
         */
        private void addFriend(final String idFriend, boolean isIdFriend) {
            if (idFriend != null) {
                if (isIdFriend) {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(idFriend, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("False")
                                            .setMessage("False to add friend success")
                                            .show();
                                }
                            });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFriend(null, false);
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("False")
                                            .setMessage("False to add friend success")
                                            .show();
                                }
                            });
                }
            } else {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("É isso aí")
                        .setMessage("Há matchs para você!")
                        .show();
            }
        }


    }

    public void swapFragment(int id){
        FriendsFragment friendsFragment = new FriendsFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(id, friendsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}


class ListMatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ListMatch listMatch;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private MatchFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public ListMatchAdapter(Context context, ListMatch listMatch, MatchFragment fragment) {
        this.listMatch = listMatch;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    public void setListMatch(ListMatch listMatch2){
        this.listMatch = listMatch2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_match, parent, false);
        return new ItemMatchViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(listMatch != null) {
            final String name = listMatch.getListMatchs().get(position).getName();
            final String id = listMatch.getListMatchs().get(position).getIdFriend();
            //final String idRoom = listMatch.getListMatchs().get(position).idRoom;
            final String avata = listMatch.getListMatchs().get(position).getAvata();
            final String email = listMatch.getListMatchs().get(position).getEmailFriend();
            ((ItemMatchViewHolder) holder).txtName.setText(name);

            ((View) ((ItemMatchViewHolder) holder).txtName.getParent().getParent().getParent())
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StaticConfig.EMAIL_TEST = email;
                            String teste = listMatch.getListMatchs().get(position).getName();
                            Toast.makeText(context, teste, Toast.LENGTH_LONG).show();
                        }
                    });


            //AINDA NÃO SEI
            ((View) ((ItemMatchViewHolder) holder).txtName.getParent().getParent().getParent())
                    .setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            String friendName = (String) ((ItemMatchViewHolder) holder).txtName.getText();

                            new AlertDialog.Builder(context)
                                    .setTitle("Delete Friend")
                                    .setMessage("Are you sure want to delete " + friendName + "?")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            final String idFriendRemoval = listMatch.getListMatchs()
                                                    .get(position).getIdFriend();
                                            dialogWaitDeleting.setTitle("Deleting...")
                                                    .setCancelable(false)
                                                    .setTopColorRes(R.color.colorAccent)
                                                    .show();
                                            deleteFriend(idFriendRemoval);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                            return true;
                        }
                    });


            if (listMatch.getListMatchs().get(position).getEnsinar().length() > 0 ||
                    listMatch.getListMatchs().get(position).getEnsinar().length() > 0) {
                ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setVisibility(View.VISIBLE);
                //((ItemMatchViewHolder) holder).txtMatch.setVisibility(View.VISIBLE);
                if (!listMatch.getListMatchs().get(position).getEnsinar().startsWith(id)) {
                    ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setText("Ensinar: Matemática");
                    ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setTypeface(Typeface.DEFAULT);
                    ((ItemMatchViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                } else {
                    ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setText("Ensinar: Matemática");
                    ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setTypeface(Typeface.DEFAULT_BOLD);
                    ((ItemMatchViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT_BOLD);
                }
                //String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
                //String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
//            if (today.equals(time)) {
//                ((ItemMatchViewHolder) holder).txtMatch.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
//            } else {
//                ((ItemMatchViewHolder) holder).txtMatch.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
//            }
            } else {
                ((ItemMatchViewHolder) holder).txtAjudaEnsinar.setVisibility(View.GONE);
                //((ItemMatchViewHolder) holder).txtMatch.setVisibility(View.GONE);
                if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
                    //mapQuery.put(id, FirebaseDatabase.getInstance().getReference().child("message/" + idRoom).limitToLast(1));
                    mapChildListener.put(id, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                            if (mapMark.get(id) != null) {
                                if (!mapMark.get(id)) {
                                    //listFriend.getListFriend().get(position).message.text = id + mapMessage.get("text");
                                } else {
                                    //listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                }
                                notifyDataSetChanged();
                                mapMark.put(id, false);
                            } else {
                                //listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                notifyDataSetChanged();
                            }
                            //listFriend.getListFriend().get(position).message.timestamp = (long) mapMessage.get("timestamp");
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                    mapMark.put(id, true);
                } else {
                    mapQuery.get(id).removeEventListener(mapChildListener.get(id));
                    mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                    mapMark.put(id, true);
                }
            }
            if (listMatch.getListMatchs().get(position).getAvata().equals(StaticConfig.STR_DEFAULT_BASE64)) {
                ((ItemMatchViewHolder) holder).avata.setImageResource(R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(listMatch.getListMatchs().get(position).getAvata(),
                        Base64.DEFAULT);
                Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ((ItemMatchViewHolder) holder).avata.setImageBitmap(src);
            }

            if(listMatch.getListMatchs().get(position).getNota() >= 0){
                ((ItemMatchViewHolder) holder).stars.setRating(3);
            }

            if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
                mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/status"));
                mapChildListenerOnline.put(id, new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment add " + id, (boolean) dataSnapshot.getValue() + "");
                            //listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                            //notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment change " + id, (boolean) dataSnapshot.getValue() + "");
                            //listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                            //notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
            }
        }else{
            Log.d("List Match", "Vazio");
        }
    }

    @Override
    public int getItemCount() {
        if(listMatch != null) {
            return listMatch.getListMatchs() != null ? listMatch.getListMatchs().size() : 0;
        }else return 0;
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(context)
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Error")
                                .setMessage("Error occurred during deleting friend")
                                .show();
                    } else {
                        String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        FirebaseDatabase.getInstance().getReference().child("friend")
                                .child(StaticConfig.UID).child(idRemoval).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogWaitDeleting.dismiss();

                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Success")
                                                .setMessage("Friend deleting successfully")
                                                .show();

                                        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
                                        intentDeleted.putExtra("idFriend", idFriend);
                                        context.sendBroadcast(intentDeleted);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Error")
                                                .setMessage("Error occurred during deleting friend")
                                                .show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred during deleting friend")
                    .show();
        }
    }
}

class ItemMatchViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView avata;
    public RatingBar stars;
    public TextView txtName, txtMatch, txtAjudaEnsinar;
    private Context context;

    ItemMatchViewHolder(Context context, View itemView) {
        super(itemView);
        avata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        //txtMatch = (TextView) itemView.findViewById(R.id.txtMatch);
        txtAjudaEnsinar = (TextView) itemView.findViewById(R.id.txtAjudaEnsinar);
        stars = (RatingBar) itemView.findViewById(R.id.ratingLevel);
        this.context = context;
    }
}

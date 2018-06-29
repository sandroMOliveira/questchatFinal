package com.android.rivchat.model;

import com.android.rivchat.data.FriendDB;

import java.util.ArrayList;

public class ListMatch {

    private ArrayList<Matchs> listMatchs;

    public ArrayList<Matchs> getListMatchs() {
        return listMatchs;
    }

    public ListMatch(){
        listMatchs = new ArrayList<>();
    }

    public String getAvataById(String id){
        for(Matchs matchs: listMatchs){
            if(id.equals(matchs.getIdFriend())){
                return matchs.getIdRoom();
            }
        }
        return "";
    }

    public void setListMatchs(ArrayList<Matchs> listMatchs) {
        this.listMatchs = listMatchs;
    }
}

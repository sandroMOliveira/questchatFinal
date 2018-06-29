package com.android.rivchat.model;

import com.android.rivchat.data.StaticConfig;

public class Matchs{

    private String idUser;
    private String idFriend;
    private String emailFriend;
    private String idRoom;
    private String ensinar;
    private String aprender;
    private long nota;
    private String chave;
    private String avata;
    private String name;

    public Matchs(){
        this.idUser = StaticConfig.UID;
    }

    public Matchs(String idFriend){
        this.idUser = StaticConfig.UID;
        this.idFriend = idFriend;
    }

    public String getIdFriend() {
        return idFriend;
    }

    public void setIdFriend(String idFriend) {
        this.idFriend = idFriend;
    }

    public String getAprender() {
        return aprender;
    }

    public void setAprender(String aprender) {
        this.aprender = aprender;
    }

    public String getEnsinar() {
        return ensinar;
    }

    public void setEnsinar(String ensinar) {
        this.ensinar = ensinar;
    }

    public String getIdRoom(){
        return this.idRoom;
    }

    public void setIdRoom(String idRoom){
        this.idRoom = idRoom;
    }

    public long getNota() {
        return nota;
    }

    public void setNota(long nota){
        this.nota = nota;
    }

    public void setEmailFriend(String emailFriend) {
        this.emailFriend = emailFriend;
    }

    public String getEmailFriend() {
        return emailFriend;
    }

    public String getChave() {
        assert aprender != null;
        if(!aprender.equals("")) {
            this.chave = emailFriend + aprender;
        }else this.chave = emailFriend + ensinar;

        return chave;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getAvata() {
        return avata;
    }

    public void setAvata(String avata) {
        this.avata = avata;
    }
}

package com.android.rivchat.model;



public class User {
    public String name;
    public String email;
    public String avata;
    public Status status;
    public String firstAccess;
    public Message message;
    public String aprender;
    public String ensinar;


    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }

    public User(int id){}
}

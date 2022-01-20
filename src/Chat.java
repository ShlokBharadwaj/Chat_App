import java.io.*;

//With the help of this class we will have differnet types of messages.
public class Chat{

    //Different types of messages that the client can send
    //WHOISIN to receive the list of the user connected
    //MESSAGE to send an ordinary text message
    //LOGOUT to disconnect from the server

    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
    private int type;
    private String message;

    //constructor
    Chat(int type, String message){
        this.type = type;
        this.message = message;
    }

    int getType(){
        return type;
    }

    String getMessage(){
        return message;
    }
}
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputFilter.Status;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.*; // for Exception

public class server{
    //unique ID for every new Connection
    private static int uniqueID;
    
    //ArrayList to track list of all clients
    private ArrayList<ClientThread> al;

    //To display the time
    private SimpleDateFormat sdf;

    //Port connection
    private int port;

    //Check server running status
    private boolean Status;

    //for notification purpose
    private String noti = "***";

    //Constructor to receive the port to listen for connection as parameter
    public server(int port){

        //the port
        this.port = port;

        //display the date
        sdf = new SimpleDateFormat("HH:mm:ss");

        //ArrayList for clients
        al = new ArrayList<ClientThread>();
    }

    //for starting the server
    public void start(){
        Status = true; //Server is Alive...

        //Creating serverSocker and waiting for connection request
        try{

            //socket used by server
            ServerSocket serverSocket = new ServerSocket(port);

            //infinite loop to wait for connection as long as the server is active
            while(Status){
                display("Server is waiting for clients on port: "+port+".");

                // accept connection if requested from client
                Socket socket = serverSocket.accept();
                // break if server stopped
                if(!Status)
                    break;
                // if client is connected, create its thread
                ClientThread t = new ClientThread(socket);
                //add this client to arraylist
                al.add(t);

                t.start();
            }

             // try block to stop the server
             try {
                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        // close all data streams and socket
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }

            catch(Exception e) {
                display("Exception, Closing the Server and Clients: " + e);
            }
        }
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    //method to stop the server
    protected void stop() {
        Status  =false;
        try{
            new Socket("localhost",port);
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    //Display event console
    private void display(String msg){
        String time = sdf.format(new Date())+ "" + msg;
        System.out.println(time);
    }

    //To broadcast message to all the clients
    private synchronized boolean broadcast(String message){

        //add timestamp
        String time = sdf.format(new Date());

        //to check if message is private i.e; Cleint to Client
        String [] w = message.split(" ", 3);

        boolean isPrivate = false;
        if(w[1].charAt(0)=='@')     //@lol msg private msg
            isPrivate=true;

        //if message is private, send only to menthioned username only
        if(isPrivate=true){
            String tocheck=w[1].substring(1,w[1].length());

            message= w[0]+w[2];
            String messageLf = time + " " + message + "\n";
            boolean found = false;

            //loop in reverse order to find the mentioned username on the server
            for(int z = al.size(); --z>=0;){
                ClientThread ct1 = al.get(z);
                String check  = ct1.getUsername();
                if(check.equals(tocheck)){
                     // try to write to the Client if it fails remove it from the list
                     if(!ct1.writeMsg(messageLf)) {
                        al.remove(z);
                        display("Disconnected Client " + ct1.username + " removed from list.");
                    }
                    // username found and delivered the message
                    found=true;
                    break;
                }
            }

            // mentioned user not found, return false
            if(found!=true)
            {
                return false;
            }
        }

        //if the message is a broadcast message...
        else{
            String messageLf = time + " " + message + "\n";

            //display the message
            System.out.println(messageLf);

            // we loop in reverse order in case we have to remove a Client
            // because they had been disconnected
            for(int i = al.size(); --i >= 0;) {
                ClientThread ct = al.get(i);
                
                // try to write to the Client if it fails remove it from the list
                if(!ct.writeMsg(messageLf)) {
                    al.remove(i);
                    display("Disconnected Client " + ct.username + " removed from list.");
                }
            }
        }
        return true;
        }

        // if client sent LOGOUT message to exit
        synchronized void remove(int id) {

        String disconnectedClient = "";

        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
           
            // if found remove it
            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                al.remove(i);
                break;
            }
        }
        broadcast(noti + disconnectedClient + " has left the chat room." + noti);
    }

    private static void main(String args[]) {

        //start the server on port 7777 unless a portNumber has been specified
        int portNumber = 7777;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java server [portNumber]");
                return;

        }
        // create a server object and start it
        server server = new server(portNumber);
        server.start();
    }

    //each client will have their own instance
    class ClientThread extends Thread{

        //Socket to get message
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        //uniqueID for disconnecting
        int id;

        //username of the Client
        String username;

        //message object to receive message
        Chat c;

        //timestamp
        String date;

        //Constructor 
        ClientThread(Socket socket){

            //a unique id
            id = ++uniqueID;
            this.socket = socket;

            //Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                broadcast(noti + username + " has joined the chat room." + noti);
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
                System.out.println(e);
            }
            date = new Date().toString() + "\n";
        }

        public String getUsername(){
            return username;
        }

        public void setUsername(){
            this.username = username;
        }

        //infinite loop to read and forward message
        public void run(){

            //loop until logout
            boolean Alive = true;
            while(Alive){
                //read String
                try{
                    c = (Chat) sInput.readObject();
                }
                catch(IOException e){
                    display(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException cnfe){
                    break;
                }
                // get the message from the Chat object received
                String message = c.getMessage();

                // different actions based on type message
                switch(c.getType()) {

                    case Chat.MESSAGE:
                        boolean confirmation =  broadcast(username + ": " + message);
                        if(confirmation==false){
                            String msg = noti + "Sorry. No such user exists." + noti;
                            writeMsg(msg);
                        }
                        break;
                    case Chat.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        Alive = false;
                        break;
                    case Chat.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        // send list of active clients
                        for(int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }

            }

            // if out of the loop then disconnected and remove from client list
            remove(id);
            close();
        }

        // close everything
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        // write a String to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display(noti + "Error sending message to " + username + noti);
                display(e.toString());
            }
            return true;
        }
    }
}

















































import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
        sdf = new SimpleDateFormat(pattern:"HH:mm:ss");

        //ArrayList for clients
        al = new ArrayList<ClientThread>();
    }

}
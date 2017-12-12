package servidor;

import servidor.Receptor;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor implements Runnable {

    private ServerSocket server;

    private List<Receptor> receptores;

    private boolean inicializado;
    private boolean executando;
    private volatile String numeros;

    private Thread thread;
    
    boolean aux = false;

    public Servidor(int port, String numeros) throws Exception {
        receptores = new ArrayList<Receptor>();
        this.numeros = numeros;
        inicializado = false;
        executando = false;
        

        open(port);
    }

    private void open(int port) throws Exception {
        server = new ServerSocket(port);
        inicializado = true;
    }

    private void close() {
        for (Receptor receptor : receptores) {
            try {
                receptor.stop();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        try {
            server.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        server = null;

        inicializado = false;
        executando = false;

        thread = null;
    }

    public void start() {
        if (!inicializado || executando) {
            return;
        }

        executando = true;
        thread = new Thread(this);
        thread.start();

    }

    public void stop() throws Exception {
        executando = false;

        if (thread != null) {
            thread.join();
        }
    }

    @Override
    public void run() {
        while (executando) {
            try {
            
                server.setSoTimeout(2500);

                Socket socket = server.accept();

                Receptor receptor = new Receptor(socket, numeros);
                receptor.start();

                receptores.add(receptor);
            } catch (SocketTimeoutException e) {

            } catch (Exception e) {
                System.out.println(e);
                break;
            }
        }

        close();
    }
    
    
    public static Connection conexao;
    public static String data;

    public static void main(String[] args) throws Exception {
        int port = 4444;
        String numero = "10 11 12 13 14 15";
        
        
        int d;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        data = sdf.format(new Date());
       
        

        try {
            Class.forName("com.mysql.jdbc.Driver");

            String driverURL = "jdbc:mysql://localhost:3306/trabalhoredes";

            conexao = DriverManager.getConnection(driverURL, "root", "");
        } catch (Exception ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

        Servidor servidor = new Servidor(port, numero);
        servidor.start();
        System.out.println("Inicializando servidor de porta " + port);

        System.out.println("Aperte ENTER para encerrar o servidor");
        new Scanner(System.in).nextLine();

        servidor.stop();
        System.out.println("Servidor encerrado");
    }
}

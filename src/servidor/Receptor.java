package servidor;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import static servidor.Servidor.conexao;
import static servidor.Servidor.data;

public class Receptor implements Runnable {

    private Socket socket;
    private final Criptografias c = new Criptografias();

    private BufferedReader in;
    private PrintStream out;

    private boolean inicializado;
    private boolean executando;
    private final String numerosServer;
    private Thread thread;

    public Receptor(Socket socket, String numeros) throws Exception {
        this.socket = socket;
        this.numerosServer = numeros;
        this.inicializado = false;
        this.executando = false;

        open();
    }

    private void open() throws Exception {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            inicializado = true;
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private void close() throws Exception {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        in = null;
        out = null;
        socket = null;

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
                Usuario user = new Usuario();
                String nomeUsuario;
                String msg;
                String msgSenha;
                msg = in.readLine();
                while (true) {
                    switch (msg) {
                        case "1":
                            msg = in.readLine();
                            user = verificaUsuario(conexao, msg);
                            if (user != null) {
                                msg = "true";
                                out.println(msg);
                                msgSenha = in.readLine();
                                msgSenha = c.decrypt(msgSenha, c.getChaveencriptacao());
                                msg = verificaSenha(conexao, user.getNome(), msgSenha);
                                if ("true".equals(msg)) {
                                    out.println(msg);
                                    conversacao(in, out, numerosServer, user);
                                    break;
                                }

                            }
                            msg = "false";
                            out.println(msg);
                            break;
                        case "2":
                            msg = in.readLine();
                            nomeUsuario = msg;
                            user = verificaUsuario(conexao, msg);
                            if (user == null) {
                                out.println("false");
                                msgSenha = in.readLine();
                                msgSenha = c.decrypt(msgSenha, c.getChaveencriptacao());
                                if (msgSenha.equals(c.decrypt(in.readLine(), c.getChaveencriptacao()))) {
                                    out.println("true");
                                    user = adicionaUsuario(nomeUsuario, msgSenha, conexao);
                                    conversacao(in, out, numerosServer, user);
                                    break;
                                }else
                                    out.println("false");
                            }else
                                out.println("true");
                        case "0":
                            break;
                    }
                    break;
                }
            } catch (Exception e) {
                System.out.println(e);
                break;
            }
        }
    }

    public static void conversacao(BufferedReader in, PrintStream out, String numerosServer, Usuario user) throws IOException, SQLException {
        String msg;
        String numeros;
        String ticket;
        int cont;
        boolean aux = true;
        out.println(user.getNome());
        while (aux) {
            msg = in.readLine();
            switch (msg) {
                case "1":

                    while (true) {
                        numeros = "";
                        numeros = in.readLine() + " " + in.readLine() + " " + in.readLine() + " " + in.readLine() + " " + in.readLine() + " " + in.readLine();
                        out.println(numeros);
                        msg = in.readLine();
                        if ("1".equals(msg)) {
                           ticket = adicionaNumeros(numeros, user.getId(), conexao);
                           cont = quantidadeTicket(user.getId(), conexao) + 1;
                            out.println(ticket);
                            out.println(cont);
                            break;
                        } else if ("9".equals(msg)) {
                            break;
                        }

                    }

                    break;
                case "2":

                    out.println(numerosServer);

                    break;
                case "3":
                    msg = in.readLine();
                    out.println(verificaTicket(msg, conexao, user.getId(), data, numerosServer));

                    break;
                case "0":
                    aux = false;
                    break;
            }

        }
    }

    public Usuario verificaUsuario(Connection conexao, String msg) throws SQLException {
        Statement operacao = conexao.createStatement();
        ResultSet resultado = operacao.executeQuery("SELECT id,nome FROM cliente WHERE nome ='" + msg + "'");

        if (resultado.next()) {
            String nome = resultado.getString("nome");
            int id = resultado.getInt("id");// se der erro usar Integer
            Usuario u = new Usuario(id, nome);
            return u;
        }
        return null;
    }

    public String verificaSenha(Connection conexao, String nomeUsuario, String msgSenha) throws SQLException {
        msgSenha = c.stringHexa(c.gerarHash(msgSenha, "SHA-1"));
        Statement operacao = conexao.createStatement();
        ResultSet resultado = operacao.executeQuery("SELECT * FROM cliente WHERE nome='" + nomeUsuario + "' AND senha='" + msgSenha + "'");

        if (resultado.next()) {
            return "true";
        }
        return "false";
    }

    private Usuario adicionaUsuario(String nomeUsuario, String msgSenha, Connection conexao) throws SQLException {

        msgSenha = c.stringHexa(c.gerarHash(msgSenha, "SHA-1"));

        String sql = "INSERT INTO cliente (nome,senha) VALUES ('" + nomeUsuario + "'," + "'" + msgSenha + "')";
        System.out.println(sql);
        Statement operacao = conexao.createStatement();
        operacao.executeUpdate(sql);
        Usuario u = new Usuario(1, nomeUsuario);
        return u;
    }

    private static String verificaTicket(String msg, Connection conexao, int id, String data, String numerosServer) throws SQLException {

        Statement operacao = conexao.createStatement();
        ResultSet resultado = operacao.executeQuery("SELECT numero FROM ticket WHERE ticket ='" + msg + "' AND idCliente ='" + id + "'");
        System.out.println("SELECT numero FROM ticket WHERE ticket ='" + msg + "' AND idCliente ='" + id + "'");
        if (resultado.next()) {
            String numeros = resultado.getString("numero");
            if (numeros.substring(17).equals(numerosServer)) {
                if (data.substring(0,10).equals(numeros.substring(0, 10)) && numeros.substring(11, 16).compareTo("21:00") < 0) {
                    return "Você foi o ganhador";
                }
            }
        }
        return "Infelizmente você não ganhou";
    }

    private static String adicionaNumeros(String numeros, int id, Connection conexao) throws SQLException {
        String ticket;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        numeros = sdf.format(new Date()) +" "+ numeros;
        ticket = Criptografias.stringHexa(Criptografias.gerarHash(numeros, "MD5"));

        String sql = "INSERT INTO ticket (idCliente,numero,ticket) VALUES ('" + id + "','" + numeros + "'," + "'" + ticket + "')";
        System.out.println(sql);
        Statement operacao = conexao.createStatement();
        operacao.executeUpdate(sql);
        return ticket;
    }

    private static int quantidadeTicket(int id, Connection conexao) throws SQLException {
        Statement operacao = conexao.createStatement();
        ResultSet resultado = operacao.executeQuery("SELECT * FROM ticket WHERE idCliente='" + id + "'");
        int cont = 0;

        while (resultado.next()) {
            cont++;
        }

        return cont;
    }
}

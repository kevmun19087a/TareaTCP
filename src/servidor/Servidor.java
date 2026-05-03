package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {
    private final Map<String, List<String>> registros = new ConcurrentHashMap<>();

    public static String obtenerFecha() {
        Date fecha = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaFormateada = dateFormat.format(fecha);

        return fechaFormateada;
    }

    public void servicio(int puerto) throws Exception {
        ServerSocket socket = new ServerSocket(puerto);
        System.out.println("Servidor iniciado en el puerto " + puerto);
        while (true) {
            final Socket cliente = socket.accept();
            System.out.println("Cliente conectado");

            new Thread(() -> {
                try {
                    InputStream input = cliente.getInputStream();
                    OutputStream output = cliente.getOutputStream();

                    DataInputStream dataInput = new DataInputStream(input);
                    String nombreCliente = dataInput.readUTF();
                    System.out.println("Mensaje recibido de: " + nombreCliente);

                    String timestamp = Servidor.obtenerFecha();

                    List<String> lista = registros.computeIfAbsent(nombreCliente, k -> new ArrayList<>());
                    String respuesta;
                    synchronized (lista) {
                        if (lista.size() >= 4) {
                            respuesta = "ERROR;MAX_REACHED;Ya se registraron 4 timbres";
                        } else {
                            lista.add(timestamp);
                            int index = lista.size();
                            respuesta = "OK;" + index + ";" + timestamp;
                        }
                    }

                    DataOutputStream dataOutput = new DataOutputStream(output);
                    dataOutput.writeUTF(respuesta);

                    cliente.close();
                } catch (Exception e) {
                    System.err.println("Error manejando cliente: " + e.getMessage());
                    try {
                        cliente.close();
                    } catch (Exception ex) {
                        // ignorar
                    }
                }
            }).start();
        }
    }
}
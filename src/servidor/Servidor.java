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
    private final String[] tiposPermitidos = new String[]{"Entrada","Salida al Almuerzo","Entrada del Almuerzo","Salida"};

    private boolean esTipoValido(String tipo) {
        if (tipo == null) return false;
        for (String t : tiposPermitidos) {
            if (t.equalsIgnoreCase(tipo)) return true;
        }
        return false;
    }

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
                    String recibido = dataInput.readUTF();
                    System.out.println("Mensaje recibido: " + recibido);

                    String[] partes = recibido.split("\\|", 2);
                    String nombreCliente = partes.length >= 1 ? partes[0].trim() : "";
                    String tipo = partes.length == 2 ? partes[1].trim() : "";

                    String respuesta;

                    if (nombreCliente.isEmpty() || !nombreCliente.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+")) {
                        respuesta = "ERROR;NOMBRE_INVALIDO;Nombre inválido. Solo letras y espacios permitidos";
                    } else if (!esTipoValido(tipo)) {
                        respuesta = "ERROR;TIPO_INVALIDO;Tipo inválido. Use uno de: Entrada, Salida al Almuerzo, Entrada del Almuerzo, Salida";
                    } else {
                        String timestamp = Servidor.obtenerFecha();
                        List<String> lista = registros.computeIfAbsent(nombreCliente, k -> new ArrayList<>());
                        synchronized (lista) {
                            if (lista.size() >= 4) {
                                respuesta = "ERROR;MAXIMO_ALCANZADO;Ya se registraron 4 timbres";
                            } else {
                                String expected = tiposPermitidos[lista.size()];
                                if (!expected.equalsIgnoreCase(tipo)) {
                                    respuesta = "ERROR;ORDEN_INVALIDA;Timbre inválido. Siguiente timbre esperado: " + expected;
                                } else {
                                    lista.add(tipo + ";" + timestamp);
                                    int index = lista.size();
                                    respuesta = "OK;" + index + ";" + timestamp + ";" + tipo;
                                }
                            }
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
                        
                    }
                }
            }).start();
        }
    }
}
package cliente;

import java.io.*;
import java.net.Socket;

public class Cliente {
    public String enviar(String nombre, String tipo) throws Exception{
        Socket client =  new Socket("localhost", 2025);
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(nombre + "|" + tipo);

            DataInputStream dis = new DataInputStream(in);
            String respuesta =  dis.readUTF();
            return respuesta;
        } finally {
            client.close();
        }
    }
}

package servidor.test;

import servidor.Servidor;

public class TestServidor {
    static void main(String[] args) throws Exception {
        Servidor servidor = new Servidor();
        servidor.servicio(2025);
    }
}


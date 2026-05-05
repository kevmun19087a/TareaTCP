package cliente.test;

import cliente.Cliente;

import java.util.Scanner;

public class TestCliente{
    static void main(String[] args) throws Exception {
        String nombre = "";
        System.out.println("Aplicacion servicio hora de entrada");
        System.out.println("Ingrese su nombre: ");

        Scanner sc = new Scanner(System.in);
        nombre = sc.nextLine();

        System.out.println("Seleccione tipo (escribir exactamente): Entrada, Salida al Almuerzo, Entrada del Almuerzo, Salida");
        String tipo = sc.nextLine();

        Cliente cliente = new Cliente();
        String resp = cliente.enviar(nombre, tipo);
        System.out.println("Respuesta servidor: " + resp);
    }
}

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/** Cliente de consola para conectarse al Servidor Intermedio e interactuar en el juego.*/
public class ClienteJuego {
    private static final int PUERTO = 12345;
    public static void main(String[] args) {
        BufferedReader lecturaTeclado = new BufferedReader(new InputStreamReader(System.in));
        try {
            // Solicitar la dirección IP donde se ejecuta el servidor intermedio
            System.out.print("Ingrese la dirección IP del Servidor Intermedio: ");
            String ipServidor = lecturaTeclado.readLine();
            System.out.println("Conectando al servidor en " + ipServidor + ":" + PUERTO + "...");
            Socket socket = new Socket(ipServidor, PUERTO);
            System.out.println("¡Conexión establecida con éxito!");

            BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salidaServidor = new PrintWriter(socket.getOutputStream(), true);
            String mensajeServidor;
            // Bucle de recepción de mensajes y turnos enviadas por el servidor
            while ((mensajeServidor = entradaServidor.readLine()) != null) {

                // Si el servidor indica que es nuestro turno, leemos de la consola la casilla deseada
                if (mensajeServidor.startsWith("TURNO_PROPIO:")) {
                    System.out.println("\n" + mensajeServidor.replace("TURNO_PROPIO: ", ""));
                    System.out.print("> Opción: ");
                    String jugada = lecturaTeclado.readLine();
                    salidaServidor.println(jugada);

                // Si se comete un error de validación, solicitamos el dato nuevamente
                } else if (mensajeServidor.startsWith("ERROR:")) {
                    System.out.println(mensajeServidor);
                    System.out.print("> Opción: ");
                    String jugada = lecturaTeclado.readLine();
                    salidaServidor.println(jugada);
                // Si es el fin del juego, mostramos el mensaje final y cerramos el cliente
                } else if (mensajeServidor.startsWith("FIN:")) {
                    System.out.println("\n==================================");
                    System.out.println(mensajeServidor.replace("FIN: ", ""));
                    System.out.println("==================================");
                    break;
 // Mensajes de estado, tablero o espera
                } else {
                    System.out.println(mensajeServidor);
                }
            }
            // Cierre de recursos
            socket.close();
            System.out.println("Conexión finalizada.");

        } catch (Exception e) {
            System.err.println("Error en la conexión con el servidor: " + e.getMessage());
        }
    }
}
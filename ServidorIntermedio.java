import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * Servidor Intermedio que gestiona la conexión entre dos computadoras clientes
 * y coordina la lógica y los turnos de la partida de juego en consola.*/
public class ServidorIntermedio {
    // Puerto por el cual escuchará las conexiones el servidor
    private static final int PUERTO = 12345;
    public static void main(String[] args) {
        System.out.println("=== SERVIDOR INTERMEDIO DE JUEGO INICIADO ===");
        System.out.println("Esperando a que dos jugadores (IPs) se conecten en el puerto " + PUERTO + "...");
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
  // Conexión y registro del Jugador 1
            Socket socketJugador1 = serverSocket.accept();
            System.out.println("Jugador 1 conectado desde la IP: " + socketJugador1.getInetAddress().getHostAddress());
            PrintWriter out1 = new PrintWriter(socketJugador1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socketJugador1.getInputStream()));
            out1.println("Bienvenido Jugador 1. Esperando a que se conecte el Jugador 2...");
            // Conexión y registro del Jugador 2
            Socket socketJugador2 = serverSocket.accept();
            System.out.println("Jugador 2 conectado desde la IP: " + socketJugador2.getInetAddress().getHostAddress());
            PrintWriter out2 = new PrintWriter(socketJugador2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socketJugador2.getInputStream()));
            out2.println("Bienvenido Jugador 2. Ambos jugadores están conectados.");
            // Notificar al Jugador 1 que ya está todo listo
            out1.println("¡Ambos jugadores están conectados! Comienza la partida.");
            // Inicializar el tablero de juego (3x3 con números del 1 al 9)
            char[] tablero = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
            int turno = 1; // 1 para Jugador 1 ('X'), 2 para Jugador 2 ('O')
            boolean juegoTerminado = false;
            // Bucle principal del juego en el servidor
            while (!juegoTerminado) {
                // Enviar el estado actual del tablero a ambos jugadores
                String dibujoTablero = obtenerDibujoTablero(tablero);
                out1.println("TABLERO\n" + dibujoTablero);
                out2.println("TABLERO\n" + dibujoTablero);
                if (turno == 1) {
                    // Turno del Jugador 1
                    out1.println("TURNO_PROPIO: Tu turno (Ficha X). Ingresa una casilla (1-9):");
                    out2.println("ESPERA: Esperando la jugada del Jugador 1...");

                    int casilla = recibirJugadaValida(in1, out1, tablero);
                    tablero[casilla - 1] = 'X';
                    // Verificar condición de victoria o empate
                    if (verificarVictoria(tablero, 'X')) {
                        notificarResultado(out1, out2, tablero, "¡Felicidades! Has ganado la partida.", "El Jugador 1 ha ganado la partida.");
                        juegoTerminado = true;
                    } else if (tableroLleno(tablero)) {
                        notificarResultado(out1, out2, tablero, "Partida terminada en Empate.", "Partida terminada en Empate.");
                        juegoTerminado = true;
                    } else {
                        turno = 2; // Cambiar turno al Jugador 2
                    }
                } else {
                    // Turno del Jugador 2
                    out2.println("TURNO_PROPIO: Tu turno (Ficha O). Ingresa una casilla (1-9):");
                    out1.println("ESPERA: Esperando la jugada del Jugador 2...");
                    int casilla = recibirJugadaValida(in2, out2, tablero);
                    tablero[casilla - 1] = 'O';
                    // Verificar condición de victoria o empate
                    if (verificarVictoria(tablero, 'O')) {
                        notificarResultado(out1, out2, tablero, "El Jugador 2 ha ganado la partida.", "¡Felicidades! Has ganado la partida.");
                        juegoTerminado = true;
                    } else if (tableroLleno(tablero)) {
                        notificarResultado(out1, out2, tablero, "Partida terminada en Empate.", "Partida terminada en Empate.");
                        juegoTerminado = true;
                    } else {
                        turno = 1; // Cambiar turno al Jugador 1
                    }
                }
            }
            // Cerrar conexiones al finalizar el juego
            socketJugador1.close();
            socketJugador2.close();
            System.out.println("Partida finalizada. Servidor cerrado.");

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
    /**
     * Valida que la casilla elegida por el jugador sea un número válido y no esté ocupada.*/
    private static int recibirJugadaValida(BufferedReader in, PrintWriter out, char[] tablero) throws Exception {
        while (true) {
            String entrada = in.readLine();
            try {
                int casilla = Integer.parseInt(entrada);
                if (casilla >= 1 && casilla <= 9) {
                    if (tablero[casilla - 1] != 'X' && tablero[casilla - 1] != 'O') {
                        return casilla; // Jugada válida
                    } else {
                        out.println("ERROR: Esa casilla ya está ocupada. Elige otra:");
                    }
                } else {
                    out.println("ERROR: Ingrese un número entre 1 y 9:");
                }
            } catch (NumberFormatException e) {
                out.println("ERROR: Entrada no válida. Debe ingresar un número del 1 al 9:");
            }
        }
    }
    /**
     * Construye la representación gráfica en texto del tablero.
     */
    private static String obtenerDibujoTablero(char[] t) {
        return " " + t[0] + " | " + t[1] + " | " + t[2] + "\n" +
               "---+---+---\n" +
               " " + t[3] + " | " + t[4] + " | " + t[5] + "\n" +
               "---+---+---\n" +
               " " + t[6] + " | " + t[7] + " | " + t[8];
    }
    /**
     * Comprueba las combinaciones ganadoras (filas, columnas y diagonales). */
    private static boolean verificarVictoria(char[] t, char ficha) {
        int[][] combinaciones = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Filas
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columnas
            {0, 4, 8}, {2, 4, 6}             // Diagonales
        };

        for (int[] c : combinaciones) {
            if (t[c[0]] == ficha && t[c[1]] == ficha && t[c[2]] == ficha) {
                return true;
            }
        }
        return false;
    }
    /**
     * Verifica si todas las casillas del tablero han sido jugadas. */
    private static boolean tableroLleno(char[] t) {
        for (char c : t) {
            if (c != 'X' && c != 'O') return false;
        }
        return true;
    }
    /**
     * Notifica el tablero final y el resultado a ambos jugadores.*/
    private static void notificarResultado(PrintWriter out1, PrintWriter out2, char[] t, String msg1, String msg2) {
        String finalTablero = "TABLERO_FINAL\n" + obtenerDibujoTablero(t);
        out1.println(finalTablero);
        out2.println(finalTablero);
        out1.println("FIN: " + msg1);
        out2.println("FIN: " + msg2);
    }
}
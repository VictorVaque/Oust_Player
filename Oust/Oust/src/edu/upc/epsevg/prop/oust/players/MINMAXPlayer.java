package edu.upc.epsevg.prop.oust.players;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de un jugador para el juego Oust que utiliza el algoritmo
 * Minimax con poda Alpha-Beta a una profundidad constante.
 * 
 */
public class MINMAXPlayer implements IPlayer, IAuto {
    
    private String name;
    private boolean timeout;
    private int nodesVisited;
    private final int profundidadMaxima;
    
    /**
     * Constructor del jugador MINMAX.
     * 
     * @param name Nombre del jugador
     * @param profundidadMaxima Profundidad máxima de búsqueda para el algoritmo Minimax.
     */
    public MINMAXPlayer(String name, int profundidadMaxima) {
        this.name = name;
        this.profundidadMaxima = profundidadMaxima;
    }
    
    /**
     * Calcula y devuelve el mejor movimiento para el estado actual del juego.
     * 
     * 
     * @param s Estado actual del juego
     * @return PlayerMove que contiene la secuencia de movimientos a realizar, el número de nodos visitados,
     *         la profundidad alcanzada y el tipo de búsqueda utilizado.
     */
    @Override
    public PlayerMove move(GameStatus s) {
        timeout = false;
        nodesVisited = 0;
        
        List<Point> moves = s.getMoves();
        if (moves.isEmpty()) {
            return new PlayerMove(null, nodesVisited, profundidadMaxima, SearchType.MINIMAX);
        }
        
        PlayerType p = s.getCurrentPlayer();
        int best = Integer.MIN_VALUE;
        List<Point> bestPath = null;
        
        System.out.println("=== INICIO BÚSQUEDA PROFUNDIDAD CONSTANTE ===");
        System.out.println("Profundidad máxima: " + profundidadMaxima);
        long startTime = System.currentTimeMillis();
        
        int a = Integer.MIN_VALUE;
        int b = Integer.MAX_VALUE;
        
        for (Point m : moves) {
            if (timeout) break;
            
            // Crear una copia para simular el turno completo
            GameStatus ns = new GameStatus(s);
            ns.placeStone(m);
            List<Point> path = new ArrayList<>();
            path.add(m);
            
            // Jugar el resto del turno
            PlayerType jugadorActual = p;
            while (ns.getCurrentPlayer() == jugadorActual && !ns.isGameOver()) {
                List<Point> opciones = ns.getMoves();
                if (opciones.isEmpty()) break;
                
                // Elegir la mejor opción según heurística
                Point mejor = opciones.get(0);
                if (opciones.size() > 1) {
                    int mejorValor = Integer.MIN_VALUE;
                    for (Point op : opciones) {
                        GameStatus temp = new GameStatus(ns);
                        temp.placeStone(op);
                        int valorTemp = Heuristica.eval(temp, jugadorActual);
                        if (valorTemp > mejorValor) {
                            mejorValor = valorTemp;
                            mejor = op;
                        }
                    }
                }
                ns.placeStone(mejor);
                path.add(mejor);
            }
            
            // Evaluar con minimax el estado resultante
            int val = minimax(ns, 1, a, b, p);
            if (timeout) break;
            
            if (val > best) {
                best = val;
                bestPath = path;
                a = Math.max(a, best);
            }
            
            if (best >= b) break;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("=== FIN BÚSQUEDA ===");
        System.out.println("Tiempo: " + elapsed + "ms");
        System.out.println("Nodos visitados: " + nodesVisited);
        System.out.println("Mejor valor: " + best);
        System.out.println("========================\n");
        
        return new PlayerMove(bestPath, nodesVisited, profundidadMaxima, SearchType.MINIMAX);
    }
    
    /**
     * Implementación recursiva del algoritmo Minimax con poda Alpha-Beta.
     * 
     * 
     * @param s Estado actual del juego
     * @param d Profundidad actual en el árbol de búsqueda
     * @param a Valor alpha para la poda
     * @param b Valor beta para la poda
     * @param maxP Jugador para el cual se está maximizando el valor
     * @return Valor heurístico del estado s para el jugador maxP
     */
    private int minimax(GameStatus s, int d, int a, int b, PlayerType maxP) {
        if (timeout) return 0;
        nodesVisited++;
        

        if (d >= profundidadMaxima || s.isGameOver()) {
            return Heuristica.eval(s, maxP);
        }
        
        PlayerType jugadorActual = s.getCurrentPlayer();
        
        List<Point> movimientosIniciales = s.getMoves();
        if (movimientosIniciales.isEmpty()) {
            GameStatus copia = new GameStatus(s);
            return minimax(copia, d + 1, a, b, maxP);
        }
        
        boolean max = (jugadorActual == maxP);
        int val = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        
        for (Point movInicial : movimientosIniciales) {
            if (timeout) break;
            
            // Crear copia y jugar el turno completo
            GameStatus ns = new GameStatus(s);
            ns.placeStone(movInicial);
            
            // Jugar el resto del turno
            while (ns.getCurrentPlayer() == jugadorActual && !ns.isGameOver()) {
                List<Point> opciones = ns.getMoves();
                if (opciones.isEmpty()) break;
                
                // Elegir la mejor opción según heurística
                Point mejor = opciones.get(0);
                if (opciones.size() > 1) {
                    int mejorValor = Integer.MIN_VALUE;
                    for (Point op : opciones) {
                        GameStatus temp = new GameStatus(ns);
                        temp.placeStone(op);
                        int valorTemp = Heuristica.eval(temp, jugadorActual);
                        if (valorTemp > mejorValor) {
                            mejorValor = valorTemp;
                            mejor = op;
                        }
                    }
                }
                ns.placeStone(mejor);
            }
            
            int v = minimax(ns, d + 1, a, b, maxP);
            if (timeout) break;
            
            if (max) {
                val = Math.max(val, v);
                if (val >= b) return val;
                a = Math.max(a, val);
            } else {
                val = Math.min(val, v);
                if (val <= a) return val;
                b = Math.min(b, val);
            }
        }
        
        return val;
    }
    
    /**
     * Método llamado por el sistema cuando se agota el tiempo del turno.
     */
    @Override
    public void timeout() {
        timeout = true;
        System.out.println("\n⏱️ TIMEOUT DETECTADO - Interrumpiendo búsqueda...");
    }
    
    /**
     * Devuelve el nombre de este jugador.
     * 
     * @return El nombre del jugador
     */
    @Override
    public String getName() {
        return name;
    }
}
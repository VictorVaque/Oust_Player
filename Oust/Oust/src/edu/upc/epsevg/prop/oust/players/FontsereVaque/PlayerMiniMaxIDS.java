package edu.upc.epsevg.prop.oust.players.FontsereVaque;

import edu.upc.epsevg.prop.oust.players.FontsereVaque.Heuristica;
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
 * Minimax con poda Alpha-Beta y búsqueda en profundidad iterativa (IDS).
 * 
 */
public class PlayerMiniMaxIDS implements IPlayer, IAuto {
    
    private String name;
    private boolean timeout;
    private int nodesVisited;
    private int currentMaxDepth;
    
    /**
     * Constructor del jugador IDS.
     * 
     * @param name Nombre del jugador
     */
    public PlayerMiniMaxIDS() {
        this.name = "FontsereVaque";
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
        currentMaxDepth = 0;
        
        List<Point> moves = s.getMoves();
        if (moves.isEmpty()) {
            return new PlayerMove(null, nodesVisited, 0, SearchType.MINIMAX);
        }
        
        PlayerType p = s.getCurrentPlayer();
        int best = Integer.MIN_VALUE;
        List<Point> bestPath = null;
        
        System.out.println("=== INICIO BÚSQUEDA IDS ===");
        long startTime = System.currentTimeMillis();
        
        for (currentMaxDepth = 1; !timeout; currentMaxDepth++) {
            System.out.println("--- Probando profundidad: " + currentMaxDepth + " ---");
            
            int currentDepthBest = Integer.MIN_VALUE;
            List<Point> currentDepthBestPath = null;
            int a = Integer.MIN_VALUE;
            int b = Integer.MAX_VALUE;
            
            for (Point m : moves) {
                if (timeout) break;
                
                GameStatus ns = new GameStatus(s);
                ns.placeStone(m);
                List<Point> path = new ArrayList<>();
                path.add(m);
                
                PlayerType jugadorActual = p;
                while (ns.getCurrentPlayer() == jugadorActual && !ns.isGameOver()) {
                    List<Point> opciones = ns.getMoves();
                    if (opciones.isEmpty()) break;
                    
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
                
                int val = minimax(ns, 1, a, b, p);
                if (timeout) break;
                
                if (val > currentDepthBest) {
                    currentDepthBest = val;
                    currentDepthBestPath = path;
                }
                
                a = Math.max(a, currentDepthBest);
                if (currentDepthBest >= b) break;
            }
            
            if (!timeout) {
                best = currentDepthBest;
                bestPath = currentDepthBestPath;
                System.out.println("Profundidad " + currentMaxDepth + " completada: mejor valor = " + best);
            } else {
                System.out.println("Profundidad " + currentMaxDepth + " interrumpida por timeout");
                break;
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("=== FIN BÚSQUEDA IDS ===");
        System.out.println("Profundidad alcanzada: " + (currentMaxDepth - 1));
        System.out.println("Tiempo: " + elapsed + "ms");
        System.out.println("Nodos visitados: " + nodesVisited);
        System.out.println("Mejor valor: " + best);
        System.out.println("========================\n");
        
        return new PlayerMove(bestPath, nodesVisited, currentMaxDepth - 1, SearchType.MINIMAX);
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
        
        if (d >= currentMaxDepth) {
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
            
            GameStatus ns = new GameStatus(s);
            ns.placeStone(movInicial);
            
            while (ns.getCurrentPlayer() == jugadorActual && !ns.isGameOver()) {
                List<Point> opciones = ns.getMoves();
                if (opciones.isEmpty()) break;
                
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
        System.out.println("\n⏱️⏱️⏱️ TIMEOUT DETECTADO ⏱️⏱️⏱️");
        System.out.println("Interrumpiendo búsqueda IDS...\n");
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
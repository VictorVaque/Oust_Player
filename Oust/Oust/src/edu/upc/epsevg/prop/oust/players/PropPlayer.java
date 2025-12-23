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
 * Jugador Minimax simple y rápido
 */
public class PropPlayer implements IPlayer, IAuto {
    
    private String name;
    private final int MAX_DEPTH;
    private boolean timeout;
    private int nodesVisited;
    
    public PropPlayer(String name, int depth) {
        this.name = name;
        this.MAX_DEPTH = depth;
    }
    
    @Override
    public PlayerMove move(GameStatus s) {
        timeout = false;
        nodesVisited = 0;
        
        List<Point> moves = s.getMoves();
        if (moves.isEmpty()) {
            return new PlayerMove(null, nodesVisited, MAX_DEPTH, SearchType.MINIMAX);
        }
        
        PlayerType p = s.getCurrentPlayer();
        int best = Integer.MIN_VALUE;
        List<Point> bestPath = null;
        int a = Integer.MIN_VALUE;
        int b = Integer.MAX_VALUE;
        
        System.out.println("=== INICIO BÚSQUEDA - Profundidad: " + MAX_DEPTH + " ===");
        long startTime = System.currentTimeMillis();
        int movimientosEvaluados = 0;
        
        for (Point m : moves) {
            if (timeout) {
                System.out.println("⏱️ TIMEOUT después de evaluar " + movimientosEvaluados + "/" + moves.size() + " movimientos");
                break;
            }
            
            GameStatus ns = new GameStatus(s);
            List<Point> path = completarPath(ns, m, p);
            int val = minimax(ns, 1, a, b, p);
            if (timeout) continue;
            movimientosEvaluados++;
            System.out.println("Movimiento " + movimientosEvaluados + "/" + moves.size() + ": " + m + " → valor: " + val);
            
            if (val > best) {
                best = val;
                bestPath = path;
            }
            
            a = Math.max(a, best);
            if (best >= b) {
                System.out.println("✂️ Poda alpha-beta en nivel raíz");
                break;
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("=== FIN BÚSQUEDA ===");
        System.out.println("Tiempo: " + elapsed + "ms");
        System.out.println("Nodos visitados: " + nodesVisited);
        System.out.println("Movimientos evaluados: " + movimientosEvaluados + "/" + moves.size());
        System.out.println("Mejor valor: " + best);
        if (timeout) {
            System.out.println("⚠️ BÚSQUEDA INTERRUMPIDA POR TIMEOUT");
        }
        System.out.println("========================\n");
        
        return new PlayerMove(bestPath, nodesVisited, MAX_DEPTH, SearchType.MINIMAX);
    }
    
    /**
     * Completa el path de una jugada (incluye capturas encadenadas)
     */
    private List<Point> completarPath(GameStatus s, Point m, PlayerType p) {
        List<Point> path = new ArrayList<>();
        path.add(m);
        s.placeStone(m);
        
        while (p == s.getCurrentPlayer() && !s.isGameOver()) {
            List<Point> conts = s.getMoves();
            if (conts.isEmpty()) break;
            
            Point mejor = conts.get(0);
            
            // Si hay múltiples opciones, elegir la que da mejor heurística
            if (conts.size() > 1) {
                int mejorV = Integer.MIN_VALUE;
                for (Point c : conts) {
                    GameStatus temp = new GameStatus(s);
                    temp.placeStone(c);
                    int v = Heuristica.eval(temp, p);
                    if (v > mejorV) {
                        mejorV = v;
                        mejor = c;
                    }
                }
            }
            
            path.add(mejor);
            s.placeStone(mejor);
        }
        
        return path;
    }
    
    /**
     * Minimax con Alpha-Beta
     */
    private int minimax(GameStatus s, int d, int a, int b, PlayerType maxP) {
        nodesVisited++;
        
        if (s.isGameOver()) {
            PlayerType w = s.GetWinner();
            if (w == maxP) return 1000000 - d;
            if (w != null) return -1000000 + d;
            return 0;
        }
        
        if (timeout || d >= MAX_DEPTH) {
            return Heuristica.eval(s, maxP);
        }
        
        List<Point> moves = s.getMoves();
        if (moves.isEmpty()) {
            return minimax(s, d + 1, a, b, maxP);
        }
        
        boolean max = (s.getCurrentPlayer() == maxP);
        int val = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        
        for (Point m : moves) {
            if (timeout) break;
            
            GameStatus ns = new GameStatus(s);
            completarPath(ns, m, s.getCurrentPlayer());
            int v = minimax(ns, d + 1, a, b, maxP);
            
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
    
    @Override
    public void timeout() {
        timeout = true;
        System.out.println("\n⏱️⏱️⏱️ TIMEOUT DETECTADO ⏱️⏱️⏱️");
        System.out.println("Interrumpiendo búsqueda minimax...\n");
    }
    
    @Override
    public String getName() {
        return "PropPlayer(" + name + ")";
    }
}
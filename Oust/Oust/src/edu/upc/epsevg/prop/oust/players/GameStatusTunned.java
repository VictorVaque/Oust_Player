package edu.upc.epsevg.prop.oust.players;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;

/**
 * GameStatus con cache simple de evaluación
 */
public class GameStatusTunned extends GameStatus {
    
    private Integer scoreP1;  // null = no calculado
    private Integer scoreP2;
    
    public GameStatusTunned(GameStatus gs) {
        super(gs);
        this.scoreP1 = null;
        this.scoreP2 = null;
    }
    
    public GameStatusTunned(GameStatusTunned other) {
        super(other);
        this.scoreP1 = other.scoreP1;
        this.scoreP2 = other.scoreP2;
    }
    
    @Override
    public void placeStone(Point point) {
        super.placeStone(point);
        // Invalidar cache cuando el tablero cambia
        scoreP1 = null;
        scoreP2 = null;
    }
    
    /**
     * Evaluación con cache
     */
    public int evaluarRapido(PlayerType jugador) {
        // Casos terminales
        int piezasP1 = contarPiezas(PlayerType.PLAYER1);
        int piezasP2 = contarPiezas(PlayerType.PLAYER2);
        
        if (piezasP2 == 0) return 500000;
        if (piezasP1 == 0) return -500000;
        
        // Usar cache si existe
        if (jugador == PlayerType.PLAYER1) {
            if (scoreP1 == null) {
                scoreP1 = Heuristica.eval(this, PlayerType.PLAYER1);
            }
            return scoreP1;
        } else {
            if (scoreP2 == null) {
                scoreP2 = Heuristica.eval(this, PlayerType.PLAYER2);
            }
            return scoreP2;
        }
    }
    
    /**
     * Cuenta piezas de un jugador (auxiliar)
     */
    private int contarPiezas(PlayerType jugador) {
        int count = 0;
        int size = getSquareSize();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Point p = new Point(i, j);
                if (isInBounds(p) && getColor(p) == jugador) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Obtiene número de piezas (sin cache, simple)
     */
    public int getPiezas(PlayerType jugador) {
        return contarPiezas(jugador);
    }
    
    /**
     * Genera clave única para transposition table
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = getSquareSize();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Point p = new Point(i, j);
                if (!isInBounds(p)) {
                    sb.append('X');
                    continue;
                }
                PlayerType c = getColor(p);
                if (c == null) sb.append('.');
                else if (c == PlayerType.PLAYER1) sb.append('1');
                else sb.append('2');
            }
        }
        
        return sb.toString();
    }
}
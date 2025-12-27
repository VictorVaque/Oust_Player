package edu.upc.epsevg.prop.oust.players;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Heuristica {

    private static final int[][] DIRS = {
        {0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}
    };
    
    private static final Set<Point> SEGUNDA_LINEA = new HashSet<>();
    
    public static void tactic_line(GameStatus s) {
        
        int size = s.getSize();
        int size2 = s.getSquareSize();
        int start = 2;
        int end = size - 1;
        for (int i = start; i <= end; i++) {
            SEGUNDA_LINEA.add(new Point(start, i));
            if (i != start) SEGUNDA_LINEA.add(new Point (i, start));
        }
        start = size2 - 3;
        end = size - 1;
        for (int i = start; i >= end; i--) {
            SEGUNDA_LINEA.add(new Point(start, i));
            if (i != start) SEGUNDA_LINEA.add(new Point (i, start));
        }
        start = 3;
        end = size;
        while (start != size - 1) {
            SEGUNDA_LINEA.add(new Point(start, end));
            SEGUNDA_LINEA.add(new Point (end, start));
            start++;
            end++;
        }    
        
    }
    

    public static int eval(GameStatus s, PlayerType p) {
        int score = 0;
        int size = s.getSquareSize();
        
        Set<Point> Visitados = new HashSet<>();
        if (s.getSize() > 6) tactic_line(s);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Point pt = new Point(i, j);
                if (!s.isInBounds(pt)) continue;

                PlayerType c = s.getColor(pt);
                if (c == null) continue;
                if (c != p) {
                    if (!Visitados.contains(pt)) {
                        int tamanoGrupo = dfsGrupo(s, pt, c, Visitados);
                        score -= tamanoGrupo * tamanoGrupo * 800;
                    }
                    continue;
                }
                if (!Visitados.contains(pt)) {
                    int tamanoGrupo = dfsGrupo(s, pt, c, Visitados);
                    score += tamanoGrupo * tamanoGrupo * 120;
                }
                int val = 25;

                if (SEGUNDA_LINEA.contains(pt)) {
                    val += 15;
                }

                int r1Propios = 0;
                int r1Rivales = 0;
                Set<Point> visitados = new HashSet<>();

                for (int[] d : DIRS) {
                    Point v1 = new Point(i + d[0], j + d[1]);
                    
                    if (!s.isInBounds(v1)) {
                        val -= 5;
                        continue;
                    }
                    visitados.add(v1);
                    PlayerType cv1 = s.getColor(v1);
                    if (cv1 == c) {
                        r1Propios++;
                    } else if (cv1 != null) {
                        r1Rivales++;
                    }
                }

                int r2Propios = 0;
                int r2Rivales = 0;

                for (int[] d1 : DIRS) {
                    Point v1 = new Point(i + d1[0], j + d1[1]);
                    if (!s.isInBounds(v1)) {
                        continue;
                    }

                    for (int[] d2 : DIRS) {
                        Point v2 = new Point(v1.x + d2[0], v1.y + d2[1]);
                        
                        if (!s.isInBounds(v2) && !visitados.contains(v2))  {
                            val -= 3;
                            visitados.add(v2);
                        }

                        if (!s.isInBounds(v2) || visitados.contains(v2) || v2.equals(pt)) {
                            continue;
                        }

                        visitados.add(v2);
                        PlayerType cv2 = s.getColor(v2);

                        if (cv2 == c) {
                            r2Propios++;
                        } else if (cv2 != null) {
                            r2Rivales++;
                        }
                    }
                }
                
                if ((r1Propios +  r1Rivales + r2Propios + r2Rivales) == 0) val+=15;

                val += r1Propios * 40;
                val += r2Propios * 10;
                
                if (r1Rivales > 0) {
                    int tamanoGrupo = dfsGrupo(s, pt, c, new HashSet<>());
                    int penalBase = r1Rivales * 350;
                    int penalExtra = 0;
                    
                    if (tamanoGrupo <= 2) {
                        penalExtra += r1Rivales * 50;
                    }
                    
                    int penalTamano = (tamanoGrupo >= 3) ? (tamanoGrupo - 2) * 100 : 0;
                    int penalCadena = (r2Propios > 0) ? r2Propios * 200 : 0;
                    val -= (penalBase + penalExtra + penalTamano + penalCadena);
                } else if (r2Rivales > 0) {
                    val -= r2Rivales * 150;
                }
                
                score += val;
            }
        }

        int centro = 0;
        int centroX = size / 2;
        int centroY = size / 2;
        
        for (int i = centroX - 2; i <= centroX + 2; i++) {
            for (int j = centroY - 2; j <= centroY + 2; j++) {
                Point pt = new Point(i, j);
                if (!s.isInBounds(pt)) continue;
                
                PlayerType c = s.getColor(pt);
                if (c == p) {
                    centro += 20;
                } else if (c != null) {
                    centro -= 20;
                }
            }
        }
        
        score += centro;

        return score;
    }
    
    private static int dfsGrupo(GameStatus s, Point start, PlayerType player, Set<Point> visitados) {
        java.util.Stack<Point> stack = new java.util.Stack<>();
        stack.push(start);
        visitados.add(start);
        int tamano = 0;
        
        while (!stack.isEmpty()) {
            Point actual = stack.pop();
            tamano++;
            for (int[] d : DIRS) {
                Point vecino = new Point(actual.x + d[0], actual.y + d[1]);
                
                if (s.isInBounds(vecino) && !visitados.contains(vecino) && s.getColor(vecino) == player) {    
                    visitados.add(vecino);
                    stack.push(vecino);
                }
            }
        }
        
        return tamano;
    }
}
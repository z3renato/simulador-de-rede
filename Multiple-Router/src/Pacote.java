/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Pacote {

    double tamanho;
    double tempo;
    int status;
    int roteador;
    boolean anterior;

    public Pacote(double tempo, int status, int roteador) {
        this.tamanho = retornaPct();
        this.tempo = tempo;
        this.status = status;
        this.roteador = roteador;
        this.anterior = false;
    }

    public static double retornaPct() {
        double u = aleatorio();
        if (u <= 0.5) {
            return 550.0;
        } else if (u <= 0.9) {
            return 40.0;
        } else {
            return 1500.0;
        }
    }

    public static double aleatorio() {
        return Math.random();
    }
}

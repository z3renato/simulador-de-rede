/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Collections;

public class HeapSort {

    private  void refaz(ArrayList<Pacote> v, int raiz, int folha) {
        int termino = 0, min;
        
        while ((raiz * 2 + 1 <= folha) && (termino == 0)) {
            min  = raiz;
            
            if (raiz * 2 + 1 == folha) {
                min = raiz * 2 + 1;
            } else if (v.get(raiz * 2 + 1).tempo < v.get(raiz * 2 + 2).tempo) {
                min = raiz * 2 + 1;
            } else {
                min = raiz * 2 + 2;
            }

            if (v.get(raiz).tempo < v.get(min).tempo) {
                Collections.swap(v, raiz, min);
            } else {
                termino = 1;
            }
        }
    }

    public  void heapSort(ArrayList<Pacote> v) {
        int i;

        for (i = (v.size() / 2) - 1; i >= 0; i--) {
            refaz(v, i, v.size() - 1);
        }

        for (i = v.size() - 1; i >= 1; i--) {
            Collections.swap(v, 0, i);
            refaz(v, 0, i - 1);
        }
    }
}

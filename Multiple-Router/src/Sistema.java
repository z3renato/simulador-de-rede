
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class Sistema {

    HeapSort h = new HeapSort();
    Roteador[] routers;
    int numRouters;
    double tempoSimulacao;
    double tempoAtual = 0;
    ArrayList<Pacote> pacotes = new ArrayList<>();
    ArrayList<Pacote> pacotesAguardando = new ArrayList<>();

    //Variáveis de Little
    double EW = 0.0;
    double EN = 0.0;
    Info EN_s = new Info();
    Info EW_entrada = new Info();
    Info EW_saida = new Info();
    double lambda = 0;

    public Pacote menorTempo(ArrayList<Pacote> pacotes) {
        if (pacotes.size() > 0) {
            return pacotes.get(0);
        } else {
            return null;
        }
    }

    public Pacote retiraMenorTempoAguardando() {
        if (pacotesAguardando.size() > 0) {
            return pacotesAguardando.get(0);
        } else {
            return null;
        }
    }

    public void inserePacoteEspera(Pacote p) {
        pacotesAguardando.add(p);
    }

    public void inserePacote(Pacote p) {
        pacotes.add(p);
    }

    public Pacote clonaPacote(Pacote p) {
        Pacote nPct = new Pacote(p.tempo, p.status, p.roteador);
        //para não gerar um tamanho aleatorio novamente, é passado o tamanho explicitamente.
        nPct.tamanho = p.tamanho;
        return nPct;
    }

    public Sistema(int numRouters, double tempoSimulacao) {
        this.numRouters = numRouters;
        this.tempoSimulacao = tempoSimulacao;
        this.routers = new Roteador[numRouters];
    }

    public double aleatorio() {
        return Math.random();
    }

    public int aguardando() {
        return 2;
    }

    public int chegada() {
        return 0;
    }

    public int processando() {
        return 1;
    }

    public boolean ehChegada(Pacote p) {
        return p.status == 0;
    }

    public boolean estaProcessando(Pacote p) {
        return p.status == 1;
    }

    public boolean estaAguardando(Pacote p) {
        return p.status == 2;
    }

    public void mostraFila() {
        System.out.println("Fila do sistema -----------------------------------------");
        for (Pacote p : pacotes) {

            System.out.print(p.tempo + " | " + p.tamanho + " | " + p.status + " | " + p.roteador);
            System.out.println("");
        }
        System.out.println("");
        System.out.println("-----------------------------------------");
    }

    public void littleEntradaSistema() {
        EN_s.somaAreas += EN_s.numEventos * (tempoAtual - EN_s.tempoAnterior);
        EN_s.tempoAnterior = tempoAtual;
        EN_s.numEventos++;

        //Cálculo E[W]
        EW_entrada.somaAreas += EW_entrada.numEventos * (tempoAtual - EW_entrada.tempoAnterior);
        EW_entrada.tempoAnterior = tempoAtual;
        EW_entrada.numEventos++;
    }

    public void littleSaidaSistema() {
        EN_s.somaAreas += EN_s.numEventos * (tempoAtual - EN_s.tempoAnterior);
        EN_s.tempoAnterior = tempoAtual;
        EN_s.numEventos--;

        EW_saida.somaAreas += EW_saida.numEventos * (tempoAtual - EW_saida.tempoAnterior);
        EW_saida.tempoAnterior = tempoAtual;
        EW_saida.numEventos++;
    }

    public void littleSistema() {
        EW_entrada.somaAreas += EW_entrada.numEventos * (tempoAtual - EW_entrada.tempoAnterior);
        EW_saida.somaAreas += EW_saida.numEventos * (tempoAtual - EW_saida.tempoAnterior);

        EW = EW_entrada.somaAreas - EW_saida.somaAreas;
        EW /= EW_entrada.numEventos;

        EN = EN_s.somaAreas / tempoAtual;

        lambda = EW_entrada.numEventos / tempoAtual;
    }

    public void mostraVariaveisLittle() {
        System.out.println("E[N] do sistema " + EN);
        System.out.println("E[W] do sistema " + EW);
        System.out.println("E[W] Little " + ((EN / lambda)));
        System.out.println("Validação " + (EN - EW * lambda));
        System.out.println("Lambda " + lambda);

        System.out.println("");
        System.out.println("");
        System.out.println("");
    }
	
	public void ajustaOcupacoes(ArrayList<Roteador> roteadores) {
        if (roteadores.size() > 1) {
            double mult = 0;
            for (int i = 1; i < roteadores.size(); i++) {
				mult = roteadores.get(i-1).ocupacaoPedida* roteadores.get(i-1).probSaidaEsperada;
				roteadores.get(i).ocupacaoPedida -= mult; 
				roteadores.get(i).setOcupacao();
				
            }
        }
    }

    public void Simulacao() {
        int cont = 0;
        //roteadores para teste	
        ArrayList<Roteador> roteadoresTeste = new ArrayList<>();
        roteadoresTeste.add(new Roteador(0, 0.8, 36750.0, 0.2, -1));
        roteadoresTeste.add(new Roteador(1, 0.8, 36750.0, 0.1, -1));
        roteadoresTeste.add(new Roteador(2, 0.8, 36750.0, 0.3, -1));
        roteadoresTeste.add(new Roteador(3, 0.8, 36750.0, 0.2, -1));
        roteadoresTeste.add(new Roteador(4, 0.8, 36750.0, 0.1, -1));
        //insere roteadores no sistema
		
		ajustaOcupacoes(roteadoresTeste);

        for (int i = 0; i < numRouters; i++) {
            routers[i] = roteadoresTeste.get(i);
        }
        Pacote pct;
        //calcula-se o tempo para cada pacote, define que é chegada com status 0 e qual é o roteador.
        for (int i = 0; i < numRouters; i++) {
            pacotes.add(new Pacote(routers[i].tempo(), chegada(), i));
        }
        //ordena os pacotes pelo tempo e coloca na posição 0 o menor.
        h.heapSort(pacotes);
        pct = pacotes.get(0);

        tempoAtual = pct.tempo;
        while (tempoAtual <= tempoSimulacao) {

            tempoAtual = pct.tempo;
            int r = pct.roteador;

            //se o status for igual a zero, é aceita a condição.
            if (ehChegada(pct)) {
                boolean anterior = false;
                boolean conseguiu = false;
                conseguiu = routers[r].entraPct(pct, tempoAtual, pacotes);
                if (conseguiu) { // se ele conseguiu colocar o pacote na fila
                    anterior = pct.anterior;
                    pct.anterior = false;
                } else {
                    pacotes.remove(pct);
                }
                littleEntradaSistema();

                // se o pacote não veio do roteador anterior eu crio o tempo de chegada do próximo
                //isso tem de ser verificado para não gerar mais pacotes em um roteador que recebeu dados do anterior
                //se não for feito o roteador vai ter uma taxa de chegada alterada
                if (!anterior && conseguiu) {
                    double tempoChegada = tempoAtual + routers[r].tempo();
                    pacotes.add(new Pacote(tempoChegada, chegada(), r));
                }
            } else { //saida do pacote
                if (routers[r].saiPct(pct, tempoAtual, numRouters - 1)) {
                    pct.roteador++;
                    pct.status = chegada();
                } else {
                    littleSaidaSistema();
                    pacotes.remove(pct);
                }
                if (!routers[r].filaVazia()) {
                    pacotes.add(routers[r].proximoPacote(tempoAtual));
                }
            }
            if (pacotes.size() > 0) {
                h.heapSort(pacotes);
            }
            pct = pacotes.get(0);

        }
        littleSistema();

        for (Roteador r : routers) {
            System.out.println("Resultados roteador " + r.id);
            r.mostraCalculos(tempoAtual);
            System.out.println("================================");
        }
        mostraVariaveisLittle();
    }
}

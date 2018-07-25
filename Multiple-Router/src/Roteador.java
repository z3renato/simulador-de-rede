
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class Roteador {

    int numDePacotesQueSairam = 0;
    int id;
    ArrayList<Pacote> filaRoteador = new ArrayList<>();
    double intervaloPacotes;
    double larguraBanda; // Bytes/s
    double probSaidaEsperada;
    double tamanhoMaxFila;
    double fila = 0.0;
    double tempoAtual = 0.0;
    double goodPut = 0.0, troughPut = 0.0;
    double ocupacao = 0.0;
    double probRede  = 0;

    Info EN_s = new Info();
    Info EW_entrada = new Info();
    Info EW_saida = new Info();
    double lambda = 0.0;
    double EW = 0.0;
    double EN = 0.0;double ocupacaoPedida = 0;

    double soma_pct_vazao = 0.0;
    double soma_pct_util = 0.0;
    double tamanho_pct = 0.0;

    double tempoSaida = 0.0;
    double tempoOcupacao = 0.0;

    public double define_ocupacao(double ocupacao) {
        ocupacao = ocupacao * larguraBanda;
        ocupacao /= 441;

        //intervalo entre pacotes em segundos
        double intervaloPct = 1 / ocupacao;
        intervaloPct = 1.0 / intervaloPct;
        return intervaloPct;
    }

    public Roteador(int id, double ocupacao, double larguraBanda, double probSaidaEsperada, double tamanhoMaxFila) {
        this.id = id;
        this.larguraBanda = larguraBanda;
        this.probSaidaEsperada = probSaidaEsperada;
        this.tamanhoMaxFila = tamanhoMaxFila;
        this.ocupacaoPedida = ocupacao;
        if (tamanhoMaxFila == -1) {
            this.tamanhoMaxFila = Double.POSITIVE_INFINITY;
        } else {
            this.tamanhoMaxFila = tamanhoMaxFila;
        }
        this.intervaloPacotes = define_ocupacao(ocupacao);
    }

    public Pacote proximoPacote(double tempoAtual) {

        if (filaRoteador.size() > 0) {
            Pacote nPct = clonaPacote(filaRoteador.get(0));
            nPct.status = 1;
            filaRoteador.remove(0);
            return nPct;
        } else {
            System.out.println("fila do roteador " + this.id + " vazia!");
            return null;
        }
    }

    public void mostraFila() {
        System.out.println("Fila no roteador-----------------------------------------");
        for (Pacote p : filaRoteador) {

            System.out.print(p.tempo + " | " + p.tamanho + " | " + p.status + " | " + p.roteador);
            System.out.println("");
        }
        System.out.println("");
        System.out.println("-----------------------------------------");
    }

    public static double aleatorio() {
        return Math.random();
    }

    public double tempo() {
        return (-1.0 / this.intervaloPacotes) * Math.log(aleatorio());
    }

    public boolean filaVazia() {
        return this.fila == 0;
    }

    public Pacote clonaPacote(Pacote p) {
        Pacote nPct = new Pacote(p.tempo, p.status, p.roteador);
        nPct.tamanho = p.tamanho;
        return nPct;
    }
    int pacotesRecebidos = 0;

    public boolean entraPct(Pacote pct, double tempoAtual, ArrayList<Pacote> p) {
        if (!filaCheia()) { //se a fila não excedeu o tamanho máximo.
//          System.out.println("========================================");
//          System.out.println("Entrada de pacote no roteador: " + this.id);

            //se o pacote veio de algum roteador.
            if (pct.anterior) {
                pacotesRecebidos++;
//              System.out.println("Recebido pacote do roteador " + (this.id - 1));
//              System.out.println("Tamanho: " + pct.tamanho);
            }
//			System.out.println("Tempo: " + pct.tempo);
//			System.out.println("Status: " + pct.status);
//			System.out.println("Tamanho do pacote: " + pct.tamanho);
//			System.out.println("Tamanho da fila atualmente: " + this.fila);
//			System.out.println("========================================");

            //se a fila está vazia, pacote recebe status 1 de processando.
            if (filaVazia()) {
                pct.tempo = tempoAtual + pct.tamanho / this.larguraBanda;
                pct.status = 1;
                this.tempoOcupacao += pct.tempo - tempoAtual;
            } else { //caso a fila nao esteja vazia, o status é 2 de aguardando 
                //e o pacote é adicionado na fila do roteador.
                pct.status = 2;
                filaRoteador.add(clonaPacote(pct));
                p.remove(pct);
            }

            fila++;
            EN_s.somaAreas += EN_s.numEventos * (tempoAtual - EN_s.tempoAnterior);
            EN_s.tempoAnterior = tempoAtual;
            EN_s.numEventos++;

            //Cálculo E[W]
            EW_entrada.somaAreas += EW_entrada.numEventos * (tempoAtual - EW_entrada.tempoAnterior);
            EW_entrada.tempoAnterior = tempoAtual;
            EW_entrada.numEventos++;

            return true;
        } else {
            //System.out.println("Pacote recusado pelo roteador " + this.id);
            return false;
        }
    }
    public void setOcupacao(){
        this.intervaloPacotes = define_ocupacao(ocupacaoPedida);
    }

    public boolean saiPct(Pacote pct, double tempoAtual, int ultimoRoteador) {
        boolean vaiParaProximo = false;
//		System.out.println("Saida de pacote do Roteador " + this.id);
        this.fila--;
//		System.out.println("Tempo: " + pct.tempo);
//		System.out.println("Status: " + pct.status);
//		System.out.println("Tamanho do pacote: " + pct.tamanho);
//		System.out.println("Tamanho da fila atualmente: " + this.fila);
//		System.out.println("========================================");
        if (this.id < ultimoRoteador && saidaEsperada()) { //se a prob de saida esperada for maior que aleatorio, recebe true.
            numDePacotesQueSairam++;
//          System.out.println("Pacote saiu do roteador " + this.id + " para o roteador " + this.id + 1);
//          System.out.println("Tamanho " + pct.tamanho);
            pct.anterior = true;
            vaiParaProximo = true;
        }
        //gerar o tempo de atendimento do pacote seguinte na fila.
        if (!filaVazia()) {
            double tempo = tempoAtual + filaRoteador.get(0).tamanho / this.larguraBanda;
            filaRoteador.get(0).tempo = tempo;
            tempoOcupacao += tempo - tempoAtual;
        }

        EN_s.somaAreas += EN_s.numEventos * (tempoAtual - EN_s.tempoAnterior);
        EN_s.tempoAnterior = tempoAtual;
        EN_s.numEventos--;

        EW_saida.somaAreas += EW_saida.numEventos * (tempoAtual - EW_saida.tempoAnterior);
        EW_saida.tempoAnterior = tempoAtual;
        EW_saida.numEventos++;

        soma_pct_vazao += pct.tamanho;
        soma_pct_util += pct.tamanho - 40.0;

        return vaiParaProximo;
    }

    public void mostraCalculos(double tempoAtual) {
        int unidade = 8;
        EW_entrada.somaAreas += EW_entrada.numEventos * (tempoAtual - EW_entrada.tempoAnterior);
        EW_saida.somaAreas += EW_saida.numEventos * (tempoAtual - EW_saida.tempoAnterior);

        EW = EW_entrada.somaAreas - EW_saida.somaAreas;
        EW /= EW_entrada.numEventos;

        EN = EN_s.somaAreas / tempoAtual;

        lambda = EW_entrada.numEventos / tempoAtual;
        ocupacao = tempoOcupacao / tempoAtual;
        System.out.println("porcentagem de pacotes que sairam: " + numDePacotesQueSairam / EW_entrada.numEventos);
        System.out.println("Pacotes que sairam: " + numDePacotesQueSairam);
        System.out.println("Pacotes que entraram: " + pacotesRecebidos);
        System.out.println("Ocupação: " + (ocupacao));
        System.out.println("E[W] little: " + ((EN / lambda)));
        System.out.println("e[W]: " + (this.EW));
        System.out.println("e[N]: " + (this.EN));
        System.out.println("Validação : " + (EN-EW*lambda));
        System.out.println("Num de pacotes: " + EW_entrada.numEventos);
        System.out.println("lambda " + this.lambda);
        System.out.println("Vazão " + ((soma_pct_vazao / tempoAtual) * unidade));
        System.out.println("Vazão útil " + ((soma_pct_util / tempoAtual) * unidade));
    }

    public boolean saidaEsperada() {
        return probSaidaEsperada > Math.random();
    }

    public boolean filaCheia() {
        return fila >= tamanhoMaxFila;
    }
}

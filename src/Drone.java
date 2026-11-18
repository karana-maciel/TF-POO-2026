public class Drone extends Robo {

    private int altitudeMaxima;

    public Drone(int id, String nome, int nivelBateria, int altitudeMaxima) {
        super(id, nome, nivelBateria);

        if (altitudeMaxima <= 0) {
            throw new IllegalArgumentException("A altitude máxima deve ser positiva.");
        }

        this.altitudeMaxima = altitudeMaxima;
    }

    public int getAltitudeMaxima() {
        return altitudeMaxima;
    }

    @Override
    public boolean podeExecutar(TipoMissao tipo) {
        return tipo == TipoMissao.RECONHECIMENTO
                || tipo == TipoMissao.LOCALIZACAO_PESSOAS;
    }

    @Override
    public int calcularConsumo(TipoMissao tipo) {
        if (tipo == TipoMissao.RECONHECIMENTO) {
            return 15;
        } else if (tipo == TipoMissao.LOCALIZACAO_PESSOAS) {
            return 25;
        }

        throw new IllegalArgumentException("Missão incompatível com o drone.");
    }

    @Override
    public void executarMissao(Missao missao) {
        if (missao == null) {
            throw new IllegalArgumentException("A missão não pode ser nula.");
        }

        if (missao.getRoboResponsavel() != this) {
            throw new IllegalStateException("O drone não está atribuído a esta missão.");
        }

        if (missao.getStatus() != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("A missão não está em andamento.");
        }

        TipoMissao tipo = missao.getTipo();

        if (!podeExecutar(tipo)) {
            throw new IllegalArgumentException("Missão incompatível com o drone.");
        }

        int consumo = calcularConsumo(tipo);

        if (getNivelBateria() < consumo) {
            throw new IllegalStateException("Bateria insuficiente para executar a missão.");
        }

        consumirBateria(consumo);

        if (tipo == TipoMissao.RECONHECIMENTO) {
            missao.registrarResultado("Reconhecimento da área realizado com sucesso.");
        } else {
            missao.registrarResultado("Busca de pessoas realizada com sucesso.");
        }

        enviarMensagem("Missão executada. Bateria restante: " + getNivelBateria() + "%");
    }

    @Override
    public void exibirResumo() {
        super.exibirResumo();
        System.out.println("Altitude máxima: " + altitudeMaxima + " metros");
    }
}
import java.io.Serializable;

public class RoboTerrestre extends Robo {
    private static final long serialVersionUID = 1L;

    private double capacidadeCarga;

    public RoboTerrestre(int id, String nome, int nivelBateria, double capacidadeCarga) {
        super(id, nome, nivelBateria);
        if (!Double.isFinite(capacidadeCarga) || capacidadeCarga <= 0) {
            throw new DadosInvalidosException("A capacidade de carga deve ser positiva.");
        }
        this.capacidadeCarga = capacidadeCarga;
    }

    public double getCapacidadeCarga() {
        return capacidadeCarga;
    }

    @Override
    public boolean podeExecutar(TipoMissao tipo) {
        return tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS;
    }

    @Override
    public int calcularConsumo(TipoMissao tipo) {
        if (tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS) {
            return 30;
        }
        throw new RegraNegocioException("Missão incompatível com o robô terrestre.");
    }

    @Override
    public void executarMissao(Missao missao) {
        if (missao == null) {
            throw new DadosInvalidosException("A missão não pode ser nula.");
        }
        if (missao.getRoboResponsavel() != this) {
            throw new RegraNegocioException("O robô não está atribuído a esta missão.");
        }
        if (missao.getStatus() != StatusMissao.EM_ANDAMENTO) {
            throw new RegraNegocioException("A missão não está em andamento.");
        }
        if (!podeExecutar(missao.getTipo())) {
            throw new RegraNegocioException("Missão incompatível com o robô terrestre.");
        }
        if (missao.getArea().getTipoTerreno() == TipoTerreno.ALAGADO) {
            throw new RegraNegocioException("O robô terrestre não pode atuar em terreno alagado.");
        }
        if (missao.getPesoSuprimentos() > capacidadeCarga) {
            throw new RegraNegocioException("A carga excede a capacidade deste robô.");
        }

        int consumo = calcularConsumo(missao.getTipo());
        if (getNivelBateria() < consumo) {
            throw new RegraNegocioException("Bateria insuficiente para executar a missão.");
        }

        consumirBateria(consumo);
        missao.registrarResultado("Transporte de " + missao.getPesoSuprimentos()
                + " kg de suprimentos realizado com sucesso.");
        enviarMensagem("Missão executada. Bateria restante: " + getNivelBateria() + "%");
    }

    @Override
    public void exibirResumo() {
        super.exibirResumo();
        System.out.println("Capacidade de carga: " + capacidadeCarga + " kg");
    }
}

import java.io.Serializable;

public class Missao implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;
    private TipoMissao tipo;
    private AreaOperacao area;
    private PrioridadeMissao prioridade;
    private StatusMissao status;
    private Robo roboResponsavel;
    private String resultado;
    private double pesoSuprimentos;

    /**
     * Construtor compatível com o projeto original.
     * Para missões de transporte, utiliza 1 kg como peso padrão.
     * O menu deve preferir o construtor que recebe o peso informado.
     */
    public Missao(int id, String descricao, TipoMissao tipo,
                  AreaOperacao area, PrioridadeMissao prioridade) {
        this(id, descricao, tipo, area, prioridade,
                tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS ? 1.0 : 0.0);
    }

    public Missao(int id, String descricao, TipoMissao tipo,
                  AreaOperacao area, PrioridadeMissao prioridade,
                  double pesoSuprimentos) {
        if (id <= 0) {
            throw new DadosInvalidosException("O ID da missão deve ser positivo.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new DadosInvalidosException("A descrição da missão não pode ser vazia.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("O tipo da missão deve ser informado.");
        }
        if (area == null) {
            throw new DadosInvalidosException("A missão deve estar associada a uma área.");
        }
        if (prioridade == null) {
            throw new DadosInvalidosException("A prioridade deve ser informada.");
        }
        if (!Double.isFinite(pesoSuprimentos) || pesoSuprimentos < 0) {
            throw new DadosInvalidosException("O peso dos suprimentos deve ser um número válido e não negativo.");
        }
        if (tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS && pesoSuprimentos <= 0) {
            throw new DadosInvalidosException("Informe um peso maior que zero para o transporte.");
        }

        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo;
        this.area = area;
        this.prioridade = prioridade;
        this.status = StatusMissao.PENDENTE;
        this.roboResponsavel = null;
        this.resultado = null;
        this.pesoSuprimentos = pesoSuprimentos;
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoMissao getTipo() {
        return tipo;
    }

    public AreaOperacao getArea() {
        return area;
    }

    public PrioridadeMissao getPrioridade() {
        return prioridade;
    }

    public StatusMissao getStatus() {
        return status;
    }

    public Robo getRoboResponsavel() {
        return roboResponsavel;
    }

    public String getResultado() {
        return resultado;
    }

    public double getPesoSuprimentos() {
        return pesoSuprimentos;
    }

    public void atribuirRobo(Robo robo) {
        if (robo == null) {
            throw new DadosInvalidosException("O robô não pode ser nulo.");
        }
        if (status != StatusMissao.PENDENTE) {
            throw new RegraNegocioException("Só é possível atribuir robô a uma missão pendente.");
        }
        if (roboResponsavel != null) {
            throw new RegraNegocioException("A missão " + id + " já possui um robô atribuído.");
        }
        if (!robo.verificarDisponibilidade()) {
            throw new RegraNegocioException("O robô " + robo.getNome() + " não está disponível.");
        }
        if (!robo.podeExecutar(tipo)) {
            throw new RegraNegocioException("Esse tipo de robô não é compatível com a missão.");
        }
        if (robo.getNivelBateria() < robo.calcularConsumo(tipo)) {
            throw new RegraNegocioException("O robô não possui bateria suficiente.");
        }

        if (robo instanceof RoboTerrestre) {
            RoboTerrestre terrestre = (RoboTerrestre) robo;
            if (area.getTipoTerreno() == TipoTerreno.ALAGADO) {
                throw new RegraNegocioException("O robô terrestre não pode atuar em terreno alagado.");
            }
            if (terrestre.getCapacidadeCarga() < pesoSuprimentos) {
                throw new RegraNegocioException("A carga excede a capacidade do robô terrestre.");
            }
        }

        this.roboResponsavel = robo;
        robo.alterarStatus(StatusRobo.EM_MISSAO);
        robo.receberMensagem("Você foi designado para a missão " + id + ": " + descricao);
    }

    public void iniciar() {
        if (status == StatusMissao.CONCLUIDA || status == StatusMissao.CANCELADA) {
            throw new RegraNegocioException("A missão " + id + " está " + status
                    + " e não pode ser iniciada novamente.");
        }
        if (status == StatusMissao.EM_ANDAMENTO) {
            throw new RegraNegocioException("A missão " + id + " já está em andamento.");
        }
        if (roboResponsavel == null) {
            throw new RegraNegocioException("A missão só pode ser iniciada após a atribuição de um robô.");
        }
        status = StatusMissao.EM_ANDAMENTO;
    }

    public void concluir() {
        if (status != StatusMissao.EM_ANDAMENTO) {
            throw new RegraNegocioException("Só é possível concluir uma missão em andamento.");
        }

        roboResponsavel.executarMissao(this);

        if (resultado == null) {
            resultado = "Missão concluída por " + roboResponsavel.getNome() + ".";
        }

        status = StatusMissao.CONCLUIDA;
        roboResponsavel.registrarMissaoRealizada();
        liberarRobo();
    }

    public void cancelar() {
        if (status == StatusMissao.CONCLUIDA || status == StatusMissao.CANCELADA) {
            throw new RegraNegocioException("A missão " + id + " já está " + status + ".");
        }

        status = StatusMissao.CANCELADA;
        resultado = "Missão cancelada.";

        if (roboResponsavel != null) {
            liberarRobo();
        }
    }

    public void registrarResultado(String resultado) {
        if (status != StatusMissao.EM_ANDAMENTO) {
            throw new RegraNegocioException("Só é possível registrar resultado durante uma missão em andamento.");
        }
        if (resultado == null || resultado.isBlank()) {
            throw new DadosInvalidosException("O resultado não pode ser vazio.");
        }
        this.resultado = resultado;
    }

    private void liberarRobo() {
        if (roboResponsavel.getStatus() == StatusRobo.EM_MISSAO) {
            if (roboResponsavel.getNivelBateria() > 0) {
                roboResponsavel.alterarStatus(StatusRobo.DISPONIVEL);
            } else {
                roboResponsavel.alterarStatus(StatusRobo.SEM_BATERIA);
            }
        }
    }

    public void exibirResumo() {
        System.out.println("ID: " + id);
        System.out.println("Descrição: " + descricao);
        System.out.println("Tipo: " + tipo);
        System.out.println("Área: " + area.getNome());
        System.out.println("Prioridade: " + prioridade);
        if (tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS) {
            System.out.println("Peso dos suprimentos: " + pesoSuprimentos + " kg");
        }
        System.out.println("Status: " + status);
        System.out.println("Robô responsável: "
                + (roboResponsavel != null ? roboResponsavel.getNome() : "nenhum"));
        System.out.println("Resultado: " + (resultado != null ? resultado : "-"));
    }
}

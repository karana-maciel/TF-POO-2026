import java.io.Serializable;

public class Missao implements Serializable {

    private int id;
    private String descricao;
    private TipoMissao tipo;
    private AreaOperacao area;
    private PrioridadeMissao prioridade;
    private StatusMissao status;
    private Robo roboResponsavel;
    private String resultado;

    public Missao(int id, String descricao, TipoMissao tipo, AreaOperacao area, PrioridadeMissao prioridade) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID da missão deve ser positivo.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da missão não pode ser vazia.");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da missão deve ser informado.");
        }

        if (area == null) {
            throw new IllegalArgumentException("A missão deve estar associada a uma área de operação.");
        }

        if (prioridade == null) {
            throw new IllegalArgumentException("A prioridade da missão deve ser informada.");
        }

        this.id = id;
        this.descricao = descricao;
        this.tipo = tipo;
        this.area = area;
        this.prioridade = prioridade;
        this.status = StatusMissao.PENDENTE;
        this.roboResponsavel = null;
        this.resultado = null;
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

    public void atribuirRobo(Robo robo) {
        if (robo == null) {
            throw new IllegalArgumentException("O robô não pode ser nulo.");
        }

        if (status != StatusMissao.PENDENTE) {
            throw new IllegalStateException("Só é possível atribuir um robô a uma missão pendente.");
        }

        if (roboResponsavel != null) {
            throw new IllegalStateException("A missão " + id + " já possui um robô atribuído.");
        }

        if (!robo.verificarDisponibilidade()) {
            throw new IllegalStateException("O robô " + robo.getNome() + " não está disponível.");
        }

        this.roboResponsavel = robo;
        robo.alterarStatus(StatusRobo.EM_MISSAO);
        robo.receberMensagem("Você foi designado para a missão " + id + ": " + descricao);
    }

    public void iniciar() {
        if (status == StatusMissao.CONCLUIDA || status == StatusMissao.CANCELADA) {
            throw new IllegalStateException("A missão " + id + " está " + status + " e não pode ser iniciada novamente.");
        }

        if (status == StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("A missão " + id + " já está em andamento.");
        }

        if (roboResponsavel == null) {
            throw new IllegalStateException("A missão só pode ser iniciada após a atribuição de um robô.");
        }

        status = StatusMissao.EM_ANDAMENTO;
    }

    public void concluir() {
        if (status != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("Só é possível concluir uma missão em andamento.");
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
            throw new IllegalStateException("A missão " + id + " já está " + status + ".");
        }

        status = StatusMissao.CANCELADA;
        resultado = "Missão cancelada.";

        if (roboResponsavel != null) {
            liberarRobo();
        }
    }

    public void registrarResultado(String resultado) {
        if (status != StatusMissao.EM_ANDAMENTO) {
            throw new IllegalStateException("Só é possível registrar resultado de uma missão em andamento.");
        }

        if (resultado == null || resultado.isBlank()) {
            throw new IllegalArgumentException("O resultado não pode ser vazio.");
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
        System.out.println("Status: " + status);
        System.out.println("Robô responsável: " + (roboResponsavel != null ? roboResponsavel.getNome() : "nenhum"));
        System.out.println("Resultado: " + (resultado != null ? resultado : "-"));
    }
}
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Robo implements Comunicavel, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private int nivelBateria;
    private StatusRobo status;
    private int missoesRealizadas;
    private List<String> historicoMensagens;

    public Robo(int id, String nome, int nivelBateria) {
        if (id <= 0) {
            throw new DadosInvalidosException("O ID do robô deve ser positivo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do robô não pode ser vazio.");
        }
        if (nivelBateria < 0 || nivelBateria > 100) {
            throw new DadosInvalidosException("A bateria deve estar entre 0% e 100%.");
        }

        this.id = id;
        this.nome = nome;
        this.nivelBateria = nivelBateria;
        this.missoesRealizadas = 0;
        this.historicoMensagens = new ArrayList<>();
        this.status = (nivelBateria == 0) ? StatusRobo.SEM_BATERIA : StatusRobo.DISPONIVEL;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getNivelBateria() {
        return nivelBateria;
    }

    public StatusRobo getStatus() {
        return status;
    }

    public int getMissoesRealizadas() {
        return missoesRealizadas;
    }

    public List<String> getHistoricoMensagens() {
        return Collections.unmodifiableList(historicoMensagens);
    }

    public boolean verificarDisponibilidade() {
        return status == StatusRobo.DISPONIVEL && nivelBateria > 0;
    }

    public int consultarBateria() {
        return nivelBateria;
    }

    public void recarregar() {
        if (status == StatusRobo.EM_MISSAO) {
            throw new RegraNegocioException("O robô não pode ser recarregado durante uma missão.");
        }

        nivelBateria = 100;
        if (status == StatusRobo.SEM_BATERIA) {
            status = StatusRobo.DISPONIVEL;
        }
        registrarMensagem("Sistema: bateria recarregada para 100%.");
    }

    public void colocarEmManutencao() {
        if (status == StatusRobo.EM_MISSAO) {
            throw new RegraNegocioException("Não é possível colocar em manutenção durante uma missão.");
        }
        if (status == StatusRobo.EM_MANUTENCAO) {
            throw new RegraNegocioException("O robô já está em manutenção.");
        }
        status = StatusRobo.EM_MANUTENCAO;
        registrarMensagem("Sistema: robô encaminhado para manutenção.");
    }

    public void finalizarManutencao() {
        if (status != StatusRobo.EM_MANUTENCAO) {
            throw new RegraNegocioException("O robô não está em manutenção.");
        }
        status = (nivelBateria > 0) ? StatusRobo.DISPONIVEL : StatusRobo.SEM_BATERIA;
        registrarMensagem("Sistema: manutenção finalizada.");
    }

    protected void consumirBateria(int consumo) {
        if (consumo < 0 || consumo > nivelBateria) {
            throw new RegraNegocioException("Consumo de bateria inválido ou superior à bateria disponível.");
        }

        nivelBateria -= consumo;
        if (nivelBateria == 0) {
            status = StatusRobo.SEM_BATERIA;
        }
    }

    protected void alterarStatus(StatusRobo status) {
        if (status == null) {
            throw new DadosInvalidosException("O status não pode ser nulo.");
        }
        if (status == StatusRobo.DISPONIVEL && nivelBateria == 0) {
            throw new RegraNegocioException("O robô está sem bateria.");
        }
        this.status = status;
    }

    protected void registrarMissaoRealizada() {
        missoesRealizadas++;
    }

    private void registrarMensagem(String mensagem) {
        historicoMensagens.add(java.time.LocalDateTime.now() + " - " + mensagem);
    }

    public void exibirResumo() {
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Bateria: " + nivelBateria + "%");
        System.out.println("Status: " + status);
        System.out.println("Missões realizadas: " + missoesRealizadas);
    }

    @Override
    public void enviarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new DadosInvalidosException("A mensagem não pode ser vazia.");
        }
        registrarMensagem("ENVIADA: " + mensagem);
        System.out.println(nome + " enviou: " + mensagem);
    }

    @Override
    public void receberMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new DadosInvalidosException("A mensagem não pode ser vazia.");
        }
        registrarMensagem("RECEBIDA: " + mensagem);
        System.out.println(nome + " recebeu: " + mensagem);
    }

    public abstract boolean podeExecutar(TipoMissao tipo);

    public abstract int calcularConsumo(TipoMissao tipo);

    public abstract void executarMissao(Missao missao);
}

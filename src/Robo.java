public abstract class Robo implements Comunicavel {

    private int id;
    private String nome;
    private int nivelBateria;
    private StatusRobo status;
    private int missoesRealizadas;

    public Robo(int id, String nome, int nivelBateria) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID do robô deve ser positivo.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do robô não pode ser vazio.");
        }

        if (nivelBateria < 0 || nivelBateria > 100) {
            throw new IllegalArgumentException("O nível de bateria do robô deve estar entre 0% e 100%.");
        }

        this.id = id;
        this.nome = nome;
        this.nivelBateria = nivelBateria;
        this.missoesRealizadas = 0;

        if (nivelBateria == 0) {
            this.status = StatusRobo.SEM_BATERIA;
        } else {
            this.status = StatusRobo.DISPONIVEL;
        }
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

    public boolean verificarDisponibilidade() {
        return status == StatusRobo.DISPONIVEL && nivelBateria > 0;
    }

    public int consultarBateria() {
        return nivelBateria;
    }

    public void recarregar() {
        if (status == StatusRobo.EM_MISSAO) {
            throw new IllegalStateException("O robô não pode ser recarregado durante uma missão.");
        }

        nivelBateria = 100;

        if (status == StatusRobo.SEM_BATERIA) {
            status = StatusRobo.DISPONIVEL;
        }
    }

    protected void consumirBateria(int consumo) {
        if (consumo < 0 || consumo > nivelBateria) {
            throw new IllegalArgumentException("Consumo de bateria inválido.");
        }

        nivelBateria -= consumo;

        if (nivelBateria == 0) {
            status = StatusRobo.SEM_BATERIA;
        }
    }

    protected void alterarStatus(StatusRobo status) {
        if (status == null) {
            throw new IllegalArgumentException("O status não pode ser nulo.");
        }

        if (status == StatusRobo.DISPONIVEL && nivelBateria == 0) {
            throw new IllegalStateException("O robô esta sem bateria.");
        }

        this.status = status;
    }

    protected void registrarMissaoRealizada() {
        missoesRealizadas++;
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
        System.out.println(nome + " enviou: " + mensagem);
    }

    @Override
    public void receberMensagem(String mensagem) {
        System.out.println(nome + " recebeu: " + mensagem);
    }

    public abstract void executarMissao(Missao missao);
}

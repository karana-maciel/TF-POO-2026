public class AreaOperacao {
    private int id;
    private String nome;
    private String localizacao;
    private TipoTerreno tipoTerreno;
    private NivelRisco nivelRisco;
    private String descricao;

    public AreaOperacao(int id, String nome, String localizacao, TipoTerreno tipoTerreno, NivelRisco nivelRisco, String descricao) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID da área de operação deve ser positivo.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da área não pode ser vazio.");
        }

        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("A localização não pode ser vazia.");
        }

        if (tipoTerreno == null) {
            throw new IllegalArgumentException("O tipo de terreno deve ser informado.");
        }

        if (nivelRisco == null) {
            throw new IllegalArgumentException("O nível de risco deve ser informado.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode ser vazia.");
        }

        this.id = id;
        this.nome = nome;
        this.localizacao = localizacao;
        this.tipoTerreno = tipoTerreno;
        this.nivelRisco = nivelRisco;
        this.descricao = descricao;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public TipoTerreno getTipoTerreno() {
        return tipoTerreno;
    }

    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void atualizarNivelRisco(NivelRisco novoNivelRisco) {
        if (novoNivelRisco == null) {
            throw new IllegalArgumentException("O nível de risco não pode ser nulo.");
        }

        this.nivelRisco = novoNivelRisco;
    }

    public void exibirInformacoes() {
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Localização: " + localizacao);
        System.out.println("Tipo de terreno: " + tipoTerreno);
        System.out.println("Nível de risco: " + nivelRisco);
        System.out.println("Descrição: " + descricao);
    }
}

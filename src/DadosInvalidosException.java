/** Exceção para dados informados que não respeitam o formato ou os limites do sistema. */
public class DadosInvalidosException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}

/** Exceção para operações que violam uma regra de funcionamento do sistema. */
public class RegraNegocioException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

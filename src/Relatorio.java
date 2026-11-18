import java.time.LocalDateTime;
import java.util.List;

public class Relatorio {

    private LocalDateTime dataGeracao;
    private int totalRobos;
    private int totalMissoes;
    private long missoesConcluidas;
    private long missoesPendentes;

    public Relatorio(List<Robo> robos, List<Missao> missoes) {
        this.dataGeracao = LocalDateTime.now();
        this.totalRobos = robos.size();
        this.totalMissoes = missoes.size();

        this.missoesConcluidas = missoes.stream()
                .filter(m -> m.getStatus() == StatusMissao.CONCLUIDA)
                .count();

        this.missoesPendentes = missoes.stream()
                .filter(m -> m.getStatus() == StatusMissao.PENDENTE)
                .count();
    }

    public String gerarResumo() {
        return "RELATÓRIO GERAL\n"
                + "Data: " + dataGeracao + "\n"
                + "Total de robôs: " + totalRobos + "\n"
                + "Total de missões: " + totalMissoes + "\n"
                + "Missões concluídas: " + missoesConcluidas + "\n"
                + "Missões pendentes: " + missoesPendentes + "\n";
    }

    public void salvarEmArquivo() {
        //pra pessoa 3 fazer
    }
}
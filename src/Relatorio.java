import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Relatorio {
    private LocalDateTime dataGeracao;
    private int totalRobos;
    private int totalMissoes;
    private long missoesConcluidas;
    private long missoesPendentes;

    public Relatorio(List<Robo> robos, List<Missao> missoes) {
        if (robos == null || missoes == null) {
            throw new DadosInvalidosException("As listas de robôs e missões não podem ser nulas.");
        }

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
        return "========== RELATÓRIO GERAL ==========\n"
                + "Data de geração: " + dataGeracao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n"
                + "Total de robôs: " + totalRobos + "\n"
                + "Total de missões: " + totalMissoes + "\n"
                + "Missões concluídas: " + missoesConcluidas + "\n"
                + "Missões pendentes: " + missoesPendentes + "\n"
                + "=====================================\n";
    }

    /**
     * Salva o relatório em relatorios/ com data e hora no nome.
     */
    public void salvarEmArquivo() {
        Path pasta = Path.of("relatorios");
        String instante = dataGeracao.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        Path arquivo = pasta.resolve("relatorio-" + instante + ".txt");

        try {
            Files.createDirectories(pasta);
            Files.writeString(arquivo, gerarResumo(), StandardCharsets.UTF_8);
            System.out.println("Relatório salvo em: " + arquivo.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o relatório: " + e.getMessage(), e);
        }
    }
}

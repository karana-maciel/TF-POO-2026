import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

public class CentralResgate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private Map<Integer, Robo> robos;
    private List<AreaOperacao> areas;
    private List<Missao> missoes;
    private Queue<Missao> missoesPendentes;

    public CentralResgate(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome da central não pode ser vazio.");
        }

        this.nome = nome;
        this.robos = new HashMap<>();
        this.areas = new ArrayList<>();
        this.missoes = new ArrayList<>();
        this.missoesPendentes = new PriorityQueue<>(new ComparadorMissao());
    }

    public String getNome() {
        return nome;
    }

    public List<Robo> getRobos() {
        return new ArrayList<>(robos.values());
    }

    public List<AreaOperacao> getAreas() {
        return new ArrayList<>(areas);
    }

    public List<Missao> getMissoes() {
        return new ArrayList<>(missoes);
    }

    public void cadastrarRobo(Robo robo) {
        if (robo == null) {
            throw new DadosInvalidosException("O robô não pode ser nulo.");
        }
        if (robos.containsKey(robo.getId())) {
            throw new RegraNegocioException("Já existe um robô com o ID " + robo.getId() + ".");
        }
        robos.put(robo.getId(), robo);
    }

    public void cadastrarArea(AreaOperacao area) {
        if (area == null) {
            throw new DadosInvalidosException("A área não pode ser nula.");
        }
        boolean idRepetido = areas.stream().anyMatch(a -> a.getId() == area.getId());
        if (idRepetido) {
            throw new RegraNegocioException("Já existe uma área com o ID " + area.getId() + ".");
        }
        areas.add(area);
    }

    public void registrarMissao(Missao missao) {
        if (missao == null) {
            throw new DadosInvalidosException("A missão não pode ser nula.");
        }
        boolean idRepetido = missoes.stream().anyMatch(m -> m.getId() == missao.getId());
        if (idRepetido) {
            throw new RegraNegocioException("Já existe uma missão com o ID " + missao.getId() + ".");
        }
        if (areas.stream().noneMatch(a -> a.getId() == missao.getArea().getId())) {
            throw new RegraNegocioException("A área da missão não está cadastrada na central.");
        }

        // Padroniza a referência da missão com a área efetivamente cadastrada.
        AreaOperacao areaCadastrada = buscarArea(missao.getArea().getId());
        if (areaCadastrada != missao.getArea()) {
            throw new RegraNegocioException("A missão deve utilizar a própria área cadastrada na central.");
        }

        missoes.add(missao);
        if (missao.getStatus() == StatusMissao.PENDENTE && missao.getRoboResponsavel() == null) {
            missoesPendentes.add(missao);
        }
    }

    public Robo buscarRobo(int id) {
        Robo robo = robos.get(id);
        if (robo == null) {
            throw new RegraNegocioException("Não existe robô com o ID " + id + ".");
        }
        return robo;
    }

    public AreaOperacao buscarArea(int id) {
        for (AreaOperacao area : areas) {
            if (area.getId() == id) {
                return area;
            }
        }
        throw new RegraNegocioException("Não existe área com o ID " + id + ".");
    }

    public Missao buscarMissao(int id) {
        for (Missao missao : missoes) {
            if (missao.getId() == id) {
                return missao;
            }
        }
        throw new RegraNegocioException("Não existe missão com o ID " + id + ".");
    }

    public List<Robo> listarRobosDisponiveis() {
        return robos.values().stream()
                .filter(Robo::verificarDisponibilidade)
                .sorted(Comparator.comparingInt(Robo::getId))
                .collect(Collectors.toList());
    }

    public List<Missao> listarMissoesPendentes() {
        // O stream ordena a cópia sem alterar a fila original.
        return missoesPendentes.stream()
                .sorted(new ComparadorMissao())
                .collect(Collectors.toList());
    }

    public boolean atribuirRoboMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);

        if (missao.getStatus() != StatusMissao.PENDENTE) {
            throw new RegraNegocioException("Só é possível atribuir robô a uma missão pendente.");
        }
        if (missao.getRoboResponsavel() != null) {
            throw new RegraNegocioException("A missão " + idMissao + " já possui um robô atribuído.");
        }

        List<Robo> candidatos = robos.values().stream()
                .filter(Robo::verificarDisponibilidade)
                .filter(robo -> isCompativel(robo, missao))
                .filter(robo -> robo.getNivelBateria() >= robo.calcularConsumo(missao.getTipo()))
                .sorted(Comparator.comparingInt(Robo::getNivelBateria).reversed()
                        .thenComparingInt(Robo::getId))
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            System.out.println("Nenhum robô disponível, compatível e com bateria suficiente.");
            System.out.println("A missão " + idMissao + " continua pendente.");
            return false;
        }

        Robo roboEscolhido = candidatos.get(0);
        missao.atribuirRobo(roboEscolhido);
        missoesPendentes.remove(missao);

        System.out.println("Central: robô " + roboEscolhido.getNome()
                + " atribuído à missão " + idMissao + ".");
        return true;
    }

    public void executarMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);
        Robo robo = missao.getRoboResponsavel();

        missao.iniciar();
        missao.concluir();

        if (robo != null) {
            robo.enviarMensagem("Missão " + idMissao + " concluída. Resultado: " + missao.getResultado());
        }
    }

    public void cancelarMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);
        missao.cancelar();
        missoesPendentes.remove(missao);
    }

    public void atenderMissoesPendentes() {
        for (Missao missao : listarMissoesPendentes()) {
            if (missao.getStatus() == StatusMissao.PENDENTE && missao.getRoboResponsavel() == null) {
                atribuirRoboMissao(missao.getId());
            }
        }
    }

    public void atualizarNivelRiscoArea(int idArea, NivelRisco novoNivel) {
        AreaOperacao area = buscarArea(idArea);
        area.atualizarNivelRisco(novoNivel);

        // Como o risco participa da prioridade, é necessário reconstruir o heap.
        List<Missao> pendentes = new ArrayList<>(missoesPendentes);
        missoesPendentes.clear();
        missoesPendentes.addAll(pendentes);
    }

    public Relatorio gerarRelatorioGeral() {
        return new Relatorio(getRobos(), getMissoes());
    }

    private boolean isCompativel(Robo robo, Missao missao) {
        if (!robo.podeExecutar(missao.getTipo())) {
            return false;
        }

        if (robo instanceof RoboTerrestre) {
            RoboTerrestre terrestre = (RoboTerrestre) robo;
            return missao.getTipo() == TipoMissao.TRANSPORTE_SUPRIMENTOS
                    && missao.getArea().getTipoTerreno() != TipoTerreno.ALAGADO
                    && terrestre.getCapacidadeCarga() >= missao.getPesoSuprimentos();
        }

        return robo instanceof Drone
                && (missao.getTipo() == TipoMissao.RECONHECIMENTO
                || missao.getTipo() == TipoMissao.LOCALIZACAO_PESSOAS);
    }

    /**
     * Prioriza: prioridade da missão (ALTA primeiro), risco (ALTO primeiro)
     * e, em caso de empate, menor identificador.
     * A classe implementa Serializable porque a PriorityQueue é persistida.
     */
    private static class ComparadorMissao implements Comparator<Missao>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Missao a, Missao b) {
            int comparacao = Integer.compare(b.getPrioridade().ordinal(), a.getPrioridade().ordinal());
            if (comparacao == 0) {
                comparacao = Integer.compare(b.getArea().getNivelRisco().ordinal(),
                        a.getArea().getNivelRisco().ordinal());
            }
            if (comparacao == 0) {
                comparacao = Integer.compare(a.getId(), b.getId());
            }
            return comparacao;
        }
    }
}

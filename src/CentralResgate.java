import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CentralResgate implements Serializable {

    private static final int BATERIA_MINIMA = 20;

    private String nome;
    private Map<Integer, Robo> robos;
    private List<AreaOperacao> areas;
    private List<Missao> missoes;
    private Queue<Missao> missoesPendentes;

    public CentralResgate(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da central não pode ser vazio.");
        }

        this.nome = nome;
        this.robos = new HashMap<>();
        this.areas = new ArrayList<>();
        this.missoes = new ArrayList<>();
        this.missoesPendentes = new LinkedList<>();
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

    // cadastros

    public void cadastrarRobo(Robo robo) {
        if (robo == null) {
            throw new IllegalArgumentException("O robô não pode ser nulo.");
        }

        if (robos.containsKey(robo.getId())) {
            throw new IllegalArgumentException("Já existe um robô com o ID " + robo.getId() + ".");
        }

        robos.put(robo.getId(), robo);
    }

    public void cadastrarArea(AreaOperacao area) {
        if (area == null) {
            throw new IllegalArgumentException("A área não pode ser nula.");
        }

        boolean idRepetido = areas.stream().anyMatch(a -> a.getId() == area.getId());
        if (idRepetido) {
            throw new IllegalArgumentException("Já existe uma área com o ID " + area.getId() + ".");
        }

        areas.add(area);
    }

    public void registrarMissao(Missao missao) {
        if (missao == null) {
            throw new IllegalArgumentException("A missão não pode ser nula.");
        }

        boolean idRepetido = missoes.stream().anyMatch(m -> m.getId() == missao.getId());
        if (idRepetido) {
            throw new IllegalArgumentException("Já existe uma missão com o ID " + missao.getId() + ".");
        }

        if (!areas.contains(missao.getArea())) {
            throw new IllegalArgumentException("A área da missão não está cadastrada na central.");
        }

        missoes.add(missao);
        missoesPendentes.add(missao);
    }

    // buscar

    public Robo buscarRobo(int id) {
        Robo robo = robos.get(id);
        if (robo == null) {
            throw new IllegalArgumentException("Não existe robô com o ID " + id + ".");
        }
        return robo;
    }

    public AreaOperacao buscarArea(int id) {
        for (AreaOperacao area : areas) {
            if (area.getId() == id) {
                return area;
            }
        }
        throw new IllegalArgumentException("Não existe área com o ID " + id + ".");
    }

    public Missao buscarMissao(int id) {
        for (Missao missao : missoes) {
            if (missao.getId() == id) {
                return missao;
            }
        }
        throw new IllegalArgumentException("Não existe missão com o ID " + id + ".");
    }

    // listas

    public List<Robo> listarRobosDisponiveis() {
        return robos.values().stream()
                .filter(r -> r.verificarDisponibilidade())
                .toList();
    }

    public List<Missao> listarMissoesPendentes() {
        return new ArrayList<>(missoesPendentes);
    }

    //operacoes

    public boolean atribuirRoboMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);

        if (missao.getStatus() != StatusMissao.PENDENTE) {
            throw new IllegalStateException("Só é possível atribuir robô a uma missão pendente.");
        }

        if (missao.getRoboResponsavel() != null) {
            throw new IllegalStateException("A missão " + idMissao + " já possui um robô atribuído.");
        }

        List<Robo> candidatos = robos.values().stream()
                .filter(r -> r.verificarDisponibilidade())
                .filter(r -> isCompativel(r, missao))
                .filter(r -> r.getNivelBateria() >= BATERIA_MINIMA)
                .toList();

        if (candidatos.isEmpty()) {
            System.out.println("Nenhum robô disponível e compatível. A missão " + idMissao + " continua pendente.");
            return false;
        }

        Robo robo = candidatos.get(0);
        for (Robo r : candidatos) {
            if (r.getNivelBateria() > robo.getNivelBateria()) {
                robo = r;
            }
        }

        missao.atribuirRobo(robo);
        missoesPendentes.remove(missao);
        System.out.println("Central: robô " + robo.getNome() + " atribuído à missão " + idMissao + ".");
        return true;
    }

    public void executarMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);

        missao.iniciar();
        missao.concluir();

        Robo robo = missao.getRoboResponsavel();
        robo.enviarMensagem("Missão " + idMissao + " concluída. Resultado: " + missao.getResultado());
    }

    public void cancelarMissao(int idMissao) {
        Missao missao = buscarMissao(idMissao);
        missao.cancelar();
        missoesPendentes.remove(missao);
    }

    public void atenderMissoesPendentes() {
        for (Missao missao : new ArrayList<>(missoesPendentes)) {
            atribuirRoboMissao(missao.getId());
        }
    }

    public Relatorio gerarRelatorioGeral() {
         return new Relatorio(getRobos(), getMissoes());
    }

    // regras

    private boolean isCompativel(Robo robo, Missao missao) {
        TipoMissao tipo = missao.getTipo();

        if (robo instanceof Drone) {
            return tipo == TipoMissao.RECONHECIMENTO || tipo == TipoMissao.LOCALIZACAO_PESSOAS;
        }

        if (robo instanceof RoboTerrestre) {
            return tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS
                    && missao.getArea().getTipoTerreno() != TipoTerreno.ALAGADO;
        }

        return false;
    }
}
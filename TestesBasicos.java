import java.nio.file.Files;
import java.nio.file.Path;

public class TestesBasicos {

    public static void main(String[] args) throws Exception {
        int total = 0;

        testeExecucaoValida();
        total++;
        testePesoMaiorQueCapacidade();
        total++;
        testeBateriaInsuficiente();
        total++;
        testeFilaPrioritaria();
        total++;
        testeManutencao();
        total++;
        testePersistencia();
        total++;

        System.out.println("\nResultado: " + total + " testes aprovados.");
    }

    private static void testeExecucaoValida() {
        CentralResgate central = new CentralResgate("Teste");
        AreaOperacao area = criarArea(1, NivelRisco.BAIXO, TipoTerreno.URBANO);
        central.cadastrarArea(area);
        Drone drone = new Drone(10, "Drone teste", 100, 100);
        central.cadastrarRobo(drone);

        Missao missao = new Missao(1, "Reconhecimento", TipoMissao.RECONHECIMENTO,
                area, PrioridadeMissao.ALTA);
        central.registrarMissao(missao);

        verificar(central.atribuirRoboMissao(1), "Atribuição válida deveria funcionar.");
        central.executarMissao(1);

        verificar(missao.getStatus() == StatusMissao.CONCLUIDA, "Missão deveria ficar concluída.");
        verificar(drone.getNivelBateria() == 85, "Drone deveria consumir 15% de bateria.");
        verificar(drone.getStatus() == StatusRobo.DISPONIVEL, "Drone deveria voltar a ficar disponível.");
        System.out.println("[OK] Execução válida e consumo de bateria.");
    }

    private static void testePesoMaiorQueCapacidade() {
        CentralResgate central = new CentralResgate("Teste");
        AreaOperacao area = criarArea(1, NivelRisco.MEDIO, TipoTerreno.URBANO);
        central.cadastrarArea(area);
        central.cadastrarRobo(new RoboTerrestre(20, "Terrestre pequeno", 100, 10));

        Missao missao = new Missao(2, "Transporte pesado", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                area, PrioridadeMissao.ALTA, 25);
        central.registrarMissao(missao);

        verificar(!central.atribuirRoboMissao(2), "Robô não deve aceitar carga maior que sua capacidade.");
        verificar(missao.getStatus() == StatusMissao.PENDENTE, "Missão deve continuar pendente.");
        System.out.println("[OK] Carga acima da capacidade.");
    }

    private static void testeBateriaInsuficiente() {
        CentralResgate central = new CentralResgate("Teste");
        AreaOperacao area = criarArea(1, NivelRisco.BAIXO, TipoTerreno.URBANO);
        central.cadastrarArea(area);
        central.cadastrarRobo(new Drone(30, "Drone com pouca bateria", 10, 100));

        Missao missao = new Missao(3, "Reconhecimento", TipoMissao.RECONHECIMENTO,
                area, PrioridadeMissao.MEDIA);
        central.registrarMissao(missao);

        verificar(!central.atribuirRoboMissao(3), "Robô sem bateria suficiente não deve ser atribuído.");
        verificar(missao.getStatus() == StatusMissao.PENDENTE, "Missão deve continuar pendente.");
        System.out.println("[OK] Bateria insuficiente.");
    }

    private static void testeFilaPrioritaria() {
        CentralResgate central = new CentralResgate("Teste");
        AreaOperacao baixo = criarArea(1, NivelRisco.BAIXO, TipoTerreno.URBANO);
        AreaOperacao alto = criarArea(2, NivelRisco.ALTO, TipoTerreno.URBANO);
        central.cadastrarArea(baixo);
        central.cadastrarArea(alto);

        central.registrarMissao(new Missao(10, "Prioridade média, risco alto",
                TipoMissao.RECONHECIMENTO, alto, PrioridadeMissao.MEDIA));
        central.registrarMissao(new Missao(11, "Prioridade alta, risco baixo",
                TipoMissao.RECONHECIMENTO, baixo, PrioridadeMissao.ALTA));
        central.registrarMissao(new Missao(12, "Prioridade alta, risco alto",
                TipoMissao.RECONHECIMENTO, alto, PrioridadeMissao.ALTA));

        verificar(central.listarMissoesPendentes().get(0).getId() == 12,
                "A primeira missão deve ter prioridade alta e risco alto.");
        verificar(central.listarMissoesPendentes().get(1).getId() == 11,
                "A segunda missão deve ter prioridade alta e risco baixo.");
        System.out.println("[OK] Ordenação da fila por prioridade e risco.");
    }

    private static void testeManutencao() {
        CentralResgate central = new CentralResgate("Teste");
        AreaOperacao area = criarArea(1, NivelRisco.BAIXO, TipoTerreno.URBANO);
        central.cadastrarArea(area);
        Robo drone = new Drone(40, "Drone em manutenção", 100, 100);
        drone.colocarEmManutencao();
        central.cadastrarRobo(drone);

        Missao missao = new Missao(4, "Reconhecimento", TipoMissao.RECONHECIMENTO,
                area, PrioridadeMissao.ALTA);
        central.registrarMissao(missao);

        verificar(!central.atribuirRoboMissao(4), "Robô em manutenção não pode receber missão.");
        verificar(missao.getStatus() == StatusMissao.PENDENTE, "Missão deve continuar pendente.");
        drone.finalizarManutencao();
        verificar(central.atribuirRoboMissao(4), "Após manutenção, o robô deve poder ser atribuído.");
        System.out.println("[OK] Bloqueio e liberação de manutenção.");
    }

    private static void testePersistencia() throws Exception {
        CentralResgate central = new CentralResgate("Central persistida");
        AreaOperacao area = criarArea(1, NivelRisco.ALTO, TipoTerreno.URBANO);
        central.cadastrarArea(area);
        central.cadastrarRobo(new Drone(50, "Drone salvo", 90, 120));
        central.registrarMissao(new Missao(5, "Missão salva", TipoMissao.RECONHECIMENTO,
                area, PrioridadeMissao.ALTA));

        Path arquivo = Files.createTempFile("central-resgate-", ".dat");
        try {
            PersistenciaCentral.salvar(central, arquivo);
            CentralResgate recuperada = PersistenciaCentral.carregar(arquivo).orElseThrow();

            verificar(recuperada.getRobos().size() == 1, "Robô deveria ser recuperado.");
            verificar(recuperada.getAreas().size() == 1, "Área deveria ser recuperada.");
            verificar(recuperada.getMissoes().size() == 1, "Missão deveria ser recuperada.");
            verificar(recuperada.listarMissoesPendentes().size() == 1,
                    "Fila de pendências deveria ser recuperada.");
        } finally {
            Files.deleteIfExists(arquivo);
        }
        System.out.println("[OK] Persistência e recuperação dos dados.");
    }

    private static AreaOperacao criarArea(int id, NivelRisco risco, TipoTerreno terreno) {
        return new AreaOperacao(id, "Área " + id, "Local " + id,
                terreno, risco, "Área para teste.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Path ARQUIVO_CENTRAL = Path.of("dados", "central-resgate.dat");

    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            CentralResgate central = carregarOuCriarCentral();

            boolean executando = true;
            while (executando) {
                exibirMenu();
                int opcao = lerInteiro(input, "Selecione uma opção: ");

                try {
                    switch (opcao) {
                        case 1 -> cadastrarRobo(input, central);

                        case 2 -> cadastrarArea(input, central);

                        case 3 -> registrarMissao(input, central);

                        case 4 -> consultarCadastrosEHistorico(central);

                        case 5 -> listarRobosDisponiveis(central);

                        case 6 -> atribuirRobo(input, central);

                        case 7 -> executarMissao(input, central);

                        case 8 -> listarMissoesPendentes(central);

                        case 9 -> gerarRelatorio(central);

                        case 0 -> {
                                salvarCentral(central);
                                System.out.println("Dados salvos. Sessão encerrada.");
                                executando = false;
    }

    default -> System.out.println("Opção inválida. Escolha uma opção de 0 a 9.");
}
                } catch (RuntimeException e) {
                    System.out.println("Não foi possível realizar a operação: " + e.getMessage());
                }

                // Salva automaticamente após cada operação para evitar perda de dados.
                salvarCentral(central);
            }
        }
    }

    private static CentralResgate carregarOuCriarCentral() {
        try {
            Optional<CentralResgate> salva = PersistenciaCentral.carregar(ARQUIVO_CENTRAL);
            if (salva.isPresent()) {
                System.out.println("Dados anteriores recuperados de " + ARQUIVO_CENTRAL + ".");
                return salva.get();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Não foi possível recuperar os dados: " + e.getMessage());
            System.out.println("Uma nova central será iniciada.");
        }

        CentralResgate central = new CentralResgate("Central de Resgate");
        criarDadosDemonstracao(central);
        salvarCentral(central);
        return central;
    }

    private static void salvarCentral(CentralResgate central) {
        try {
            PersistenciaCentral.salvar(central, ARQUIVO_CENTRAL);
        } catch (IOException | RuntimeException e) {
            System.out.println("Aviso: não foi possível salvar os dados: " + e.getMessage());
        }
    }

    private static void exibirMenu() {
    System.out.println("\n========== CENTRAL DE RESGATE ==========");
    System.out.println("1 - Cadastrar robô");
    System.out.println("2 - Cadastrar área de operação");
    System.out.println("3 - Registrar missão");
    System.out.println("4 - Consultar cadastros e histórico de missões");
    System.out.println("5 - Listar robôs disponíveis");
    System.out.println("6 - Atribuir robô a uma missão");
    System.out.println("7 - Executar missão");
    System.out.println("8 - Consultar fila de missões pendentes");
    System.out.println("9 - Gerar relatório geral");
    System.out.println("0 - Salvar e sair");
    }

    private static void cadastrarRobo(Scanner input, CentralResgate central) {
        System.out.println("Tipo de robô: 1 - Drone | 2 - Robô terrestre");
        int tipo = lerInteiro(input, "Escolha: ");
        int id = lerInteiro(input, "ID: ");
        String nome = lerTexto(input, "Nome: ");
        int bateria = lerInteiro(input, "Bateria (0 a 100): ");

        Robo robo;
        if (tipo == 1) {
            int altitude = lerInteiro(input, "Altitude máxima em metros: ");
            robo = new Drone(id, nome, bateria, altitude);
        } else if (tipo == 2) {
            double capacidade = lerDecimal(input, "Capacidade de carga em kg: ");
            robo = new RoboTerrestre(id, nome, bateria, capacidade);
        } else {
            throw new DadosInvalidosException("Tipo de robô inválido.");
        }

        central.cadastrarRobo(robo);
        System.out.println("Robô cadastrado com sucesso.");
    }

    private static void cadastrarArea(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID da área: ");
        String nome = lerTexto(input, "Nome: ");
        String localizacao = lerTexto(input, "Localização: ");
        TipoTerreno terreno = lerEnum(input, TipoTerreno.class, "Tipo de terreno");
        NivelRisco risco = lerEnum(input, NivelRisco.class, "Nível de risco");
        String descricao = lerTexto(input, "Descrição: ");

        central.cadastrarArea(new AreaOperacao(id, nome, localizacao, terreno, risco, descricao));
        System.out.println("Área cadastrada com sucesso.");
    }

    private static void registrarMissao(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID da missão: ");
        String descricao = lerTexto(input, "Descrição: ");
        TipoMissao tipo = lerEnum(input, TipoMissao.class, "Tipo de missão");
        int idArea = lerInteiro(input, "ID da área associada: ");
        AreaOperacao area = central.buscarArea(idArea);
        PrioridadeMissao prioridade = lerEnum(input, PrioridadeMissao.class, "Prioridade");

        Missao missao;
        if (tipo == TipoMissao.TRANSPORTE_SUPRIMENTOS) {
            double peso = lerDecimal(input, "Peso dos suprimentos em kg: ");
            missao = new Missao(id, descricao, tipo, area, prioridade, peso);
        } else {
            missao = new Missao(id, descricao, tipo, area, prioridade);
        }

        central.registrarMissao(missao);
        System.out.println("Missão registrada e adicionada à fila de pendências.");
    }

    private static void listarRobosDisponiveis(CentralResgate central) {
        System.out.println("\n--- ROBÔS DISPONÍVEIS ---");
        if (central.listarRobosDisponiveis().isEmpty()) {
            System.out.println("Não há robôs disponíveis.");
            return;
        }
        central.listarRobosDisponiveis().forEach(robo -> {
            robo.exibirResumo();
            System.out.println("-------------------------");
        });
    }

    private static void listarMissoesPendentes(CentralResgate central) {
        System.out.println("\n--- FILA DE MISSÕES PENDENTES ---");
        if (central.listarMissoesPendentes().isEmpty()) {
            System.out.println("Não há missões pendentes.");
            return;
        }
        int posicao = 1;
        for (Missao missao : central.listarMissoesPendentes()) {
            System.out.println("Posição " + posicao++ + ":");
            missao.exibirResumo();
            System.out.println("-------------------------");
        }
    }

    private static void atribuirRobo(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID da missão: ");
        boolean atribuiu = central.atribuirRoboMissao(id);
        if (!atribuiu) {
            System.out.println("A missão permanece na fila.");
        }
    }

    private static void executarMissao(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID da missão: ");
        central.executarMissao(id);
        System.out.println("Missão executada com sucesso.");
    }

    private static void consultarHistorico(CentralResgate central) {
        System.out.println("\n--- HISTÓRICO DE MISSÕES ---");
        if (central.getMissoes().isEmpty()) {
            System.out.println("Ainda não há missões registradas.");
            return;
        }
        central.getMissoes().stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .forEach(missao -> {
                    missao.exibirResumo();
                    System.out.println("-------------------------");
                });
    }

    private static void gerarRelatorio(CentralResgate central) {
        Relatorio relatorio = central.gerarRelatorioGeral();
        System.out.println(relatorio.gerarResumo());
        relatorio.salvarEmArquivo();
    }

    private static void consultarMensagens(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID do robô: ");
        Robo robo = central.buscarRobo(id);
        System.out.println("\n--- MENSAGENS DE " + robo.getNome() + " ---");
        if (robo.getHistoricoMensagens().isEmpty()) {
            System.out.println("Esse robô ainda não possui mensagens.");
            return;
        }
        robo.getHistoricoMensagens().forEach(System.out::println);
    }

    private static void recarregarRobo(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID do robô: ");
        central.buscarRobo(id).recarregar();
        System.out.println("Robô recarregado.");
    }

    private static void colocarRoboEmManutencao(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID do robô: ");
        central.buscarRobo(id).colocarEmManutencao();
        System.out.println("Robô colocado em manutenção.");
    }

    private static void finalizarManutencao(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID do robô: ");
        central.buscarRobo(id).finalizarManutencao();
        System.out.println("Manutenção finalizada.");
    }

    private static void atualizarRiscoArea(Scanner input, CentralResgate central) {
        int id = lerInteiro(input, "ID da área: ");
        NivelRisco novoRisco = lerEnum(input, NivelRisco.class, "Novo nível de risco");
        central.atualizarNivelRiscoArea(id, novoRisco);
        System.out.println("Risco atualizado. A fila de missões foi reorganizada.");
    }

    private static void listarAreas(CentralResgate central) {
        System.out.println("\n--- ÁREAS CADASTRADAS ---");
        if (central.getAreas().isEmpty()) {
            System.out.println("Não há áreas cadastradas.");
            return;
        }
        central.getAreas().forEach(area -> {
            area.exibirInformacoes();
            System.out.println("-------------------------");
        });
    }

    private static int lerInteiro(Scanner input, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = input.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDecimal(Scanner input, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = input.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (Double.isFinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException ignored) {
                // A mensagem de erro é exibida abaixo.
            }
            System.out.println("Digite um número válido. Exemplo: 12,5");
        }
    }

    private static String lerTexto(Scanner input, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = input.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("O texto não pode ficar vazio.");
        }
    }

    private static void consultarCadastrosEHistorico(CentralResgate central) {

    System.out.println("\n--- ROBÔS CADASTRADOS ---");

    if (central.getRobos().isEmpty()) {
        System.out.println("Nenhum robô cadastrado.");
    }

    central.getRobos().forEach(Robo::exibirResumo);


    System.out.println("\n--- ÁREAS CADASTRADAS ---");

    if (central.getAreas().isEmpty()) {
        System.out.println("Nenhuma área cadastrada.");
    }

    central.getAreas().forEach(AreaOperacao::exibirInformacoes);


    System.out.println("\n--- HISTÓRICO DE MISSÕES ---");

    if (central.getMissoes().isEmpty()) {
        System.out.println("Nenhuma missão registrada.");
    }

    central.getMissoes().forEach(Missao::exibirResumo);
    }

    private static <E extends Enum<E>> E lerEnum(Scanner input, Class<E> tipo, String titulo) {
        while (true) {
            System.out.println(titulo + " " + Arrays.toString(tipo.getEnumConstants()));
            System.out.print("Digite uma das opções: ");
            String texto = input.nextLine().trim().toUpperCase(Locale.ROOT);
            try {
                return Enum.valueOf(tipo, texto);
            } catch (IllegalArgumentException e) {
                System.out.println("Opção inválida. Digite um dos valores apresentados.");
            }
        }
    }

    private static void criarDadosDemonstracao(CentralResgate central) {
        AreaOperacao urbana = new AreaOperacao(1, "Centro", "Região central",
                TipoTerreno.URBANO, NivelRisco.MEDIO, "Área urbana para reconhecimento.");
        AreaOperacao alagada = new AreaOperacao(2, "Bairro Ribeirinho", "Margem do rio",
                TipoTerreno.ALAGADO, NivelRisco.ALTO, "Região atingida por enchente.");
        AreaOperacao irregular = new AreaOperacao(3, "Encosta", "Zona de morros",
                TipoTerreno.IRREGULAR, NivelRisco.ALTO, "Terreno com obstáculos.");

        central.cadastrarArea(urbana);
        central.cadastrarArea(alagada);
        central.cadastrarArea(irregular);

        central.cadastrarRobo(new Drone(101, "Drone Águia", 90, 150));
        central.cadastrarRobo(new Drone(102, "Drone Falcão", 70, 100));
        central.cadastrarRobo(new RoboTerrestre(201, "Terrestre Atlas", 100, 50));
        central.cadastrarRobo(new RoboTerrestre(202, "Terrestre Hércules", 80, 100));

        central.registrarMissao(new Missao(1, "Reconhecer o centro", TipoMissao.RECONHECIMENTO,
                urbana, PrioridadeMissao.ALTA));
        central.registrarMissao(new Missao(2, "Localizar pessoas na enchente", TipoMissao.LOCALIZACAO_PESSOAS,
                alagada, PrioridadeMissao.ALTA));
        central.registrarMissao(new Missao(3, "Transportar kits de água", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                urbana, PrioridadeMissao.MEDIA, 35.0));
        central.registrarMissao(new Missao(4, "Levar mantimentos à encosta", TipoMissao.TRANSPORTE_SUPRIMENTOS,
                irregular, PrioridadeMissao.ALTA, 80.0));

        System.out.println("Dados demonstrativos cadastrados para facilitar os testes.");
    }
}

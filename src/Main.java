import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        int opcao = -1;

        do {
            System.out.println();
            System.out.println("====== Sistema de Coordenação de Robôs para Missões de Resgate ======");
            System.out.println("1 - Cadastrar robô");
            System.out.println("2 - Cadastrar área de operação");
            System.out.println("3 - Registrar missão");
            System.out.println("4 - Listar robôs disponíveis");
            System.out.println("5 - Listar missões pendentes");
            System.out.println("6 - Atribuir robô a missão");
            System.out.println("7 - Executar missão");
            System.out.println("8 - Consultar histórico de missões");
            System.out.println("9 - Gerar relatório geral");
            System.out.println("0 - Sair");
            System.out.print("Selecione uma opção: ");

            if (input.hasNextInt()) {
                opcao = input.nextInt();
            } else {
                input.nextLine();
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    System.out.println("Cadastro de robô");
                    break;

                case 2:
                    System.out.println("Cadastro de área de operação");
                    break;

                case 3:
                    System.out.println("Registro de missão.");
                    break;

                case 4:
                    System.out.println("Listagem de robôs disponíveis.");
                    break;

                case 5:
                    System.out.println("Listagem de missões pendentes.");
                    break;

                case 6:
                    System.out.println("Atribuição de robô a missão.");
                    break;

                case 7:
                    System.out.println("Execução de missão.");
                    break;

                case 8:
                    System.out.println("Histórico de missões.");
                    break;

                case 9:
                    System.out.println("Relatório geral.");
                    break;

                case 0:
                    System.out.println("Sessão encerrada.");
                    break;

                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }

        } while (opcao != 0);

        input.close();
    }
}

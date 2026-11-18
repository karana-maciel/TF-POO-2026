public class RoboTerrestre extends Robo {

    private double capacidadeCarga;

    public RoboTerrestre(int id, String nome, int nivelBateria, double capacidadeCarga) {
        super(id, nome, nivelBateria);

        if (capacidadeCarga <= 0) {
            throw new IllegalArgumentException("A capacidade de carga deve ser positiva.");
        }

        this.capacidadeCarga = capacidadeCarga;
    }

    public double getCapacidadeCarga() {
        return capacidadeCarga;
    }

    @Override
    public void exibirResumo() {
        super.exibirResumo();
        System.out.println("Capacidade de carga: " + capacidadeCarga + " kg");
    }

    @Override
    public void executarMissao(Missao missao) {

    }
}

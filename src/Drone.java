public class Drone extends Robo {

    private int altitudeMaxima;

    public Drone(int id, String nome, int nivelBateria, int altitudeMaxima) {
        super(id, nome, nivelBateria);

        if (altitudeMaxima <= 0) {
            throw new IllegalArgumentException("A altitude máxima deve ser positiva.");
        }

        this.altitudeMaxima = altitudeMaxima;
    }

    public int getAltitudeMaxima() {
        return altitudeMaxima;
    }

    @Override
    public void exibirResumo() {
        super.exibirResumo();
        System.out.println("Altitude máxima: " + altitudeMaxima + " metros");
    }

    @Override
    public void executarMissao(Missao missao) {

    }
}
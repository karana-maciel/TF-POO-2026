import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;


public final class PersistenciaCentral {
    private PersistenciaCentral() { }

    public static Optional<CentralResgate> carregar(Path arquivo) throws IOException, ClassNotFoundException {
        if (!Files.exists(arquivo)) return Optional.empty();
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(arquivo))) {
            Object objeto = entrada.readObject();
            if (!(objeto instanceof CentralResgate central)) {
                throw new IOException("O arquivo não contém dados de uma CentralResgate.");
            }
            return Optional.of(central);
        }
    }

    
    public static void salvar(CentralResgate central, Path arquivo) throws IOException {
        if (central == null) throw new DadosInvalidosException("A central não pode ser nula.");
        Path destino = arquivo.toAbsolutePath();
        Path pasta = destino.getParent();
        if (pasta != null) Files.createDirectories(pasta);

        Path temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
        try {
            try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                saida.writeObject(central);
                saida.flush();
            }
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException naoAtomico) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }
}

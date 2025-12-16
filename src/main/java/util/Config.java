package util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configurações estáticas e diretório de dados.
 */
public class Config {
    private static Path dataDir = Paths.get("./data");

    public static void init() {
        // poderia ser carregado de arquivo config.json; por ora usa padrão ./data
    }

    public static Path getDataDir() {
        return dataDir;
    }
}
package export;

import java.io.File;

public interface Exportavel {
    void gerarRelatorio(String formato, File destino) throws Exception;
}
import export.RelatorioMensal;
import model.Categoria;
import model.Despesa;
import model.Moeda;
import model.Transacao;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TesteGerarPDF {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTE DE PDF ===");

        try {
            // 1. Criando dados falsos para testar
            Categoria catAlimentacao = new Categoria("1", "Alimentação", "#FF0000");
            Categoria catLazer = new Categoria("2", "Lazer", "#00FF00");

            Transacao t1 = new Despesa(UUID.randomUUID().toString(), LocalDate.now(), new BigDecimal("150.00"), Moeda.BRL, catAlimentacao, "Pizza Gigante");
            t1.setValorBRL(new BigDecimal("150.00")); // Forçando valor BRL

            Transacao t2 = new Despesa(UUID.randomUUID().toString(), LocalDate.now().minusDays(1), new BigDecimal("50.00"), Moeda.BRL, catLazer, "Cinema");
            t2.setValorBRL(new BigDecimal("50.00"));

            List<Transacao> listaFalsa = Arrays.asList(t1, t2);

            // 2. Definindo onde salvar (Na Área de Trabalho para você achar fácil)
            // Se der erro de caminho, ele vai salvar na raiz do projeto
            String caminhoUsuario = System.getProperty("user.home");
            File arquivoDestino = new File(caminhoUsuario + "/Desktop/TESTE_RELATORIO.pdf");

            // Se não achar o Desktop, salva na raiz do projeto mesmo
            if (!arquivoDestino.getParentFile().exists()) {
                arquivoDestino = new File("TESTE_RELATORIO.pdf");
            }

            System.out.println("Tentando salvar em: " + arquivoDestino.getAbsolutePath());

            // 3. Chamando sua classe original
            RelatorioMensal relatorio = new RelatorioMensal(2026, 1, listaFalsa);
            relatorio.gerarRelatorio("PDF", arquivoDestino);

            System.out.println(">>> SUCESSO! PDF GERADO! <<<");
            System.out.println("Vá até o local acima e abra o arquivo.");

        } catch (Exception e) {
            System.err.println("DEU ERRO AO GERAR O PDF:");
            e.printStackTrace();
        }
    }
}
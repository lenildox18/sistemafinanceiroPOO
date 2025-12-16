package export;

import model.Transacao;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação simples de Relatório mensal que gera TXT e PDF.
 */
public class RelatorioMensal implements Exportavel {
    private final int ano;
    private final int mes;
    private final List<Transacao> transacoes;

    public RelatorioMensal(int ano, int mes, List<Transacao> transacoes) {
        this.ano = ano;
        this.mes = mes;
        this.transacoes = transacoes;
    }

    @Override
    public void gerarRelatorio(String formato, File destino) throws Exception {
        if ("TXT".equalsIgnoreCase(formato)) {
            gerarTxt(destino);
        } else if ("PDF".equalsIgnoreCase(formato)) {
            gerarPdf(destino);
        } else {
            throw new IllegalArgumentException("Formato não suportado: " + formato);
        }
    }

    private void gerarTxt(File destino) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(destino))) {
            w.write("Relatório " + YearMonth.of(ano, mes).toString());
            w.newLine();
            w.write("Total de transações: " + transacoes.size());
            w.newLine();
            w.write("----");
            w.newLine();
            for (Transacao t : transacoes) {
                w.write(String.format("%s | %s | %.2f BRL | %s",
                        t.getData(), t.getCategoria() == null ? "SEM" : t.getCategoria().getNome(),
                        t.getValorBRL() == null ? t.getValorOriginal().doubleValue() : t.getValorBRL().doubleValue(),
                        t.getDescricao()));
                w.newLine();
            }
        }
    }

    private void gerarPdf(File destino) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(document, page);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.beginText();
        cs.newLineAtOffset(50, 750);
        cs.showText("Relatório " + YearMonth.of(ano, mes).toString());
        cs.newLineAtOffset(0, -20);
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.showText("Total de transações: " + transacoes.size());
        cs.newLineAtOffset(0, -20);
        cs.showText("----");
        cs.newLineAtOffset(0, -20);
        for (Transacao t : transacoes) {
            String line = String.format("%s | %s | %.2f BRL | %s",
                    t.getData(), t.getCategoria() == null ? "SEM" : t.getCategoria().getNome(),
                    t.getValorBRL() == null ? t.getValorOriginal().doubleValue() : t.getValorBRL().doubleValue(),
                    t.getDescricao());
            cs.showText(line);
            cs.newLineAtOffset(0, -16);
        }
        cs.endText();
        cs.close();
        document.save(destino);
        document.close();
    }
}
package service;

import export.Exportavel;
import export.RelatorioMensal;
import model.Transacao;
import persistence.RepositorioPersistencia;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço que fornece funções de geração de relatórios.
 */
public class RelatorioService {
    private final RepositorioPersistencia repo;

    public RelatorioService(RepositorioPersistencia repo) {
        this.repo = repo;
    }

    public void gerarRelatorioMensal(int ano, int mes, File destino, String formato) throws Exception {
        List<Transacao> trans = repo.getTransacoes().stream()
                .filter(t -> t.getData().getYear() == ano && t.getData().getMonthValue() == mes)
                .collect(Collectors.toList());
        Exportavel rel = new RelatorioMensal(ano, mes, trans);
        rel.gerarRelatorio(formato, destino);
    }
}
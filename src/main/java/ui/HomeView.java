package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Transacao;
import persistence.RepositorioPersistencia;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tela Home: resumo e últimas transações.
 */
public class HomeView {
    private final RepositorioPersistencia repo;
    private final BorderPane view;

    public HomeView(RepositorioPersistencia repo, Runnable onNovaTransacao, Runnable onConfiguracoes) {
        this.repo = repo;
        this.view = new BorderPane();
        build(onNovaTransacao, onConfiguracoes);
    }

    private void build(Runnable onNovaTransacao, Runnable onConfiguracoes) {
        VBox left = new VBox(10);
        left.setPadding(new Insets(10));
        Label saldoLabel = new Label("Saldo atual: R$ " + calcularSaldo());
        Label totalReceitas = new Label("Total Receitas: R$ " + totalReceitas());
        Label totalDespesas = new Label("Total Despesas: R$ " + totalDespesas());
        Button btnNova = new Button("Nova Transação");
        btnNova.setOnAction(e -> onNovaTransacao.run());
        Button btnCfg = new Button("Categorias / Config");
        btnCfg.setOnAction(e -> onConfiguracoes.run());
        left.getChildren().addAll(saldoLabel, totalReceitas, totalDespesas, btnNova, btnCfg);

        // gráfico pizza por categoria (despesas)
        PieChart chart = new PieChart();
        Map<String, Double> porCat = porCategoria();
        List<PieChart.Data> data = porCat.entrySet().stream()
                .map(en -> new PieChart.Data(en.getKey(), en.getValue()))
                .collect(Collectors.toList());
        chart.setData(FXCollections.observableArrayList(data));

        // últimas transações
        TableView<Transacao> table = new TableView<>();
        TableColumn<Transacao, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty.stringExpression(
                javafx.beans.property.SimpleStringProperty.stringExpression(new javafx.beans.property.SimpleStringProperty(c.getValue().getData().toString()))
        ));
        TableColumn<Transacao, String> colCat = new TableColumn<>("Categoria");
        colCat.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty.stringExpression(
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCategoria() == null ? "" : c.getValue().getCategoria().getNome())
        ));
        TableColumn<Transacao, String> colValor = new TableColumn<>("Valor (BRL)");
        colValor.setCellValueFactory(c -> javafx.beans.property.SimpleStringProperty.stringExpression(
                new javafx.beans.property.SimpleStringProperty(String.format("%.2f", c.getValue().getValorBRL() == null ? c.getValue().getValorOriginal().doubleValue() : c.getValue().getValorBRL().doubleValue()))
        ));
        table.getColumns().addAll(colData, colCat, colValor);
        // ordenar últimas 10
        List<Transacao> ultimas = repo.getTransacoes().stream()
                .sorted(Comparator.comparing(Transacao::getData).reversed())
                .limit(10)
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(ultimas));

        view.setLeft(left);
        view.setCenter(chart);
        view.setBottom(table);
    }

    public Node getView() {
        return view;
    }

    private double calcularSaldo() {
        return repo.getTransacoes().stream()
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .reduce(0.0, Double::sum);
    }

    private double totalReceitas() {
        return repo.getTransacoes().stream()
                .filter(t -> t.getClass().getSimpleName().equals("Receita"))
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .reduce(0.0, Double::sum);
    }

    private double totalDespesas() {
        return repo.getTransacoes().stream()
                .filter(t -> t.getClass().getSimpleName().equals("Despesa"))
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .map(v -> -v) // impacto retorna negativo
                .reduce(0.0, Double::sum);
    }

    private Map<String, Double> porCategoria() {
        Map<String, Double> map = new HashMap<>();
        repo.getTransacoes().forEach(t -> {
            String nome = t.getCategoria() == null ? "Sem Categoria" : t.getCategoria().getNome();
            double val = Math.abs((t.getValorBRL() == null ? t.getValorOriginal().doubleValue() : t.getValorBRL().doubleValue()));
            map.put(nome, map.getOrDefault(nome, 0.0) + val);
        });
        return map;
    }
}
package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Transacao;
import model.Receita;
import model.Despesa;
import persistence.RepositorioPersistencia;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tela Home: Dashboard moderno com cards de resumo, gráfico e tabela.
 */
public class HomeView {
    private final RepositorioPersistencia repo;
    private final BorderPane view;
    // Formatador para Dinheiro Brasileiro (R$ 1.000,00)
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HomeView(RepositorioPersistencia repo, Runnable onNovaTransacao, Runnable onConfiguracoes) {
        this.repo = repo;
        this.view = new BorderPane();
        build(onNovaTransacao, onConfiguracoes);
    }

    private void build(Runnable onNovaTransacao, Runnable onConfiguracoes) {
        view.setPadding(new Insets(20));

        // --- 1. BARRA LATERAL (Botões) ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(0, 20, 0, 0)); // Espaço na direita
        sidebar.setPrefWidth(200);

        Label lblMenu = new Label("Acesso Rápido");
        lblMenu.getStyleClass().add("section-title");

        Button btnNova = new Button("+ Nova Transação");
        btnNova.setMaxWidth(Double.MAX_VALUE); // Botão largo
        btnNova.getStyleClass().add("button-success"); // Verde
        btnNova.setOnAction(e -> onNovaTransacao.run());

        Button btnCfg = new Button("Configurações");
        btnCfg.setMaxWidth(Double.MAX_VALUE);
        btnCfg.setOnAction(e -> onConfiguracoes.run());

        sidebar.getChildren().addAll(lblMenu, btnNova, btnCfg);
        view.setLeft(sidebar);

        // --- 2. ÁREA CENTRAL (Dashboard) ---
        VBox centerLayout = new VBox(20); // Espaço vertical entre elementos

        // 2.1 Cards de Resumo (Topo)
        HBox cardsBox = new HBox(20); // Espaço horizontal entre cards
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        double saldo = calcularSaldo();
        double rec = totalReceitas();
        double desp = totalDespesas();

        // Criação dos cards usando método auxiliar
        VBox cardSaldo = createCard("SALDO ATUAL", saldo, saldo >= 0 ? "text-primary" : "text-danger");
        VBox cardReceita = createCard("RECEITAS", rec, "text-success");
        VBox cardDespesa = createCard("DESPESAS", desp, "text-danger");

        cardsBox.getChildren().addAll(cardSaldo, cardReceita, cardDespesa);

        // 2.2 Gráfico e Tabela (Dividindo o espaço restante)
        HBox contentBox = new HBox(20);
        contentBox.setPrefHeight(1000); // Força ocupar altura

        // Coluna da Tabela (Esquerda do content)
        VBox tableBox = new VBox(10);
        Label lblUltimas = new Label("Últimas Transações");
        lblUltimas.getStyleClass().add("section-title");

        TableView<Transacao> table = createTable();
        VBox.setVgrow(table, Priority.ALWAYS); // Tabela cresce
        tableBox.getChildren().addAll(lblUltimas, table);
        HBox.setHgrow(tableBox, Priority.ALWAYS); // Box da tabela cresce horizontalmente

        // Coluna do Gráfico (Direita do content)
        VBox chartBox = new VBox(10);
        Label lblChart = new Label("Despesas por Categoria");
        lblChart.getStyleClass().add("section-title");

        PieChart chart = createPieChart();
        chart.setLabelsVisible(false); // Limpa visual
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chartBox.getChildren().addAll(lblChart, chart);
        chartBox.setMinWidth(300); // Tamanho fixo pro gráfico não sumir

        contentBox.getChildren().addAll(tableBox, chartBox);

        // Adiciona tudo ao layout central
        centerLayout.getChildren().addAll(cardsBox, contentBox);
        view.setCenter(centerLayout);
    }

    // --- Métodos Auxiliares de Construção ---

    private VBox createCard(String title, double value, String cssClass) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card"); // Classe CSS do card branco

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title");

        Label lblValue = new Label(nf.format(value)); // Formata R$
        lblValue.getStyleClass().add("card-value");
        lblValue.getStyleClass().add(cssClass); // Cor do texto (verde/vermelho/azul)

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private TableView<Transacao> createTable() {
        TableView<Transacao> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Colunas ocupam todo espaço

        TableColumn<Transacao, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(df.format(c.getValue().getData()))
        );

        TableColumn<Transacao, String> colCat = new TableColumn<>("Categoria");
        colCat.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCategoria() == null ? "-" : c.getValue().getCategoria().getNome()
                )
        );

        TableColumn<Transacao, String> colValor = new TableColumn<>("Valor");
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;"); // Alinha números à direita
        colValor.setCellValueFactory(c -> {
            BigDecimal val = c.getValue().getValorBRL() != null ? c.getValue().getValorBRL() : c.getValue().getValorOriginal();
            return new javafx.beans.property.SimpleStringProperty(nf.format(val));
        });

        table.getColumns().addAll(colData, colCat, colValor);

        // Ordenar últimas 15
        List<Transacao> ultimas = repo.getTransacoes().stream()
                .sorted(Comparator.comparing(Transacao::getData).reversed())
                .limit(15)
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(ultimas));

        return table;
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        // Filtra apenas DESPESAS para o gráfico fazer sentido
        Map<String, Double> porCat = porCategoriaDespesa();

        List<PieChart.Data> data = porCat.entrySet().stream()
                .map(en -> new PieChart.Data(en.getKey(), en.getValue()))
                .collect(Collectors.toList());

        chart.setData(FXCollections.observableArrayList(data));
        return chart;
    }

    public Node getView() {
        return view;
    }

    // --- Métodos de Cálculo ---

    private double calcularSaldo() {
        return repo.getTransacoes().stream()
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .reduce(0.0, Double::sum);
    }

    private double totalReceitas() {
        return repo.getTransacoes().stream()
                .filter(t -> t instanceof Receita) // Uso seguro de instanceof
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .reduce(0.0, Double::sum);
    }

    private double totalDespesas() {
        return repo.getTransacoes().stream()
                .filter(t -> t instanceof Despesa)
                .map(Transacao::impactoNoSaldo)
                .map(BigDecimal::doubleValue)
                .map(Math::abs) // Pega valor absoluto para mostrar no card
                .reduce(0.0, Double::sum);
    }

    private Map<String, Double> porCategoriaDespesa() {
        Map<String, Double> map = new HashMap<>();
        repo.getTransacoes().stream()
                .filter(t -> t instanceof Despesa) // Apenas despesas no gráfico
                .forEach(t -> {
                    String nome = t.getCategoria() == null ? "Outros" : t.getCategoria().getNome();
                    double val = Math.abs((t.getValorBRL() == null ? t.getValorOriginal().doubleValue() : t.getValorBRL().doubleValue()));
                    map.put(nome, map.getOrDefault(nome, 0.0) + val);
                });
        return map;
    }
}
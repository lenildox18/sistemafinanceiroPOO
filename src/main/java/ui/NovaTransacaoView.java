package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Categoria;
import model.Despesa;
import model.Moeda;
import model.Receita;
import persistence.RepositorioPersistencia;
import service.CurrencyService;
import util.DateUtils;
import util.exceptions.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tela de Nova Transação (botão de cotar/converter ao lado do ComboBox de moeda).
 */
public class NovaTransacaoView {
    private final RepositorioPersistencia repo;
    private final GridPane view;
    private final CurrencyService currencyService;

    public NovaTransacaoView(RepositorioPersistencia repo, Runnable onSaved) {
        this.repo = repo;
        this.view = new GridPane();
        this.currencyService = new CurrencyService();
        build(onSaved);
    }

    private void build(Runnable onSaved) {
        // --- Configuração do Layout ---
        view.setPadding(new Insets(20)); // Mais espaço nas bordas
        view.setHgap(15); // Espaço horizontal entre colunas
        view.setVgap(15); // Espaço vertical entre linhas
        view.setAlignment(Pos.TOP_CENTER); // Centraliza o formulário

        // --- Título da Tela ---
        Label titulo = new Label("Nova Transação");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        view.add(titulo, 0, 0, 2, 1); // Ocupa 2 colunas

        // --- Campos ---
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Receita", "Despesa"));
        tipo.getSelectionModel().selectFirst();
        tipo.setMaxWidth(Double.MAX_VALUE); // Estica o campo

        TextField valor = new TextField();

        ComboBox<Moeda> moeda = new ComboBox<>(FXCollections.observableArrayList(Moeda.BRL, Moeda.USD, Moeda.EUR));
        moeda.getSelectionModel().select(Moeda.BRL);
        moeda.setMaxWidth(Double.MAX_VALUE);

        DatePicker data = new DatePicker(LocalDate.now());
        data.setMaxWidth(Double.MAX_VALUE); // Estica o campo

        // Carrega categorias (proteção caso esteja vazio)
        ComboBox<Categoria> categoria = new ComboBox<>();
        if (repo.getCategorias() != null) {
            categoria.setItems(FXCollections.observableArrayList(repo.getCategorias()));
        }
        categoria.setMaxWidth(Double.MAX_VALUE); // Estica o campo

        TextField descricao = new TextField();

        // --- Botões e Ações ---
        Button btnConverter = new Button("Cotar/Converter");
        Label lblConvertido = new Label("");
        lblConvertido.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Texto verde destaque

        Button btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("button-success"); // CLASSE CSS VERDE
        btnSalvar.setDefaultButton(true); // Ativa com Enter

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("button-danger"); // CLASSE CSS VERMELHA
        btnCancelar.setOnAction(e -> onSaved.run()); // Volta para a home sem salvar

        // HBox para agrupar Salvar e Cancelar lado a lado
        HBox boxBotoes = new HBox(10, btnSalvar, btnCancelar);
        boxBotoes.setAlignment(Pos.CENTER_RIGHT);

        // --- Adicionando ao Grid ---
        view.add(new Label("Tipo:"), 0, 1);
        view.add(tipo, 1, 1);

        view.add(new Label("Valor:"), 0, 2);
        view.add(valor, 1, 2);

        // MOEDA: colocamos o ComboBox e o botão dentro de um HBox na mesma célula
        view.add(new Label("Moeda:"), 0, 3);
        HBox moedaBox = new HBox(10);
        moedaBox.setAlignment(Pos.CENTER_LEFT);
        moedaBox.getChildren().addAll(moeda, btnConverter);
        HBox.setHgrow(moeda, Priority.ALWAYS);
        btnConverter.setMinWidth(120);
        view.add(moedaBox, 1, 3); // moeda + converter na mesma célula

        // Resultado da conversão logo abaixo (na mesma coluna)
        view.add(lblConvertido, 1, 4);

        view.add(new Label("Data:"), 0, 5);
        view.add(data, 1, 5);

        view.add(new Label("Categoria:"), 0, 6);
        view.add(categoria, 1, 6);

        view.add(new Label("Descrição:"), 0, 7);
        view.add(descricao, 1, 7);

        view.add(boxBotoes, 1, 9);

        // --- Ações dos botões ---
        btnConverter.setOnAction(e -> {
            try {
                if (valor.getText().isEmpty()) throw new Exception("Digite um valor para converter.");
                BigDecimal v = new BigDecimal(valor.getText().replace(",", "."));
                Moeda from = moeda.getValue();
                if (from == Moeda.BRL) {
                    lblConvertido.setText("Já está em BRL.");
                    return;
                }
                BigDecimal converted = currencyService.convert(from.name(), "BRL", v);
                lblConvertido.setText(String.format("≈ R$ %.2f (Cotação Online)", converted.doubleValue()));
            } catch (NumberFormatException nfe) {
                showAlert("Valor numérico inválido.");
            } catch (Exception ex) {
                lblConvertido.setText("");
                showAlert("Erro conversão: " + ex.getMessage());
            }
        });

        btnSalvar.setOnAction(e -> {
            try {
                if (valor.getText().isEmpty()) throw new Exception("Digite um valor");

                BigDecimal v = new BigDecimal(valor.getText().replace(",", "."));
                LocalDate d = data.getValue();

                if (d == null) throw new Exception("Data inválida");
                if (DateUtils.isFuture(d)) {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Data no futuro. Confirmar?", ButtonType.YES, ButtonType.NO);
                    a.showAndWait();
                    if (a.getResult() != ButtonType.YES) return;
                }

                Categoria cat = categoria.getValue();
                if (cat == null) {
                    showAlert("Selecione uma categoria");
                    return;
                }

                String id = UUID.randomUUID().toString();
                java.math.BigDecimal valorOriginal = v;
                model.Transacao t;
                if ("Receita".equals(tipo.getValue())) {
                    t = new Receita(id, d, valorOriginal, moeda.getValue(), cat, descricao.getText());
                } else {
                    t = new Despesa(id, d, valorOriginal, moeda.getValue(), cat, descricao.getText());
                }

                // Lógica de conversão ao salvar
                if (t.getMoeda() != Moeda.BRL) {
                    try {
                        BigDecimal converted = currencyService.convert(t.getMoeda().name(), "BRL", t.getValorOriginal());
                        t.setValorBRL(converted);
                    } catch (Exception ex) {
                        TextInputDialog dlg = new TextInputDialog();
                        dlg.setHeaderText("API Offline. Informe a taxa manual (1 " + t.getMoeda() + " = ? BRL)");
                        dlg.showAndWait();
                        if (dlg.getResult() == null || dlg.getResult().isEmpty()) {
                            showAlert("Taxa necessária para salvar.");
                            return;
                        }
                        BigDecimal taxa = new BigDecimal(dlg.getResult().replace(",", "."));
                        t.setValorBRL(t.getValorOriginal().multiply(taxa));
                    }
                } else {
                    t.setValorBRL(t.getValorOriginal());
                }

                repo.addTransacao(t);
                showInfo("Transação salva com sucesso!");
                onSaved.run(); // Volta para a tela anterior

            } catch (NumberFormatException nfe) {
                showAlert("Valor numérico inválido.");
            } catch (SaldoInsuficienteException sie) {
                showAlert("Saldo insuficiente: " + sie.getMessage());
            } catch (Exception ex) {
                showAlert("Erro: " + ex.getMessage());
            }
        });
    }

    public Node getView() {
        return view;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}
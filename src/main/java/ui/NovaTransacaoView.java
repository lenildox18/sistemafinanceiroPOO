package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.*;
import persistence.RepositorioPersistencia;
import service.CurrencyService;
import util.DateUtils;
import util.exceptions.DataInvalidaException;
import util.exceptions.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tela de nova transação com conversão via CurrencyService.
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
        view.setPadding(new Insets(10));
        view.setHgap(10);
        view.setVgap(10);

        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Receita", "Despesa"));
        tipo.getSelectionModel().selectFirst();
        TextField valor = new TextField();
        ComboBox<Moeda> moeda = new ComboBox<>(FXCollections.observableArrayList(Moeda.BRL, Moeda.USD, Moeda.EUR));
        moeda.getSelectionModel().select(Moeda.BRL);
        DatePicker data = new DatePicker(LocalDate.now());
        ComboBox<model.Categoria> categoria = new ComboBox<>(FXCollections.observableArrayList(repo.getCategorias().toArray(new model.Categoria[0])));
        TextField descricao = new TextField();
        Button btnConverter = new Button("Converter para BRL");
        Label lblConvertido = new Label("");
        Button btnSalvar = new Button("Salvar");

        view.add(new Label("Tipo"), 0, 0);
        view.add(tipo, 1, 0);
        view.add(new Label("Valor"), 0, 1);
        view.add(valor, 1, 1);
        view.add(new Label("Moeda"), 0, 2);
        view.add(moeda, 1, 2);
        view.add(btnConverter, 2, 2);
        view.add(lblConvertido, 1, 3);
        view.add(new Label("Data"), 0, 4);
        view.add(data, 1, 4);
        view.add(new Label("Categoria"), 0, 5);
        view.add(categoria, 1, 5);
        view.add(new Label("Descrição"), 0, 6);
        view.add(descricao, 1, 6);
        view.add(btnSalvar, 1, 7);

        btnConverter.setOnAction(e -> {
            try {
                BigDecimal v = new BigDecimal(valor.getText());
                Moeda m = moeda.getValue();
                if (m == Moeda.BRL) {
                    lblConvertido.setText("Já em BRL: R$ " + v);
                    return;
                }
                BigDecimal converted = currencyService.convert(m.name(), "BRL", v);
                lblConvertido.setText(String.format("Convertido: R$ %.2f (taxa usada)", converted.doubleValue()));
            } catch (Exception ex) {
                showAlert("Erro conversão: " + ex.getMessage());
            }
        });

        btnSalvar.setOnAction(e -> {
            try {
                BigDecimal v = new BigDecimal(valor.getText());
                LocalDate d = data.getValue();
                if (DateUtils.isFuture(d)) {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Data no futuro. Confirmar?", ButtonType.YES, ButtonType.NO);
                    a.showAndWait();
                    if (a.getResult() != ButtonType.YES) return;
                }
                model.Categoria cat = categoria.getValue();
                if (cat == null) {
                    showAlert("Categoria obrigatória");
                    return;
                }
                String id = UUID.randomUUID().toString();
                Transacao t;
                if ("Receita".equals(tipo.getValue())) {
                    t = new Receita(id, d, v, moeda.getValue(), cat, descricao.getText());
                } else {
                    t = new Despesa(id, d, v, moeda.getValue(), cat, descricao.getText());
                }
                // conversão para BRL se necessário
                if (t.getMoeda() != Moeda.BRL) {
                    try {
                        BigDecimal converted = currencyService.convert(t.getMoeda().name(), "BRL", t.getValorOriginal());
                        t.setValorBRL(converted);
                    } catch (Exception ex) {
                        // fallback: pedir taxa manual
                        TextInputDialog dlg = new TextInputDialog();
                        dlg.setHeaderText("API de câmbio falhou. Informe taxa (1 " + t.getMoeda() + " = ? BRL)");
                        dlg.showAndWait();
                        if (dlg.getResult() == null) {
                            showAlert("Taxa necessária para conversão");
                            return;
                        }
                        BigDecimal taxa = new BigDecimal(dlg.getResult());
                        t.setValorBRL(t.getValorOriginal().multiply(taxa));
                    }
                } else {
                    t.setValorBRL(t.getValorOriginal());
                }

                // validação de saldo
                double saldoAtual = repo.getTransacoes().stream()
                        .map(Transacao::impactoNoSaldo)
                        .mapToDouble(b -> b.doubleValue())
                        .sum();
                if (t instanceof model.Despesa) {
                    boolean proibidoNegativo = true; // para demo; poderia vir de Config
                    if (proibidoNegativo && saldoAtual + t.impactoNoSaldo().doubleValue() < 0) {
                        throw new SaldoInsuficienteException("Saldo insuficiente para registrar esta despesa");
                    }
                }

                repo.addTransacao(t);
                showInfo("Transação salva");
                onSaved.run();
            } catch (SaldoInsuficienteException sie) {
                showAlert("Erro: " + sie.getMessage());
            } catch (DataInvalidaException die) {
                showAlert("Data inválida: " + die.getMessage());
            } catch (NumberFormatException nfe) {
                showAlert("Valor inválido");
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
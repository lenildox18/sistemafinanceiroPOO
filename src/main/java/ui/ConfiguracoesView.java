package ui;

import javafx.stage.FileChooser;
import service.RelatorioService;
import java.io.File;
import java.time.LocalDate;
import javafx.scene.control.Separator;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Categoria;
import persistence.RepositorioPersistencia;

import java.util.UUID;

/**
 * Tela de configurações: Gerenciamento de Categorias e Relatórios.
 */
public class ConfiguracoesView {
    private final RepositorioPersistencia repo;
    private final BorderPane view;

    // Componentes de UI
    private ListView<Categoria> list;
    private TextField txtNome;
    private ColorPicker colorPicker;
    private Button btnSalvar;
    private Button btnExcluir;
    private Button btnNova;

    // Controle de estado
    private Categoria categoriaSelecionada = null;

    public ConfiguracoesView(RepositorioPersistencia repo) {
        this.repo = repo;
        this.view = new BorderPane();
        build();
    }

    private void build() {
        view.setPadding(new Insets(20));

        // --- TÍTULO ---
        Text titulo = new Text("Configurações do Sistema");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // --- COLUNA DA ESQUERDA (LISTA DE CATEGORIAS) ---
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(300);

        Label lblList = new Label("Categorias Cadastradas");
        lblList.getStyleClass().add("section-title");

        list = new ListView<>();
        list.setItems(FXCollections.observableArrayList(repo.getCategorias()));
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNome());
                    // Opcional: bolinha de cor
                    // setStyle("-fx-control-inner-background: " + item.getCor());
                }
            }
        });

        // Evento: Ao clicar na lista, preenche o formulário
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selecionarCategoria(newVal);
            }
        });

        leftPane.getChildren().addAll(lblList, list);

        // --- COLUNA DA DIREITA (FORMULÁRIOS) ---
        VBox rightPane = new VBox(15);
        rightPane.setPadding(new Insets(0, 0, 0, 20)); // Afasta da lista
        rightPane.setAlignment(Pos.TOP_LEFT);

        // 1. Bloco de Categorias
        Label lblForm = new Label("Detalhes da Categoria");
        lblForm.getStyleClass().add("section-title");

        Label lblNome = new Label("Nome:");
        txtNome = new TextField();
        txtNome.setMaxWidth(300);

        Label lblCor = new Label("Cor (Etiqueta):");
        colorPicker = new ColorPicker(Color.GRAY);
        colorPicker.setMaxWidth(300);

        btnNova = new Button("Nova / Limpar");
        btnNova.setOnAction(e -> limparFormulario());

        btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("button-success");
        btnSalvar.setOnAction(e -> salvar());

        btnExcluir = new Button("Excluir");
        btnExcluir.getStyleClass().add("button-danger");
        btnExcluir.setDisable(true);
        btnExcluir.setOnAction(e -> excluir());

        HBox buttonBox = new HBox(10, btnNova, btnSalvar, btnExcluir);

        // --- 2. BLOCO DE RELATÓRIOS (NOVO CÓDIGO AQUI) ---

        Separator separator = new Separator();
        separator.setPadding(new Insets(20, 0, 10, 0)); // Espaço visual

        Label lblRelatorios = new Label("Relatórios e Exportação");
        lblRelatorios.getStyleClass().add("section-title");

        Button btnPdf = new Button("Baixar Relatório Mensal (PDF)");
        // Estilo Laranja para destacar
        btnPdf.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPdf.setMaxWidth(300);

        // AÇÃO DO BOTÃO PDF
        btnPdf.setOnAction(e -> {
            try {
                // A. Configura Janela de Salvar
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Salvar Relatório Financeiro");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));
                fileChooser.setInitialFileName("Relatorio_" + LocalDate.now() + ".pdf");

                File destino = fileChooser.showSaveDialog(null);

                if (destino != null) {
                    // B. Instancia o Serviço usando o repositório existente
                    RelatorioService service = new RelatorioService(this.repo);

                    // Pega mês atual (pode ser melhorado com DatePicker depois)
                    int ano = LocalDate.now().getYear();
                    int mes = LocalDate.now().getMonthValue();

                    // C. Gera o PDF
                    service.gerarRelatorioMensal(ano, mes, destino, "PDF");

                    // D. Feedback
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Relatório salvo em:\n" + destino.getAbsolutePath());
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Erro ao gerar PDF: " + ex.getMessage());
            }
        });

        // Adiciona tudo na tela da direita (Categorias + Relatórios)
        rightPane.getChildren().addAll(
                lblForm, lblNome, txtNome, lblCor, colorPicker, buttonBox, // Parte de Categorias
                separator, lblRelatorios, btnPdf                           // Parte de Relatórios
        );

        // --- MONTAGEM FINAL ---
        view.setTop(titulo);
        view.setLeft(leftPane);
        view.setCenter(rightPane);
        BorderPane.setMargin(titulo, new Insets(0, 0, 20, 0));
    }

    // --- MÉTODOS AUXILIARES ---

    private void selecionarCategoria(Categoria c) {
        this.categoriaSelecionada = c;
        txtNome.setText(c.getNome());
        try {
            colorPicker.setValue(Color.web(c.getCor()));
        } catch (Exception e) {
            colorPicker.setValue(Color.GRAY);
        }
        btnSalvar.setText("Atualizar");
        btnExcluir.setDisable(false);
    }

    private void limparFormulario() {
        this.categoriaSelecionada = null;
        list.getSelectionModel().clearSelection();
        txtNome.clear();
        colorPicker.setValue(Color.GRAY);
        btnSalvar.setText("Salvar");
        btnExcluir.setDisable(true);
    }

    private void salvar() {
        String nome = txtNome.getText();
        if (nome == null || nome.trim().isEmpty()) {
            showAlert("O nome da categoria é obrigatório.");
            return;
        }

        String hexColor = toHexString(colorPicker.getValue());

        if (categoriaSelecionada == null) {
            Categoria nova = new Categoria(UUID.randomUUID().toString(), nome, hexColor);
            repo.addCategoria(nova);
            list.getItems().add(nova);
            limparFormulario();
            showInfo("Categoria criada com sucesso!");
        } else {
            categoriaSelecionada.setNome(nome);
            categoriaSelecionada.setCor(hexColor);
            repo.saveCategorias();
            list.refresh();
            showInfo("Categoria atualizada!");
        }
    }

    private void excluir() {
        if (categoriaSelecionada == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja excluir a categoria '" + categoriaSelecionada.getNome() + "'?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            repo.removeCategoria(categoriaSelecionada);
            list.getItems().remove(categoriaSelecionada);
            limparFormulario();
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
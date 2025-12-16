package ui;

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
 * Tela de configurações: Gerenciamento de Categorias com visual Master-Detail.
 */
public class ConfiguracoesView {
    private final RepositorioPersistencia repo;
    private final BorderPane view;

    // Componentes de UI que precisam ser acessados pelos eventos
    private ListView<Categoria> list;
    private TextField txtNome;
    private ColorPicker colorPicker; // Adicionei para ficar mais profissional
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
        Text titulo = new Text("Gerenciar Categorias");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // --- COLUNA DA ESQUERDA (LISTA) ---
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(300);

        Label lblList = new Label("Categorias Cadastradas");
        lblList.getStyleClass().add("section-title"); // Classe CSS

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
                    // Bolinha com a cor da categoria (opcional, visual)
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

        // --- COLUNA DA DIREITA (FORMULÁRIO) ---
        VBox rightPane = new VBox(15);
        rightPane.setPadding(new Insets(0, 0, 0, 20)); // Afasta da lista
        rightPane.setAlignment(Pos.TOP_LEFT);

        Label lblForm = new Label("Detalhes da Categoria");
        lblForm.getStyleClass().add("section-title");

        // Campos
        Label lblNome = new Label("Nome:");
        txtNome = new TextField();
        txtNome.setMaxWidth(300);

        Label lblCor = new Label("Cor (Etiqueta):");
        colorPicker = new ColorPicker(Color.GRAY);
        colorPicker.setMaxWidth(300);

        // Botões
        btnNova = new Button("Nova / Limpar");
        btnNova.setOnAction(e -> limparFormulario());

        btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("button-success"); // Verde
        btnSalvar.setOnAction(e -> salvar());

        btnExcluir = new Button("Excluir");
        btnExcluir.getStyleClass().add("button-danger"); // Vermelho
        btnExcluir.setDisable(true); // Começa desabilitado
        btnExcluir.setOnAction(e -> excluir());

        HBox buttonBox = new HBox(10, btnNova, btnSalvar, btnExcluir);

        rightPane.getChildren().addAll(lblForm, lblNome, txtNome, lblCor, colorPicker, buttonBox);

        // --- MONTAGEM FINAL ---
        view.setTop(titulo);
        view.setLeft(leftPane);
        view.setCenter(rightPane);
        BorderPane.setMargin(titulo, new Insets(0, 0, 20, 0));
    }

    private void selecionarCategoria(Categoria c) {
        this.categoriaSelecionada = c;
        txtNome.setText(c.getNome());
        // Tenta converter a string Hex para cor, se falhar usa cinza
        try {
            colorPicker.setValue(Color.web(c.getCor()));
        } catch (Exception e) {
            colorPicker.setValue(Color.GRAY);
        }

        btnSalvar.setText("Atualizar");
        btnExcluir.setDisable(false); // Pode excluir se tem algo selecionado
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

        // Converte a cor do ColorPicker para Hex String (#RRGGBB)
        String hexColor = toHexString(colorPicker.getValue());

        if (categoriaSelecionada == null) {
            // MODO CRIAR
            Categoria nova = new Categoria(UUID.randomUUID().toString(), nome, hexColor);
            repo.addCategoria(nova);
            list.getItems().add(nova);
            limparFormulario();
            showInfo("Categoria criada com sucesso!");
        } else {
            // MODO EDITAR
            categoriaSelecionada.setNome(nome);
            categoriaSelecionada.setCor(hexColor);
            repo.saveCategorias(); // Salva no JSON
            list.refresh(); // Atualiza o texto na lista visual
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

    // Utilitário para converter Cor do JavaFX para String Hex
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
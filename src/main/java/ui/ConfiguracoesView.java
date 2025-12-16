package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Categoria;
import persistence.RepositorioPersistencia;

import java.util.UUID;

/**
 * Tela de configurações simples: CRUD de categorias e export/import.
 */
public class ConfiguracoesView {
    private final RepositorioPersistencia repo;
    private final GridPane view;

    public ConfiguracoesView(RepositorioPersistencia repo) {
        this.repo = repo;
        this.view = new GridPane();
        build();
    }

    private void build() {
        view.setPadding(new Insets(10));
        view.setHgap(10);
        view.setVgap(10);

        ListView<Categoria> list = new ListView<>();
        list.setItems(FXCollections.observableArrayList(repo.getCategorias()));
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        TextField nome = new TextField();
        Button btnAdd = new Button("Adicionar");
        Button btnEditar = new Button("Editar Selecionada");
        Button btnRemover = new Button("Remover Selecionada");

        view.add(new Label("Categorias"), 0, 0);
        view.add(list, 0, 1, 2, 1);
        view.add(new Label("Nome"), 0, 2);
        view.add(nome, 1, 2);
        view.add(btnAdd, 0, 3);
        view.add(btnEditar, 1, 3);
        view.add(btnRemover, 2, 3);

        btnAdd.setOnAction(e -> {
            String n = nome.getText();
            if (n == null || n.trim().isEmpty()) {
                showAlert("Nome obrigatório");
                return;
            }
            Categoria c = new Categoria(UUID.randomUUID().toString(), n, "#AAAAAA");
            repo.addCategoria(c);
            list.getItems().add(c);
            nome.clear();
        });

        btnEditar.setOnAction(e -> {
            Categoria sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Selecione"); return; }
            String n = nome.getText();
            if (n == null || n.trim().isEmpty()) { showAlert("Nome obrigatório"); return; }
            sel.setNome(n);
            repo.saveCategorias();
            list.refresh();
        });

        btnRemover.setOnAction(e -> {
            Categoria sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Selecione"); return; }
            repo.removeCategoria(sel);
            list.getItems().remove(sel);
        });
    }

    public Node getView() {
        return view;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}
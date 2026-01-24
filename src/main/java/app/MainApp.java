package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import persistence.RepositorioPersistencia;
import ui.ConfiguracoesView;
import ui.HomeView;
import ui.NovaTransacaoView;
import util.Config;

import java.net.URL;
import java.util.Optional;

public class MainApp extends Application {
    private Stage primaryStage;
    private RepositorioPersistencia repositorio;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        Config.init(); // carrega configurações iniciais (pasta data etc)
        this.repositorio = RepositorioPersistencia.getInstance();
        repositorio.loadAll();

        BorderPane root = new BorderPane();
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        HomeView home = new HomeView(repositorio, this::showNovaTransacao, this::showConfiguracoes, this::refreshHome);
        root.setCenter(home.getView());

        Scene scene = new Scene(root, 1000, 600);

        // Ajuste: verifica se style.css existe antes de aplicar, evitando NPE em getResource()
        URL cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Aviso: style.css não encontrado em resources; continuando sem estilo.");
        }

        stage.setTitle("Gerenciador de Finanças Pessoais");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menuArquivo = new Menu("Arquivo");
        MenuItem nova = new MenuItem("Nova Transação");
        nova.setOnAction(e -> showNovaTransacao());
        MenuItem reiniciar = new MenuItem("Reiniciar Gestão");
        reiniciar.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Reiniciar Gestão");
            a.setHeaderText("Reiniciar dados");
            a.setContentText("Isto apagará todas as transações e recriará categorias padrão. Deseja continuar?");
            Optional<javafx.scene.control.ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                repositorio.resetData();
                refreshHome();
            }
        });
        MenuItem sair = new MenuItem("Sair");
        sair.setOnAction(e -> {
            repositorio.saveAll();
            primaryStage.close();
        });
        menuArquivo.getItems().addAll(nova, reiniciar, sair);

        Menu menuEditar = new Menu("Editar");
        MenuItem categorias = new MenuItem("Categorias / Configurações");
        categorias.setOnAction(e -> showConfiguracoes());
        menuEditar.getItems().add(categorias);

        menuBar.getMenus().addAll(menuArquivo, menuEditar);
        return menuBar;
    }

    private void showNovaTransacao() {
        NovaTransacaoView novaView = new NovaTransacaoView(repositorio, () -> refreshHome());
        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        pane.setCenter(novaView.getView());
    }

    private void showConfiguracoes() {
        ConfiguracoesView cfg = new ConfiguracoesView(repositorio);
        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        pane.setCenter(cfg.getView());
    }

    private void refreshHome() {
        BorderPane pane = (BorderPane) primaryStage.getScene().getRoot();
        HomeView home = new HomeView(repositorio, this::showNovaTransacao, this::showConfiguracoes, this::refreshHome);
        pane.setCenter(home.getView());
    }

    @Override
    public void stop() {
        repositorio.saveAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
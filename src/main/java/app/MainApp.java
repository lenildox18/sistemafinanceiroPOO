package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import persistence.RepositorioPersistencia;
import ui.ConfiguracoesView;
import ui.HomeView;
import ui.NovaTransacaoView;
import util.Config;

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

        HomeView home = new HomeView(repositorio, this::showNovaTransacao, this::showConfiguracoes);
        root.setCenter(home.getView());

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Gerenciador de Finanças Pessoais");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menuArquivo = new Menu("Arquivo");
        MenuItem nova = new MenuItem("Nova Transação");
        nova.setOnAction(e -> showNovaTransacao());
        MenuItem sair = new MenuItem("Sair");
        sair.setOnAction(e -> {
            repositorio.saveAll();
            primaryStage.close();
        });
        menuArquivo.getItems().addAll(nova, sair);

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
        HomeView home = new HomeView(repositorio, this::showNovaTransacao, this::showConfiguracoes);
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
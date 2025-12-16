package persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Categoria;
import model.Transacao;
import util.Config;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonPersistencia {
    private final Gson gson;
    private final Path dataDir;
    private final Path transacoesFile;
    private final Path categoriasFile;

    public JsonPersistencia() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDate.class, new util.adapters.LocalDateAdapter())
                .create();
        this.dataDir = Config.getDataDir();
        this.transacoesFile = dataDir.resolve("transacoes.json");
        this.categoriasFile = dataDir.resolve("categorias.json");
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            if (!Files.exists(transacoesFile)) Files.createFile(transacoesFile);
            if (!Files.exists(categoriasFile)) Files.createFile(categoriasFile);
        } catch (Exception e) {
            // ignore: será tratado em operações de I/O
        }
    }

    public List<Transacao> loadTransacoes() {
        try (Reader r = new FileReader(transacoesFile.toFile())) {
            Type listType = new TypeToken<List<Transacao>>() {}.getType();
            List<Transacao> list = gson.fromJson(r, listType);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Categoria> loadCategorias() {
        try (Reader r = new FileReader(categoriasFile.toFile())) {
            Type listType = new TypeToken<List<Categoria>>() {}.getType();
            List<Categoria> list = gson.fromJson(r, listType);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveTransacoes(List<Transacao> transacoes) {
        try (Writer w = new FileWriter(transacoesFile.toFile())) {
            gson.toJson(transacoes, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveCategorias(List<Categoria> categorias) {
        try (Writer w = new FileWriter(categoriasFile.toFile())) {
            gson.toJson(categorias, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
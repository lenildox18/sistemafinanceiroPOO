package persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Categoria;
import model.Transacao;
import util.Config;
import util.exceptions.CategoriaNaoEncontradaException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RepositorioPersistencia {
    private static RepositorioPersistencia instance;
    private List<Transacao> transacoes;
    private List<Categoria> categorias;
    private final JsonPersistencia json;

    private RepositorioPersistencia() {
        this.transacoes = new ArrayList<>();
        this.categorias = new ArrayList<>();
        this.json = new JsonPersistencia();
    }

    public static synchronized RepositorioPersistencia getInstance() {
        if (instance == null) instance = new RepositorioPersistencia();
        return instance;
    }

    public List<Transacao> getTransacoes() { return transacoes; }

    public List<Categoria> getCategorias() { return categorias; }

    public void addCategoria(Categoria c) {
        categorias.add(c);
        saveCategorias();
    }

    public void removeCategoria(Categoria c) {
        categorias.remove(c);
        saveCategorias();
    }

    public Optional<Categoria> findCategoriaById(String id) {
        return categorias.stream().filter(cat -> cat.getId().equals(id)).findFirst();
    }

    public Categoria requireCategoriaById(String id) throws CategoriaNaoEncontradaException {
        return findCategoriaById(id).orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada: " + id));
    }

    public void addTransacao(Transacao t) {
        transacoes.add(t);
        saveTransacoes();
    }

    public void removeTransacao(Transacao t) {
        transacoes.remove(t);
        saveTransacoes();
    }

    public void loadAll() {
        try {
            this.categorias = json.loadCategorias();
        } catch (Exception e) {
            this.categorias = new ArrayList<>();
        }
        try {
            this.transacoes = json.loadTransacoes();
        } catch (Exception e) {
            this.transacoes = new ArrayList<>();
        }
        // Se não houver categorias, criar algumas padrões
        if (categorias.isEmpty()) {
            categorias.add(new Categoria(UUID.randomUUID().toString(), "Salário", "#4CAF50"));
            categorias.add(new Categoria(UUID.randomUUID().toString(), "Alimentação", "#FF9800"));
            categorias.add(new Categoria(UUID.randomUUID().toString(), "Transporte", "#2196F3"));
            saveCategorias();
        }
    }

    public void saveAll() {
        saveCategorias();
        saveTransacoes();
    }

    public void saveTransacoes() {
        json.saveTransacoes(transacoes);
    }

    public void saveCategorias() {
        json.saveCategorias(categorias);
    }
}
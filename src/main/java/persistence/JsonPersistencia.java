package persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Categoria;
import model.Transacao;
import model.Receita;
import model.Despesa;
import model.Moeda;
import util.Config;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistência em JSON.
 * Ajustes:
 * - Serializa transações incluindo um campo "tipo" e a categoria embutida,
 *   evitando desserializar diretamente em classe abstrata.
 * - Desserializa criando instâncias de Receita/Despesa explicitamente.
 */
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
        List<Transacao> list = new ArrayList<>();
        try (Reader r = new FileReader(transacoesFile.toFile())) {
            JsonElement root = JsonParser.parseReader(r);
            if (root == null || !root.isJsonArray()) return new ArrayList<>();
            JsonArray arr = root.getAsJsonArray();
            for (JsonElement el : arr) {
                try {
                    JsonObject o = el.getAsJsonObject();
                    String tipo = o.has("tipo") && !o.get("tipo").isJsonNull() ? o.get("tipo").getAsString() : "Receita";
                    String id = o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsString() : java.util.UUID.randomUUID().toString();
                    LocalDate data = o.has("data") && !o.get("data").isJsonNull() ? LocalDate.parse(o.get("data").getAsString()) : LocalDate.now();
                    BigDecimal valorOriginal = o.has("valorOriginal") && !o.get("valorOriginal").isJsonNull() ? new BigDecimal(o.get("valorOriginal").getAsString()) : BigDecimal.ZERO;
                    BigDecimal valorBRL = o.has("valorBRL") && !o.get("valorBRL").isJsonNull() ? new BigDecimal(o.get("valorBRL").getAsString()) : null;
                    Moeda moeda = o.has("moeda") && !o.get("moeda").isJsonNull() ? Moeda.valueOf(o.get("moeda").getAsString()) : Moeda.BRL;
                    Categoria cat = null;
                    if (o.has("categoria") && !o.get("categoria").isJsonNull()) {
                        cat = gson.fromJson(o.get("categoria"), Categoria.class);
                    }
                    String descricao = o.has("descricao") && !o.get("descricao").isJsonNull() ? o.get("descricao").getAsString() : "";

                    Transacao t;
                    if ("Despesa".equalsIgnoreCase(tipo)) {
                        t = new Despesa(id, data, valorOriginal, moeda, cat, descricao);
                    } else {
                        t = new Receita(id, data, valorOriginal, moeda, cat, descricao);
                    }

                    if (valorBRL != null) t.setValorBRL(valorBRL);

                    list.add(t);
                } catch (Exception exInner) {
                    // pula registro inválido, mas não para toda a leitura
                    exInner.printStackTrace();
                }
            }
            return list;
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
            JsonArray arr = new JsonArray();
            for (Transacao t : transacoes) {
                JsonObject o = new JsonObject();
                o.addProperty("tipo", t instanceof Despesa ? "Despesa" : "Receita");
                o.addProperty("id", t.getId());
                o.addProperty("data", t.getData().toString());
                o.addProperty("valorOriginal", t.getValorOriginal().toString());
                if (t.getValorBRL() != null) o.addProperty("valorBRL", t.getValorBRL().toString());
                o.addProperty("moeda", t.getMoeda().name());
                if (t.getCategoria() != null) {
                    // embute a categoria para restauração completa
                    o.add("categoria", gson.toJsonTree(t.getCategoria()));
                } else {
                    o.add("categoria", JsonNull.INSTANCE);
                }
                o.addProperty("descricao", t.getDescricao());
                arr.add(o);
            }
            gson.toJson(arr, w);
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
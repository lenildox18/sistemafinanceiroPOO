package model;

import java.util.Objects;

public class Categoria {
    private String id;
    private String nome;
    private String cor; // representação simples (hex) para gráfico

    public Categoria(String id, String nome, String cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor == null ? "#888888" : cor;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        Categoria that = (Categoria) o;
        return Objects.equals(id, that.id);
    }
}
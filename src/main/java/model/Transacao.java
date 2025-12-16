package model;

import export.Exportavel;
import util.DateUtils;
import util.exceptions.DataInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Transacao {
    private String id;
    private LocalDate data;
    private BigDecimal valorOriginal; // no caso de moeda diferente, valor na moeda original
    private BigDecimal valorBRL; // valor convertido e salvo
    private Moeda moeda;
    private Categoria categoria;
    private String descricao;

    public Transacao(String id, LocalDate data, BigDecimal valorOriginal, Moeda moeda, Categoria categoria, String descricao) throws DataInvalidaException {
        setData(data);
        this.id = id;
        setValorOriginal(valorOriginal);
        this.moeda = moeda == null ? Moeda.BRL : moeda;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public String getId() { return id; }

    public LocalDate getData() { return data; }

    public void setData(LocalDate data) throws DataInvalidaException {
        if (data == null) throw new DataInvalidaException("Data nula");
        if (DateUtils.isFuture(data)) {
            // política: permitir mas avisar — aqui apenas validamos
            // pode-se pedir confirmação na UI
        }
        this.data = data;
    }

    public BigDecimal getValorOriginal() { return valorOriginal; }

    public void setValorOriginal(BigDecimal v) {
        if (v == null) throw new IllegalArgumentException("Valor nulo");
        if (v.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Valor deve ser positivo");
        this.valorOriginal = v;
    }

    public BigDecimal getValorBRL() { return valorBRL; }

    public void setValorBRL(BigDecimal valorBRL) {
        this.valorBRL = valorBRL;
    }

    public Moeda getMoeda() { return moeda; }

    public Categoria getCategoria() { return categoria; }

    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    // polimorfismo: impacto no saldo
    public abstract BigDecimal impactoNoSaldo();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transacao transacao = (Transacao) o;
        return Objects.equals(id, transacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
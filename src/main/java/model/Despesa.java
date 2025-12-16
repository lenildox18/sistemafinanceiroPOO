package model;

import util.exceptions.DataInvalidaException;
import util.exceptions.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Despesa extends Transacao {
    public Despesa(String id, LocalDate data, BigDecimal valorOriginal, Moeda moeda, Categoria categoria, String descricao) throws DataInvalidaException {
        super(id, data, valorOriginal, moeda, categoria, descricao);
    }

    @Override
    public BigDecimal impactoNoSaldo() {
        // despesa reduz o saldo: retornamos valor negativo
        BigDecimal v = getValorBRL() == null ? getValorOriginal() : getValorBRL();
        return v.negate();
    }
}
package model;

import util.exceptions.DataInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Receita extends Transacao {
    public Receita(String id, LocalDate data, BigDecimal valorOriginal, Moeda moeda, Categoria categoria, String descricao) throws DataInvalidaException {
        super(id, data, valorOriginal, moeda, categoria, descricao);
    }

    @Override
    public BigDecimal impactoNoSaldo() {
        // receita aumenta o saldo: valor convertido em BRL
        return getValorBRL() == null ? getValorOriginal() : getValorBRL();
    }
}
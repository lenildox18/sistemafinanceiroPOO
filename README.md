

# Gerenciador de Finanças Pessoais (JavaFX + POO)

Projeto acadêmico (IFPB - Campus Esperança) desenvolvido em Java para gestão financeira pessoal. O sistema permite controle de receitas e despesas, conversão monetária em tempo real e persistência de dados.

## 🚀 Principais Funcionalidades
* **Interface Gráfica Moderna:** Dashboard com cards de resumo, gráficos interativos e tabelas estilizadas via CSS (Dark/Clean).
* **Gestão Completa:** CRUD de transações e gerenciamento de categorias (padrão Master-Detail).
* **Multi-Moeda:** Suporte nativo a Real (BRL), Dólar (USD) e Euro (EUR).
* **Cotação Online:** Integração com a **AwesomeAPI** para taxas de câmbio em tempo real (com cache inteligente).
* **Persistência:** Dados salvos automaticamente em JSON (`./data/`).
* **Relatórios:** Exportação de dados para TXT e PDF (via PDFBox).

## 🛠 Tecnologias e Requisitos
* **Linguagem:** Java 17+
* **Interface:** JavaFX (com CSS customizado)
* **Build:** Maven
* **Bibliotecas:** Gson (JSON), Apache PDFBox (Relatórios).

### Destaques de Orientação a Objetos (POO)
O projeto aplica conceitos fundamentais para garantir extensibilidade e manutenção:
* **Herança:** Classe base `Transacao` estendida por `Receita` e `Despesa`.
* **Polimorfismo:** Método `impactoNoSaldo()` comporta-se de forma distinta para créditos e débitos.
* **Encapsulamento:** Validações robustas nos *setters* e uso de exceções personalizadas (`SaldoInsuficienteException`).
* **Composição:** O `RepositorioPersistencia` gerencia coleções de objetos.
* **Interfaces:** Contrato `Exportavel` para gerar relatórios em múltiplos formatos.

## 📂 Estrutura do Projeto

```
src/main/
├── java/
│   ├── app/           # Launcher e MainApp
│   ├── model/         # Classes de domínio (Transacao, Categoria...)
│   ├── persistence/   # Gerenciamento de arquivos JSON
│   ├── service/       # CurrencyService (API) e RelatorioService
│   ├── ui/            # Views (Home, NovaTransacao, Config)
│   └── util/          # Utilitários de data e configuração
└── resources/
    └── style.css      # Estilização visual da interface

``` 

**⚙️ Como Rodar** - #Pré-requisitos* JDK 17 ou superior.
* Maven instalado.

**Execução via Maven**

Devido às modularização do JavaFX (versões 11+), o projeto utiliza uma classe `Launcher` para inicialização correta.

1. **Compile o projeto:**
```bash
mvn clean package

```


2. **Execute (apontando para o Launcher):**
```bash
mvn exec:java -Dexec.mainClass="app.Launcher"

```



*Nota: Ao iniciar pela primeira vez, a pasta `./data` será criada automaticamente.*

*📝 Notas Técnicas*
* **API de Câmbio:** Migrado de *exchangerate.host* para **[AwesomeAPI](https://docs.awesomeapi.com.br/)** (HTTPS, Gratuita e sem Key).
* **Cache:** O sistema armazena cotações em memória por 10 minutos para economizar requisições e garantir performance.
* **Estilização:** A interface não utiliza o visual padrão do JavaFX (Modena), aplicando um tema personalizado em `src/main/resources/style.css`.

*📜 Licença*

Projeto desenvolvido para fins educacionais no curso de Análise e Desenvolvimento de Sistemas (IFPB). Livre para estudo e modificação.

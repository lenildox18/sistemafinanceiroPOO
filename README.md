# Gerenciador de Finanças Pessoais com Cotação (Java)

Projeto exemplo em Java usando JavaFX para gerenciar receitas e despesas com suporte a conversão de moedas (USD/EUR → BRL), persistência por arquivos JSON e exportação de relatórios (TXT e PDF).

Sumário rápido
- Interface gráfica com 3 telas (Home, Nova Transação, Configurações/Categorias).
- Modelos orientados a objetos: Transacao (abstrata), Receita, Despesa, Categoria.
- Composição, herança, polimorfismo, encapsulamento, interfaces e exceções personalizadas.
- Persistência em JSON (`./data/transacoes.json`, `./data/categorias.json`, `./data/config.json`).
- Consome API pública de câmbio (exchangerate.host) com cache (10 minutos) e fallback para taxa manual.
- Exporta relatórios em TXT (obrigatório) e PDF (opcional — depende da biblioteca).
- Pacote sugerido: model, service, persistence, ui, util, export.

Requisitos atendidos
- OO: classes, composição (Categoria dentro de Transacao / Repositorio), herança (Transacao → Receita/Despesa), polimorfismo (impacto no saldo), encapsulamento (getters/setters com validação), interfaces (Exportavel), exceções personalizadas.
- GUI JavaFX com ao menos 3 telas.
- Persistência por arquivos JSON.
- Consumo de API de câmbio com cache e tratamento de erros.
- Exportação em TXT e PDF (usando PDFBox).
- Mensagens de erro na UI.
- Justificativas e observações (abaixo).

Justificativas de uso de conceitos OO (resumo)
- Herança (Transacao abstrata): evita repetição de atributos e permite comportamentos distintos (Despesa valida saldo).
- Composição: RepositorioPersistencia "tem" coleções de transações e categorias; facilita salvar/carregar.
- Polimorfismo: método aplicarNoSaldo() em Transacao é implementado por Receita/Despesa de forma diferente.
- Encapsulamento: validações no setter de data e valor; exceções lançadas para regras de domínio.
- Interfaces: Exportavel define contrato para geração de relatórios em diferentes formatos.
- Exceções personalizadas: tornam regras de negócio previsíveis e fáceis de tratar na camada de UI.

Observações / Omissões justificadas
- Não foi implementado Controle de Usuário (Usuario) para manter foco nos requisitos essenciais. Poderia ser incluído facilmente.
- Internacionalização foi preparada (mensagens em pt-BR no código) — integração completa para en-US pode ser adicionada.
- Persistência por arquivos JSON foi escolhida por legibilidade e facilidade de inspeção. Serialização binária poderia economizar espaço, mas reduziria portabilidade.

Como rodar
1. Pré-requisitos:
   - JDK 17+ instalado.
   - Maven (para construir) ou configurar dependências manualmente.
   - JavaFX SDK (se estiver usando execução direta, pode ser necessário apontar --module-path).

2. Build (Maven):
   - `mvn clean package`
   - Para executar com Maven + JavaFX (exemplo):
     mvn exec:java -Dexec.mainClass="app.MainApp"

   Observação: dependendo da sua versão de Java e JavaFX, você pode precisar configurar o `--module-path` e `--add-modules javafx.controls,javafx.fxml`.

3. Dados
   - Ao primeiro uso os arquivos serão criados automaticamente em `./data`.
   - Você pode importar/exportar JSON via funcionalidades na tela de configurações.

Estrutura de arquivos (resumida)
- src/main/java/
  - app.MainApp (inicia JavaFX)
  - model.*
  - persistence.*
  - service.CurrencyService, RelatorioService
  - export.Exportavel, RelatorioMensal
  - ui.HomeView, NovaTransacaoView, ConfiguracoesView
  - util.Config, DateUtils, exceptions/*

Notas técnicas
- API de câmbio: https://exchangerate.host — usada para realizar conversões e obter taxas.
- Cache de cotações: 10 minutos por par de moedas.
- Exportação em PDF usa Apache PDFBox.
- JSON via Gson.

Testes
- Inclui testes básicos para CurrencyService (checagem de cache e fallback).
- Testes mais extensos podem ser adicionados para cobertura do domínio.

Licença e uso
- Código de exemplo, livre para estudo e modificação. Bibliotecas externas podem ter suas próprias licenças (PDFBox, Gson, JavaFX).

Abaixo seguem todos os arquivos fonte principais para compilar/executar o projeto.
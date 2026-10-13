# Log de Decisões de Arquitetura — App Guardião SMS

Este documento existe para justificar, item a item, as escolhas técnicas do aplicativo perante a banca. Cada decisão tem: **o que foi decidido**, **por quê**, e **quem decidiu** (banca, orientador, ou decisão técnica do desenvolvimento).

---

## 1. Decisões já fixadas pela banca (documento "Diretrizes Pós Pré-Banca")

Estas não são mais escolhas em aberto — são requisitos.

| # | Decisão | Origem / Razão |
|---|---|---|
| 1 | Produto final = aplicativo Android nativo | Bossini, Nelson e Milkes exigiram um produto que materialize o TCC; Nelson não conseguia identificar o usuário final sem ele |
| 2 | Usuário final: pessoas vulneráveis a golpes (idosos, baixo letramento digital) | Público já descrito na introdução do trabalho |
| 3 | Toda inferência ocorre localmente no dispositivo (on-device), sem servidor externo | Sustenta a promessa de privacidade e conformidade com a LGPD |
| 4 | Modelo: BERTimbau fine-tunado (único modelo real do sistema) | Já treinado e avaliado no TCC 1 (acurácia 96%, threshold 0,3) |
| 5 | Gemini não faz parte do app em produção | Foi usado apenas na fase de geração sintética do dataset — precisa ficar explícito no texto para não confundir a banca |
| 6 | Conversão do modelo para ONNX + quantização dinâmica INT8 | Reduz de ~440MB para ~110MB, viabilizando embarque no celular |
| 7 | Runtime de inferência: ONNX Runtime Mobile | Suporte oficial ao Android, mais simples que alternativas |
| 8 | Captura de mensagens: `BroadcastReceiver` + permissão `RECEIVE_SMS` | Única via estável no Android; WhatsApp fica como trabalho futuro (sem API de leitura de conteúdo por proteção de privacidade do sistema) |
| 9 | Padrão de código: MVVM | Recomendado pelo Google para Android; facilita defesa perante a banca |
| 10 | Threshold de classificação: 0,3 | Calibrado nos experimentos do TCC 1 para priorizar recall da classe "golpe" |
| 11 | Linguagem/IDE: Kotlin + Android Studio | Já prevista na metodologia do trabalho |
| 12 | UI: Jetpack Compose | Toolkit declarativo moderno, mais produtivo que XML |

## 2. Decisões definidas nesta etapa de desenvolvimento (13/07/2026)

| # | Decisão | Razão |
|---|---|---|
| 13 | Escopo da v1: alerta em tempo real + tela de histórico + tela de configurações (ajuste de sensibilidade) | Confirmado pelo responsável do TCC como escopo desejado para a entrega de agosto |
| 14 | Nome do app: **Guardião SMS** | Nome em português, simples e direto, alinhado ao público-alvo (idosos/baixo letramento digital) descrito no trabalho. **Isso é um placeholder — pode e deve ser trocado se já existir um nome definido para o produto.** |
| 15 | Package: `com.tcc.guardiaosms` | Convenção padrão Android (domínio reverso); sem significado além de identificar o projeto |
| 16 | Persistência do histórico: Room (SQLite local) | Já que toda a análise é local, o histórico também precisa ser local — sem trocar a promessa de privacidade por conveniência de nuvem |
| 17 | Preferências (threshold ajustável, notificações): DataStore (Preferences) | Substituto moderno recomendado pelo Google para SharedPreferences |
| 18 | Camada de classificação isolada atrás de uma interface (`ClassifierRepository`), com duas implementações: `MockClassifier` (provisória) e `OnnxClassifier` (real) | **Decisão crítica de sequenciamento**: o modelo fine-tunado (pesos Hugging Face) ainda não foi convertido para ONNX nem enviado a este ambiente de desenvolvimento. Para não travar o desenvolvimento do app enquanto a conversão do modelo é feita, a UI, o banco de dados, a captura de SMS e as notificações são construídos e testáveis desde já contra uma implementação simulada. Quando o `.onnx` estiver pronto, a troca é de um único arquivo — o resto do app não muda. Isso também é defensável como boa prática de engenharia (inversão de dependência) |
| 19 | minSdk 26 (Android 8.0) / targetSdk 34 | Equilíbrio entre compatibilidade com aparelhos mais simples/antigos (público idoso tende a trocar de celular com menos frequência) e recursos modernos (notificações com canais, exigido a partir do Android 8) |
| 20 | Tokenizador: porte manual do WordPiece (mesmo vocabulário do BERTimbau) para Kotlin, em vez de depender de uma lib pronta | Já sinalizado nas diretrizes como ponto de atenção; depende do arquivo `vocab.txt` do tokenizer treinado, que ainda precisa ser enviado junto com os pesos do modelo |
| 21 | Script de exportação ONNX validado nesta sessão com um modelo BERT de arquitetura idêntica, porém com pesos aleatórios (não os pesos reais do fine-tuning) | Este ambiente de desenvolvimento não tem acesso de rede ao Hugging Face Hub (apenas PyPI e GitHub são permitidos), então não foi possível baixar o BERTimbau nem os pesos reais para testar. A validação com pesos aleatórios confirma que o pipeline de exportação e quantização funciona mecanicamente; a exportação real ainda precisa ser rodada com o checkpoint verdadeiro |
| 22 | Exportação ONNX forçada a usar o exportador "legado" (`dynamo=False`) do PyTorch, opset 17 | O exportador novo ("dynamo"), padrão em PyTorch recente, falhou ao tentar fazer downgrade automático para opset 14 (erro de conversão de `LayerNormalization`). O exportador legado é mais maduro para grafos de transformers e mais previsível para compatibilidade com o ONNX Runtime Mobile |
| 23 | Script de conversão renomeia explicitamente o arquivo de vocabulário para `vocab.txt` | `tokenizer.save_vocabulary()` não retornou um arquivo `vocab.txt` na versão do `transformers` instalada aqui (retornou `tokenizer.model`, indicando que a lib mudou o backend interno de tokenização); o script agora não depende desse nome e sempre normaliza para `vocab.txt`, que é o nome esperado por `WordPieceTokenizer.kt` |
| 24 | Distribuição: só via APK instalado diretamente (demonstração para a banca), não pela Google Play Store | Confirmado com o responsável do TCC em 13/07/2026. Relevante porque a Play Store restringe a permissão `RECEIVE_SMS` a apps que sejam o SMS padrão do aparelho (ou poucas exceções) — como o app não será publicado na loja, essa restrição não se aplica agora. **Se decidirem publicar no futuro**, será necessário revisar essa parte (ex: registrar o app como handler de SMS padrão, ou solicitar exceção de política) |

| 25 | Adicionada uma quarta tela, "Testar mensagem": campo de texto livre + botão que classifica na hora, com exemplos pré-carregados | Duas razões. (a) **Praticidade de demonstração**: depender de receber um SMS real na frente da banca é frágil (sinal, emulador, outro aparelho). A tela remove essa dependência. (b) **Validação do modelo**: permite testar dezenas de mensagens rapidamente sem disparar SMS, útil para conferir o comportamento do BERTimbau depois de integrado. A tela mostra a probabilidade **e** o limiar configurado, deixando explícito por que aquela mensagem virou (ou não) alerta — o que ajuda a justificar o threshold de 0,3 perante a banca |
| 26 | A tela de teste **não** grava no histórico | O histórico representa mensagens realmente recebidas pelo usuário; misturar testes manuais ali distorceria a leitura da tela e atrapalharia uma demonstração |
| 27 | Barra de probabilidade implementada com `Box` em vez de `LinearProgressIndicator` | A assinatura do `LinearProgressIndicator` mudou entre versões do Material3 (o parâmetro `progress` passou de `Float` para `() -> Float`); usar `Box` elimina o risco de erro de compilação conforme a versão resolvida pelo BOM |

## 3. Pendências (bloqueadas até vocês fornecerem os arquivos)

- [ ] **Atualização (13/07/2026):** não existe checkpoint salvo em lugar nenhum — o `fine_tuning.py` baixa o BERTimbau base e treina do zero toda vez que é executado; a pasta `modelo_smishing_final/` só existe durante/depois de uma execução local (inclusive está no `.gitignore` do repositório, então nunca foi versionada). Portanto é necessário **rodar o treinamento novamente** para gerar os pesos.
- [ ] Rodar `fine_tuning.py` (sem alterações — o script já termina com `model.save_pretrained("./modelo_smishing_final")`) em um ambiente com internet e, de preferência, GPU (ex: o mesmo Colab/Kaggle usado antes). Este ambiente de desenvolvimento (usado por mim, Claude) não consegue rodar esse treinamento porque não tem acesso de rede ao Hugging Face Hub (só PyPI e GitHub são permitidos aqui) — o download do `neuralmind/bert-base-portuguese-cased` falharia
- [ ] Enviar a pasta `modelo_smishing_final/` resultante (zipada) — por upload direto no chat, ou, se for grande demais, como asset de uma Release no GitHub do projeto (consigo baixar diretamente de lá)
- [ ] Confirmar qual versão de `torch` e `transformers` foi usada no treinamento (`fine_tuning.py`) — a versão mais recente do PyPI mudou o comportamento interno do tokenizer (ver decisão 23) e pode ter outras incompatibilidades ao carregar checkpoints antigos
- [ ] Rodar a conversão real para ONNX + quantização INT8
- [ ] Extrair o `vocab.txt` real para portar o tokenizer em Kotlin
- [ ] Substituir `MockClassifier` por `OnnxClassifier` real no módulo de injeção de dependência
- [ ] Testar em dispositivo/emulador Android real (este ambiente de desenvolvimento não tem Android SDK/emulador disponível — a compilação final precisa ser feita no Android Studio de vocês)

## 4. Decisões de interface e acessibilidade (13/10/2026)

Objetivo: adequar a interface ao usuário final (decisão 2: idosos / baixo letramento digital) — frases curtas, linguagem simples, letra e botões grandes, contraste alto e respeito ao tamanho de fonte do sistema. **Nada aqui altera a classificação**: modelo, tokenizador, limiar padrão (0,3) e a regra `probabilidade >= limiar` continuam iguais.

| # | Decisão | Razão |
|---|---|---|
| 28 | O usuário vê um **nível de risco em palavras** ("Risco alto", "Atenção", "Parece segura") em vez de porcentagem (Início, Histórico, detalhe e notificação) | Porcentagem é uma abstração difícil para o público-alvo; um rótulo curto, com ícone e cor, é entendido de imediato. O nível nunca depende só da cor (há ícone e texto) |
| 29 | Mapeamento: **Risco alto** = `p >= limiar` (exatamente a decisão de alerta atual, única que notifica); **Atenção** = `limiar/2 <= p < limiar`; **Parece segura** = `p < limiar/2` | "Risco alto" coincide com o alerta, então nenhuma mensagem muda de status. "Atenção" marca a faixa logo abaixo do limiar, em que o modelo não teve certeza nem para alertar nem para considerar segura; por ser relativa ao limiar, acompanha o ajuste de sensibilidade sem criar um segundo número fixo. Na prática "Atenção" é rara: no conjunto de teste, o modelo v1 dá probabilidades quase sempre próximas de 0 ou 1 (métricas iguais nos limiares 0,3, 0,4 e 0,5). No Histórico, o nível usa a decisão e o limiar gravados no momento em que o SMS chegou, para coincidir com o alerta que foi (ou não) dado |
| 30 | A **porcentagem aparece só na tela Testar** (para a banca), com 1 casa decimal e vírgula, **truncada** (0,9997 → "99,9%") | A banca precisa ver o número e o limiar para entender a decisão; o usuário final, não. Truncar evita exibir "100,0%", que sugeriria uma certeza absoluta que o modelo não tem |
| 31 | **Tela de detalhe do alerta**, aberta ao tocar na notificação ou num item do Histórico: nível em destaque, remetente, data, mensagem completa, pontos de atenção e guia "O que fazer" | Antes a notificação não abria nada, e o usuário ficava sem saber o que fazer. A mensagem é exibida como texto simples: links **não** ficam clicáveis |
| 32 | **Notificação** com texto curto ("Cuidado: esta mensagem pode ser golpe. Toque para ver o que fazer.") e **sem o corpo do SMS**; o `PendingIntent` usa o id da mensagem como `requestCode` | Linguagem simples e privacidade (o corpo do SMS ficaria visível na tela de bloqueio). Com `FLAG_IMMUTABLE` e `requestCode` repetido, o Android reaproveitaria o intent da primeira notificação e um segundo alerta abriria a mensagem errada |
| 33 | **Pontos de atenção**: regras simples de texto (`AttentionPointsDetector`, com teste unitário) que apontam link/encurtador, pedido de dinheiro, pedido de senha/código/dados, urgência ou ameaça, telefone para contato e texto disfarçado (letras de outros alfabetos misturadas, letras "estilizadas" ou caracteres invisíveis). A tela diz explicitamente: "Estas dicas são uma verificação simples do texto. A decisão de alerta é feita pelo modelo de IA." | Ajudam o usuário a aprender a reconhecer golpes, sem fingir ser a explicação do modelo (o BERTimbau não fornece justificativas). As dicas não alteram o nível de risco. Para reduzir falsos sinais: "hoje" só conta em combinações ("expira hoje", "até hoje", "ainda hoje"...), "código" só conta junto de pedido ("envie o código", "código de verificação") — não "código de rastreio" —, "boleto" só conta com urgência ou link, e números colados a letras (ex.: "BR123456789") não contam como telefone. Quando o nível é "Parece segura" e há dicas, o título vira "Mesmo assim, fique atento a:" |
| 34 | **Guia "O que fazer"** com cuidados fixos e curtos (não clicar em links; não enviar senhas, códigos ou dados; não fazer Pix/transferência; não ligar para números da mensagem; confirmar pelo número salvo ou app oficial; pedir ajuda a alguém de confiança). Havendo pedido de dinheiro, um cartão em destaque orienta a confirmar com a pessoa pelo número antigo | Orientação acionável em linguagem simples. O destaque cobre o golpe do falso parente, comum entre idosos |
| 35 | **Modo escuro**: esquemas de cor claro e escuro completos, cores de risco semânticas (`RiskColors`) que acompanham o tema, barras de sistema conforme o tema do app (edge-to-edge) e opção nos Ajustes "Automático (segue o celular) / Claro / Escuro", salva no DataStore, com Automático como padrão | Cores fixas (fundos claros) ficavam ilegíveis no modo escuro do celular. Um teste unitário (`ColorContrastTest`) verifica o contraste WCAG nos dois temas: texto >= 4,5:1 e ícones/barras/bordas >= 3:1 |
| 36 | **Tamanhos**: botões e linhas clicáveis com no mínimo 56dp (acima dos 48dp recomendados); fontes maiores que o padrão do Material 3, todas em `sp`; telas roláveis e sem altura fixa em textos | Respeita o tamanho de fonte escolhido no celular (inclusive 200%) sem cortar conteúdo, e facilita o toque para quem tem menos precisão motora |
| 37 | Todos os textos da interface ficam em `strings.xml` (inclusive os exemplos da tela Testar) | Facilita revisar a linguagem num único lugar e uma eventual tradução |

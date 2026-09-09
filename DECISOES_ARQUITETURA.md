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

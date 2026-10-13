# Guardião SMS

Aplicativo Android desenvolvido como produto final do TCC "Construção de um
dataset sintético para a detecção de smishing no contexto brasileiro",
conforme exigido pela banca na pré-banca (ver diretrizes do orientador).

Detecta, **localmente no aparelho** (sem enviar nada para servidores externos,
em conformidade com a LGPD), SMS suspeitos de golpe (smishing), usando um
modelo BERTimbau fine-tunado e convertido para ONNX.

## Status atual (13/10/2026)

O app usa o **BERTimbau fine-tunado real** ([`Wontsimt/detector-phishing-v3-final`](https://huggingface.co/Wontsimt/detector-phishing-v3-final),
treinado com o `dataset_v7`).

- O modelo foi convertido para ONNX e quantizado em **INT8 dinâmico per-channel** (a "variante v1").
- A inferência roda **100% no aparelho**, com o ONNX Runtime.
- O tokenizador WordPiece foi portado para Kotlin e gera os mesmos IDs do Hugging Face.
- O `MockClassifier` continua no código só para testes de interface. O app usa o `OnnxClassifier` (ver `di/AppContainer.kt`).

O que o app faz hoje:

- **Alerta por notificação:** quando chega um SMS com "Risco alto", o app avisa com um texto curto. Tocar na notificação abre o detalhe da mensagem.
- **Detalhe do alerta:** mostra o nível de risco ("Risco alto", "Atenção" ou "Parece segura"), a mensagem completa e os pontos de atenção (dicas de leitura, como link, pedido de Pix ou urgência), além do guia "O que fazer".
- **Histórico** das mensagens recebidas, com o nível de risco de cada uma.
- **Testar:** analisa um texto digitado na hora, mostrando a probabilidade e o limiar. Serve para a demonstração à banca.
- **Ajustes:**
  - tema automático, claro ou escuro;
  - sensibilidade do alerta (limiar padrão 0,3);
  - notificações ligadas ou desligadas.

As decisões técnicas e de interface estão em `DECISOES_ARQUITETURA.md`.

## Resultados (conjunto de teste, 510 mensagens)

| Modelo | Acurácia | Recall (golpe) |
|---|---|---|
| BERTimbau original (PyTorch) | 95,7% | 97,4% |
| **Modelo no app (ONNX INT8 per-channel, v1)** | **96,3%** | **97,8%** |
| Baseline TF-IDF + Regressão Logística | 88,4% | 86,2% |

- O conjunto de teste é o mesmo do `fine_tuning.py`: divisão 70/15/15 estratificada do `dataset_v7`, com 232 golpes e 278 legítimas. As métricas usam o limiar 0,5.
- O modelo do app tem desempenho **equivalente ao original**. A pequena diferença a favor da v1 está dentro do ruído da quantização (uma mensagem a mais ou a menos).
- O detalhamento está em [`conversao_onnx/RESULTADOS.md`](https://github.com/Wontsz/TCC-Mitigar-Golpes-Digitais/blob/main/conversao_onnx/RESULTADOS.md), no repositório de ML. Lá estão as outras variantes testadas, os tempos por mensagem, as métricas por limiar e a comparação com o modelo antigo.

## Como obter o modelo

O `app/src/main/assets/model.onnx` **não vai para o git**: ele tem cerca de 105 MB, acima do limite de 100 MB do GitHub.

- Ele é gerado pelos scripts da pasta `conversao_onnx/` do repositório de ML, [Wontsz/TCC-Mitigar-Golpes-Digitais](https://github.com/Wontsz/TCC-Mitigar-Golpes-Digitais). O passo a passo está no README dele.
- O arquivo usado é `conversao_onnx/saida/variantes/v1_per_channel.onnx`. Copie-o para:

```
app/src/main/assets/model.onnx
```

**SHA-256 esperado:** `d93dc8974fc2f45bf909e4b257dcf3e65062de040d28306c4f70af202f0a3463` (110.115.256 bytes)

Para conferir no PowerShell, a partir da pasta do projeto:

```powershell
(Get-FileHash app\src\main\assets\model.onnx -Algorithm SHA256).Hash
# deve imprimir D93DC8974FC2F45BF909E4B257DCF3E65062DE040D28306C4F70AF202F0A3463
```

O `vocab.txt` (vocabulário do tokenizador) já está versionado em `app/src/main/assets/`. Sem o `model.onnx`, o app compila, mas a classificação falha. Não existe recurso automático para cair no `MockClassifier`.

## Como abrir

1. Coloque o `model.onnx` em `app/src/main/assets/` (ver seção anterior).
2. Abra a pasta `GuardiaoSMS/` no Android Studio, versão Koala (2024.1) ou mais recente. O projeto usa AGP 8.5, Kotlin 1.9.24 e Gradle 8.7.
3. Deixe o Gradle sincronizar. Na primeira sincronização ele baixa as dependências e precisa de internet.
4. Rode em um emulador ou aparelho com Android 8.0 (API 26) ou superior.
5. Na primeira abertura, permita as notificações e a leitura de SMS (botão "Permitir" na tela Início).

Também é possível compilar pela linha de comando. No Windows, use o Java que vem com o Android Studio:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

## Testar com SMS simulado no emulador

1. Com o app aberto no emulador e a permissão de SMS concedida, abra os **Extended Controls**: os "..." na barra lateral do emulador.
2. Vá em **Phone**, preencha o remetente ("From") e o texto, e clique em **Send Message**.
3. Para ver um alerta, envie por exemplo: `URGENTE: sua conta sera bloqueada hoje. Regularize seu CPF agora: bit.ly/regularizar`.
   - Deve aparecer a notificação "Risco alto: pode ser golpe".
   - Ao tocar nela, abre o detalhe com os pontos de atenção e o guia "O que fazer".
4. Uma mensagem legítima, como `Oi mae, cheguei bem em casa. Te ligo mais tarde!`, aparece no Histórico como "Parece segura", sem notificação.

Para testar sem SMS, use a aba **Testar**, que tem exemplos prontos.

## Testes unitários

| Teste | O que verifica |
|---|---|
| `WordPieceTokenizerTest` | O tokenizador em Kotlin gera **os mesmos IDs do tokenizer do Hugging Face** em **3.408/3.408** mensagens: todo o `dataset_v7` mais 12 casos extras (pontuação Unicode, caracteres invisíveis, emoji, CJK...). |
| `AttentionPointsDetectorTest` | As regras dos pontos de atenção, inclusive mensagens legítimas que não podem gerar dica. |
| `RiskLevelTest` | O mapeamento dos níveis de risco. "Risco alto" é exatamente a decisão de alerta (`p >= limiar`). |
| `ColorContrastTest` | O contraste WCAG das cores nos temas claro e escuro: texto >= 4,5:1, ícones e barras >= 3:1. |
| `FormattersTest` | A porcentagem truncada com vírgula ("99,9%") e a data. |

Para rodar:

```powershell
.\gradlew.bat testDebugUnitTest
```

Ou, no Android Studio, clique com o botão direito em `app/src/test` e escolha **Run 'Tests in ...'**.

## Estrutura

```
app/src/main/java/com/tcc/guardiaosms/
├── classifier/     # Interface do classificador, OnnxClassifier (real), WordPieceTokenizer e MockClassifier (testes de UI)
├── data/           # Room (histórico) e DataStore (limiar, notificações, tema)
├── di/             # Container manual de dependências + factory de ViewModels
├── notification/   # Notificação de alerta (abre o detalhe da mensagem)
├── risk/           # Níveis de risco (Risco alto / Atenção / Parece segura)
├── sms/            # BroadcastReceiver de captura de SMS
├── tips/           # Pontos de atenção (regras simples de texto)
└── ui/             # Telas em Compose: início, histórico, detalhe, testar, ajustes, tema e componentes
```

## Pendências

Falta o teste em aparelho físico com chip, recebendo SMS reais. Ver a seção "Pendências" em `DECISOES_ARQUITETURA.md`.

# Guardião SMS

Aplicativo Android desenvolvido como produto final do TCC "Construção de um
dataset sintético para a detecção de smishing no contexto brasileiro",
conforme exigido pela banca na pré-banca (ver diretrizes do orientador).

Detecta, **localmente no aparelho** (sem enviar nada para servidores externos,
em conformidade com a LGPD), SMS suspeitos de golpe (smishing), usando um
modelo BERTimbau fine-tunado e convertido para ONNX.

## Status atual (13/07/2026)

🚧 **MVP em desenvolvimento.** A UI, o histórico, as configurações, a captura
de SMS e as notificações estão implementados e funcionais. **O modelo real
ainda não está integrado** — o app usa um classificador provisório
(`MockClassifier`) baseado em heurística de palavras-chave, só para permitir
testar o app de ponta a ponta enquanto o modelo fine-tunado é convertido
para ONNX. Ver `DECISOES_ARQUITETURA.md` para o detalhamento completo e a
lista de pendências.

## Como abrir

1. Abra a pasta `GuardiaoSMS/` no Android Studio (versão recomendada:
   Koala/2024.1 ou mais recente, compatível com AGP 8.5 e Kotlin 1.9.24).
2. Deixe o Gradle sincronizar (vai baixar as dependências do Google
   Maven/Maven Central — precisa de internet na primeira sincronização).
3. Rode em um emulador ou aparelho físico com Android 8.0 (API 26) ou
   superior.
4. Na primeira abertura, conceda a permissão de leitura de SMS na tela
   inicial.

## Estrutura

```
app/src/main/java/com/tcc/guardiaosms/
├── classifier/     # Interface do classificador + Mock (atual) + ONNX (real, pendente)
├── data/           # Room (histórico) e DataStore (preferências/threshold)
├── di/             # Container manual de dependências + factory de ViewModels
├── notification/   # Disparo do alerta de golpe
├── sms/            # BroadcastReceiver de captura de SMS
└── ui/             # Telas em Compose: home, history, settings + navegação
```

## Testar sem o modelo real

O app funciona hoje com `MockClassifier`, que usa palavras-chave e padrões de
URL (os mesmos sinais superficiais identificados como viés no TCC 1) para
simular um alerta. Envie a si mesmo um SMS de teste contendo, por exemplo,
"URGENTE: sua conta será bloqueada, regularize agora: bit.ly/teste" para ver
o alerta disparar.

## Próximos passos

Ver a seção "Pendências" em `DECISOES_ARQUITETURA.md`. Resumo: enviar o
checkpoint do fine-tuning para conversão em ONNX (`model_conversion/`) e
trocar `MockClassifier` por `OnnxClassifier` em `AppContainer.kt`.

Esta pasta precisa conter:

- vocab.txt   vocabulário do tokenizador (BERTimbau, 29.794 tokens). Já está
              versionado no git.
- model.onnx  modelo fine-tunado Wontsimt/detector-phishing-v3-final, convertido
              para ONNX e quantizado em INT8 dinâmico per-channel (variante v1).
              NÃO vai para o git, porque passa do limite de 100 MB do GitHub.

De onde vem o model.onnx: ele é gerado pela pasta conversao_onnx/ do repositório
https://github.com/Wontsz/TCC-Mitigar-Golpes-Digitais (o passo a passo está no
README de lá). Copie conversao_onnx/saida/variantes/v1_per_channel.onnx para
esta pasta com o nome model.onnx.

SHA-256 esperado do model.onnx (110.115.256 bytes):
d93dc8974fc2f45bf909e4b257dcf3e65062de040d28306c4f70af202f0a3463

Para conferir no PowerShell:
(Get-FileHash app\src\main\assets\model.onnx -Algorithm SHA256).Hash

Sem o model.onnx, o app compila, mas a classificação falha. NÃO existe recurso
automático para cair no MockClassifier, que hoje serve só para testes de
interface (ver AppContainer.kt e DECISOES_ARQUITETURA.md).

Se o model.onnx for trocado por outro modelo, incremente MODEL_VERSION em
OnnxClassifier.kt. Assim os aparelhos que já tinham o app copiam o modelo novo.

Esta pasta precisa conter, antes do app funcionar com o modelo real:

- model.onnx  (gerado por model_conversion/convert_to_onnx.py)
- vocab.txt   (gerado pelo mesmo script)

Sem esses dois arquivos, o app funciona normalmente, mas usa o
MockClassifier (heurística provisória) em vez do BERTimbau real —
ver AppContainer.kt e DECISOES_ARQUITETURA.md.

package com.tcc.guardiaosms.tips

import java.text.Normalizer

/**
 * Sinais de leitura ("pontos de atenção") encontrados no texto de uma mensagem.
 *
 * IMPORTANTE: são dicas para o usuário ler a mensagem com cuidado, obtidas por
 * regras simples de texto. NÃO são o motivo da decisão de alerta, que é feita
 * apenas pelo modelo de IA (BERTimbau); as dicas também não alteram o nível de risco.
 */
enum class AttentionPoint {
    LINK,
    MONEY_REQUEST,
    DATA_REQUEST,
    URGENCY_OR_THREAT,
    PHONE_CONTACT,
    DISGUISED_TEXT
}

object AttentionPointsDetector {

    private const val TLDS =
        "com|br|net|org|ly|cc|gg|me|io|co|xyz|online|site|app|link|top|info|club|shop|vip|live|click"

    private val shortenerRegex = Regex(
        """\b(bit\.ly|tinyurl\.com|cutt\.ly|gg\.gg|tiny\.cc|is\.gd|t\.co|ow\.ly|rb\.gy|shorturl\.at|encurtador\.com\.br|abre\.ai)\b"""
    )
    private val urlRegex = Regex("""(https?://|www\.)\S+""")
    private val domainRegex = Regex("""\b[a-z0-9][a-z0-9-]*(\.[a-z0-9-]+)*\.($TLDS)\b(/\S*)?""")

    private val moneyRegex = Regex("""\br\$|\bpix\b|\btransfer\w*|\bempresta\w*|\bemprestimo\w*|\bdeposit\w*""")
    private val boletoRegex = Regex("""\bboletos?\b""")

    private val dataRegex = Regex("""\b(senhas?|cpf|cartao|cartoes|token|cvv)\b|\b(seus|teus) dados\b""")
    private val codeRequestRegex = Regex(
        """\b(envie|enviar|envia|informe|informar|informa|passe|passar|passa|digite|digitar|mande|mandar|manda|confirme|confirmar|repasse|repassar|me (passa|manda|envia))\s+(o\s+|esse\s+|este\s+|seu\s+)?codigo\b""" +
            """|\bcodigos?\s+de\s+(verificacao|seguranca|acesso|confirmacao)\b"""
    )

    private val urgencyRegex = Regex(
        """\b(urgente|urgencia|bloque\w*|ultimo dia|cancelamento|cancelad\w*|imediatamente|suspens\w*|expira\w*)\b""" +
            """|\b(expira|vence|ate|ainda|somente|so|apenas)\s+hoje\b"""
    )

    // Telefone brasileiro: +55 opcional, DDD opcional, 8 ou 9 dígitos com separadores;
    // ou 0800/4004/3003. Não pode estar colado a letras/dígitos (ex.: "BR123456789").
    private val phoneRegex = Regex(
        """(?<![\p{L}\d])(\+?55[\s.-]?)?(\(?\d{2}\)?[\s.-]?)?9?\d{4}[\s.-]?\d{4}(?![\p{L}\d])""" +
            """|(?<![\p{L}\d])(0800|4004|3003)[\s.-]?\d{3,4}[\s.-]?\d{0,4}(?![\p{L}\d])""" +
            """|\bwa\.me/"""
    )

    fun detect(text: String): Set<AttentionPoint> {
        val normalized = normalize(text)
        val found = linkedSetOf<AttentionPoint>()

        val hasLink = shortenerRegex.containsMatchIn(normalized) ||
            urlRegex.containsMatchIn(normalized) ||
            domainRegex.containsMatchIn(normalized)
        val hasUrgency = urgencyRegex.containsMatchIn(normalized)
        val hasMoney = moneyRegex.containsMatchIn(normalized) ||
            (boletoRegex.containsMatchIn(normalized) && (hasUrgency || hasLink))
        val hasData = dataRegex.containsMatchIn(normalized) || codeRequestRegex.containsMatchIn(normalized)

        if (hasLink) found += AttentionPoint.LINK
        if (hasMoney) found += AttentionPoint.MONEY_REQUEST
        if (hasData) found += AttentionPoint.DATA_REQUEST
        if (hasUrgency) found += AttentionPoint.URGENCY_OR_THREAT
        if (phoneRegex.containsMatchIn(normalized)) found += AttentionPoint.PHONE_CONTACT
        if (hasDisguisedText(text)) found += AttentionPoint.DISGUISED_TEXT

        // Ordem fixa (a do enum), para a tela ser previsível.
        return AttentionPoint.entries.filter { it in found }.toCollection(linkedSetOf())
    }

    /** Minúsculas e sem acentos, para as regras não dependerem de "código"/"codigo". */
    private fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")

    /**
     * Texto disfarçado:
     * (a) palavra que mistura letras latinas com letras de outra escrita (ex.: "CΛIXΛ");
     * (b) letras "estilizadas" (blocos Mathematical Alphanumeric e Fullwidth, ex.: "𝐁𝐚𝐧𝐜𝐨");
     * (c) caracteres invisíveis de formatação (categoria Cf), exceto o ZWJ (U+200D)
     *     e as "tags" de bandeiras, usados em emojis. Seletores de variação (U+FE0F) não são Cf.
     */
    private fun hasDisguisedText(text: String): Boolean {
        var hasLatin = false
        var hasOtherScript = false

        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            i += Character.charCount(cp)

            // ZWJ e "tags" (U+E0020..U+E007F) fazem parte de emojis compostos e de bandeiras.
            if (Character.getType(cp) == Character.FORMAT.toInt() && cp != 0x200D && cp !in 0xE0020..0xE007F) {
                return true
            }

            if (!Character.isLetter(cp)) {
                if (hasLatin && hasOtherScript) return true
                hasLatin = false
                hasOtherScript = false
                continue
            }
            val block = Character.UnicodeBlock.of(cp)
            if (block == Character.UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS ||
                block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            ) return true

            when (Character.UnicodeScript.of(cp)) {
                Character.UnicodeScript.LATIN -> hasLatin = true
                Character.UnicodeScript.COMMON, Character.UnicodeScript.INHERITED -> Unit
                else -> hasOtherScript = true
            }
        }
        return hasLatin && hasOtherScript
    }
}

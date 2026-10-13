package com.tcc.guardiaosms.tips

import com.tcc.guardiaosms.tips.AttentionPoint.DATA_REQUEST
import com.tcc.guardiaosms.tips.AttentionPoint.DISGUISED_TEXT
import com.tcc.guardiaosms.tips.AttentionPoint.LINK
import com.tcc.guardiaosms.tips.AttentionPoint.MONEY_REQUEST
import com.tcc.guardiaosms.tips.AttentionPoint.PHONE_CONTACT
import com.tcc.guardiaosms.tips.AttentionPoint.URGENCY_OR_THREAT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttentionPointsDetectorTest {

    private fun detect(text: String) = AttentionPointsDetector.detect(text)

    private fun assertHas(text: String, point: AttentionPoint) =
        assertTrue("esperava $point em \"$text\", veio ${detect(text)}", point in detect(text))

    private fun assertHasNot(text: String, point: AttentionPoint) =
        assertFalse("não esperava $point em \"$text\", veio ${detect(text)}", point in detect(text))

    // --- Mensagens legítimas que NÃO podem gerar nenhuma dica ---

    @Test
    fun mensagensLegitimasNaoGeramDicas() {
        listOf(
            "Oi mãe, chego hoje à noite",
            "Seu pedido foi enviado, código de rastreio BR123456789",
            "Sua fatura chegou, o boleto vence dia 10",
            "Oi mae, cheguei bem em casa. Te ligo mais tarde!",
            "Seu pedido foi enviado e chega ate amanha. Obrigado pela preferencia!",
            "Bom dia! A reunião de hoje foi remarcada para as 15h.",
            "Família 👨‍👩‍👧 reunida, coração ❤️",
            "Seu CEP 01234-567 foi atualizado em 13/10/2026 às 16:37"
        ).forEach { texto ->
            assertEquals("\"$texto\" não deveria gerar dicas", emptySet<AttentionPoint>(), detect(texto))
        }
    }

    // --- Exemplos usados no app ---

    @Test
    fun exemplosDeGolpeDaTelaTestar() {
        assertEquals(
            setOf(LINK, DATA_REQUEST, URGENCY_OR_THREAT),
            detect("URGENTE: sua conta sera bloqueada hoje. Regularize seu CPF agora: bit.ly/regularizar")
        )
        assertEquals(
            setOf(LINK, MONEY_REQUEST, DATA_REQUEST),
            detect("Detectamos um Pix nao autorizado. Confirme seus dados para cancelar: gg.gg/pix-cancelar")
        )
        assertEquals(setOf(MONEY_REQUEST), detect("Bom dia, filho! Me empresta R$10??"))
    }

    @Test
    fun ordemDeSaidaFixa() {
        val pontos = detect("Ligue 0800 123 4567 URGENTE, pague o Pix: bit.ly/x e envie o código")
        assertEquals(listOf(LINK, MONEY_REQUEST, DATA_REQUEST, URGENCY_OR_THREAT, PHONE_CONTACT), pontos.toList())
    }

    // --- LINK ---

    @Test
    fun link() {
        assertHas("Acesse https://banco-seguro.net/login", LINK)
        assertHas("Veja em www.promocao.com.br", LINK)
        assertHas("clique: bit.ly/abc", LINK)
        assertHas("tinyurl.com/xyz", LINK)
        assertHas("cutt.ly/a1", LINK)
        assertHas("encurtador.com.br/abc", LINK)
        assertHas("shorturl.at/abc", LINK)
        assertHas("abre.ai/pix", LINK)
        assertHas("is.gd/abc", LINK)
        assertHas("acesse bradesco-acesso.com/token", LINK)
        assertHasNot("Valor de R$ 1.234,56 confirmado", LINK)
        assertHasNot("Sr. Silva, tudo bem?", LINK)
    }

    // --- DINHEIRO ---

    @Test
    fun pedidoDeDinheiro() {
        assertHas("Faz um Pix pra mim?", MONEY_REQUEST)
        assertHas("Preciso de uma transferência ainda hoje", MONEY_REQUEST)
        assertHas("me transfere 300 reais", MONEY_REQUEST)
        assertHas("Pode me emprestar 200?", MONEY_REQUEST)
        assertHas("Faça o depósito na conta", MONEY_REQUEST)
        assertHas("Valor: R$ 89,90", MONEY_REQUEST)
    }

    @Test
    fun boletoSoContaComUrgenciaOuLink() {
        assertHasNot("Sua fatura chegou, o boleto vence dia 10", MONEY_REQUEST)
        assertHas("Seu boleto vence hoje, pague para evitar bloqueio", MONEY_REQUEST)
        assertHas("Segunda via do boleto: boletos-online.com/2via", MONEY_REQUEST)
    }

    // --- DADOS ---

    @Test
    fun pedidoDeDados() {
        assertHas("Informe sua senha para continuar", DATA_REQUEST)
        assertHas("Atualize seu CPF", DATA_REQUEST)
        assertHas("Confirme os dados do cartão", DATA_REQUEST)
        assertHas("Digite o token do app", DATA_REQUEST)
        assertHas("Confirme seus dados", DATA_REQUEST)
        assertHas("Me passa o código que chegou aí", DATA_REQUEST)
        assertHas("Envie o codigo recebido por SMS", DATA_REQUEST)
        assertHas("Código de verificação do WhatsApp: 592-104", DATA_REQUEST)
        assertHas("Seu código de segurança é 1234", DATA_REQUEST)
    }

    @Test
    fun codigoDeRastreioNaoEPedidoDeDados() {
        assertHasNot("Seu pedido foi enviado, código de rastreio BR123456789", DATA_REQUEST)
        assertHasNot("Código do produto: 4521", DATA_REQUEST)
    }

    // --- URGÊNCIA ---

    @Test
    fun urgenciaOuAmeaca() {
        assertHas("URGENTE: regularize", URGENCY_OR_THREAT)
        assertHas("Sua conta foi bloqueada", URGENCY_OR_THREAT)
        assertHas("Evite o bloqueio do cartão", URGENCY_OR_THREAT)
        assertHas("Último dia para resgatar seus pontos", URGENCY_OR_THREAT)
        assertHas("Evite o cancelamento do plano", URGENCY_OR_THREAT)
        assertHas("Responda imediatamente", URGENCY_OR_THREAT)
        assertHas("Sua linha será suspensa", URGENCY_OR_THREAT)
        assertHas("Seus pontos expiram amanhã", URGENCY_OR_THREAT)
        assertHas("A oferta expira hoje", URGENCY_OR_THREAT)
        assertHas("Pague até hoje", URGENCY_OR_THREAT)
        assertHas("Resolva ainda hoje", URGENCY_OR_THREAT)
        assertHas("Somente hoje: 50% de desconto", URGENCY_OR_THREAT)
    }

    @Test
    fun hojeSozinhoNaoEUrgencia() {
        assertHasNot("Oi mãe, chego hoje à noite", URGENCY_OR_THREAT)
        assertHasNot("Hoje tem jogo do Brasil", URGENCY_OR_THREAT)
    }

    // --- TELEFONE ---

    @Test
    fun telefoneParaContato() {
        assertHas("Ligue para (11) 91234-5678", PHONE_CONTACT)
        assertHas("Chame no zap 11912345678", PHONE_CONTACT)
        assertHas("Contato: +55 11 91234-5678", PHONE_CONTACT)
        assertHas("Central: 0800 123 4567", PHONE_CONTACT)
        assertHas("Ligue 4004-0001", PHONE_CONTACT)
        assertHas("Fale no WhatsApp: wa.me/5511912345678", PHONE_CONTACT)
        assertHas("Ligue 3456-7890", PHONE_CONTACT)
    }

    @Test
    fun numerosQueNaoSaoTelefone() {
        assertHasNot("código de rastreio BR123456789", PHONE_CONTACT)
        assertHasNot("CPF 123.456.789-00", PHONE_CONTACT)
        assertHasNot("Valor R$ 1.234,56", PHONE_CONTACT)
        assertHasNot("Data 13/10/2026 16:37", PHONE_CONTACT)
        assertHasNot("Código 592-104", PHONE_CONTACT)
    }

    // --- TEXTO DISFARÇADO ---

    @Test
    fun textoDisfarcado() {
        assertHas("ΛTENÇÃO: sua conta", DISGUISED_TEXT)          // Λ grego no lugar de A
        assertHas("CΛIXΛ informa", DISGUISED_TEXT)
        assertHas("Bаnco do Brasil", DISGUISED_TEXT)              // "а" cirílico
        assertHas("Seu Pi⁨x foi bloqueado", DISGUISED_TEXT)  // U+2068 invisível
        assertHas("con‌firme agora", DISGUISED_TEXT)         // U+200C invisível
        assertHas("ban​co", DISGUISED_TEXT)                  // zero-width space
        assertHas("pa­gamento", DISGUISED_TEXT)              // soft hyphen
        assertHas("𝐁𝐚𝐧𝐜𝐨 informa", DISGUISED_TEXT)               // letras matemáticas
        assertHas("ＢＡＮＣＯ", DISGUISED_TEXT)                       // letras fullwidth
    }

    @Test
    fun textoNormalOuEmojiNaoEDisfarcado() {
        assertHasNot("Atenção: reunião às 10h", DISGUISED_TEXT)
        assertHasNot("Família 👨‍👩‍👧 reunida", DISGUISED_TEXT)
        assertHasNot("Coração ❤️", DISGUISED_TEXT)
        assertHasNot("Seu pacote chegou 📦", DISGUISED_TEXT)
        assertHasNot("Καλημέρα", DISGUISED_TEXT)                 // grego puro, sem mistura
        assertHasNot("Valor: π × r²", DISGUISED_TEXT)
    }
}

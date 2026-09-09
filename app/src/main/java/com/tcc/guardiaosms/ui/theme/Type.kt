package com.tcc.guardiaosms.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tamanhos de fonte ligeiramente maiores que o padrão do Material 3.
 *
 * Decisão de acessibilidade: o público-alvo descrito no trabalho (idosos,
 * baixo letramento digital) se beneficia de texto maior e mais espaçado.
 * Isso é mais fácil de justificar academicamente do que "porque ficou
 * bonito" — está diretamente ligado ao usuário final definido pela banca.
 */
val GuardiaoSmsTypography = Typography(
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 20.sp)
)

package br.com.gorio.bqcodereader

import br.com.gorio.mlbarcodescanner.Segmento.getSegmento
import java.text.SimpleDateFormat
import java.util.*

class Febraban(barra: String) {
    var tipo = 0
    var linhaDigitavel: String? = null
    var codigoBanco: String? = null
    var nomeBanco: String? = null
    var segmento: String? = null
    var moeda: String? = null
    var valor: String? = null
    var vencimento: String? = null
    private var error = false
    var errorMessage: String? = null

    init {
        calculaLinhaDigitavel(barra)
    }

    /**
     * Obtem a linha digitável a partir da leitura do código de barras
     *
     * @param barra Código de barras com 44 caracteres
     */
    private fun calculaLinhaDigitavel(barra: String) { // Elimina qualquer caracter que não seja número
        var barcode = barra
        barcode = barcode.replace(REGEX.toRegex(), "")

        // Verifica o tipo de conta e direciona para o método correto
        if (barcode.length == 44 && barcode.substring(0, 1) == "8")
            calculaArrecadacao(barcode)
        else
            calculaBoletoBancario(barcode)
    }

    /**
     * Calcula linha digitável para boletos de Arrecadação/Recebimento/Concessionária
     *
     * @param barra Código de barras com 44 caracteres
     */
    private fun calculaArrecadacao(barra: String) {
        tipo = BOLETO_ARRECADACAO
        val campo1 = barra.substring(0, 11)
        val campo2 = barra.substring(11, 22)
        val campo3 = barra.substring(22, 33)
        val campo4 = barra.substring(33, 44)
        // Configura Segmento
        segmento = getSegmento(barra.substring(1, 2))
        // Configura o valor do boleto
        valor = Integer.valueOf(barra.substring(4, 13)).toString() + "," + barra.substring(13, 15)
        // Configura a linha digitável
        linhaDigitavel = (
                campo1 + modulo10(campo1)
                + ' '
                + campo2 + modulo10(campo2)
                + ' '
                + campo3 + modulo10(campo3)
                + ' '
                + campo4 + modulo10(campo4))
    }

    /**
     * Calcula linha digitável para boletos bancários
     *
     * @param barra Código de barras com 44 caracteres
     */
    private fun calculaBoletoBancario(barra: String) {
        tipo = BOLETO_BANCARIO
        if (barra.length == 44) {
            val campo1 =
                barra.substring(0, 4) + barra.substring(19, 20) + '.' + barra.substring(20, 24)
            val campo2 = barra.substring(24, 29) + '.' + barra.substring(29, 34)
            val campo3 = barra.substring(34, 39) + '.' + barra.substring(39, 44)
            val campo4 = barra.substring(4, 5) // Digito verificador
            var campo5 = barra.substring(5, 19) // Vencimento + Valor
            if (campo5 == "0") campo5 = "000"
            // Configura código do banco
            codigoBanco = campo1.substring(0, 3)
            // Configura nome do banco
            nomeBanco = Bancos.getBanco(campo1.substring(0, 3))
            // Configura moeda
            moeda = campo1.substring(3, 4)
            // Configura vencimento
            vencimento = fator_vencimento(Integer.valueOf(campo5.substring(0, 4)))
            // Configura valor do boleto
            valor = Integer.valueOf(campo5.substring(4, 12)).toString() + "," + campo5.substring(12, 14)
            // Configura a linha digitável
            linhaDigitavel = (
                    campo1 + modulo10(campo1)
                    + ' '
                    + campo2 + modulo10(campo2)
                    + ' '
                    + campo3 + modulo10(campo3)
                    + ' '
                    + campo4
                    + ' '
                    + campo5)
        } else if (barra.length == 47) {
            validaLinhaDigitavel(barra)
        } else {
            setError(true)
            errorMessage = "Código de barras inválido"
        }
    }

    /**
     * Transforma Linha Digitável em Objeto Febraban e valida se é válida
     *
     * @param linhaDigitavel
     * @return
     */
    private fun validaLinhaDigitavel(linhaDigitavel: String) { // Configura código do banco
        codigoBanco = linhaDigitavel.substring(0, 3)
        // Configura nome do banco
        nomeBanco = Bancos.getBanco(linhaDigitavel.substring(0, 3))
        // Configura moeda
        moeda = linhaDigitavel.substring(3, 4)
        // Configura vencimento
        vencimento = fator_vencimento(Integer.valueOf(linhaDigitavel.substring(33, 37)))
        // Configura valor do boleto
        valor = Integer.valueOf(linhaDigitavel.substring(37, 47)).toString()
        // Configura a linha digitável
        this.linhaDigitavel = linhaDigitavel
    }

    fun hasError(): Boolean {
        return error
    }

    fun setError(error: Boolean) {
        this.error = error
    }

    companion object {
        const val BOLETO_ARRECADACAO = 0
        const val BOLETO_BANCARIO = 1
        private const val REGEX = "[^0-9]"
        /**
         * Cálculo do Modulo 10
         *
         * @param numero Conjunto de números para cálculo do módulo 10
         */
        private fun modulo10(numero: String): Int {
            var num = numero
            num = num.replace(REGEX.toRegex(), "")
            var soma = 0
            var peso = 2
            var contador = num.length - 1
            while (contador >= 0) {
                var multiplicacao =
                    Integer.valueOf(num.substring(contador, contador + 1)) * peso
                if (multiplicacao >= 10) {
                    multiplicacao = 1 + (multiplicacao - 10)
                }
                soma += multiplicacao
                peso = if (peso == 2) {
                    1
                } else {
                    2
                }
                contador -= 1
            }
            var digito = 10 - soma % 10
            if (digito == 10) digito = 0
            return digito
        }

        private fun fator_vencimento(days: Int): String {
            val default_time =
                876231261000L // Fator contado a partir da data base 07/10/1997
            val dtStartDate = Date(default_time)
            val sdf =
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val c = Calendar.getInstance()
            c.time = dtStartDate
            c.add(Calendar.DATE, days)
            return sdf.format(c.time)
        }
    }
}
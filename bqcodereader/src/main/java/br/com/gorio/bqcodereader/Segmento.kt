package br.com.gorio.bqcodereader

import java.util.*

object Segmento {
    private var listaSegmentos: HashMap<String, String>? = null
    @JvmStatic
    fun getSegmento(codigo: String?): String? {
        if (listaSegmentos == null) fillHashSegmentos()
        return listaSegmentos!![codigo]
    }

    private fun fillHashSegmentos() {
        listaSegmentos = HashMap()
        listaSegmentos!!["1"] = "Prefeituras"
        listaSegmentos!!["2"] = "Saneamento"
        listaSegmentos!!["3"] = "Energia Elétrica e Gás"
        listaSegmentos!!["4"] = "Telecomunicações"
        listaSegmentos!!["5"] = "Órgãos Governamentais"
        listaSegmentos!!["6"] = "Carnes e Assemelhados ou demais Empresas / Órgãos que serão identificadas através do CNPJ."
        listaSegmentos!!["7"] = "Multas de trânsito"
        listaSegmentos!!["9"] = "Uso interno do banco"
    }
}
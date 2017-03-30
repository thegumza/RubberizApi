package com.thegumza

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import model.LocalModel
import model.LocalRubber
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.util.*

object LocalPriceTask : TimerTask() {

    var months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฏาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

    override fun run() {
        val client = OkHttpClient()

        val request = Request.Builder()
                .url("http://www.rubberthai.com/yang/HisLoc.php")
                .get()
                .addHeader("cache-control", "no-cache")
                .build()

        val response = client.newCall(request).execute()
        val doc: Document? = Jsoup.parse(response.body().string())
        val elements: Elements? = doc?.getElementsByTag("tbody")?.first()?.getElementsByTag("tr")
        val rubberModel: LocalModel? = LocalModel()
        var month: String? = null
        val year = doc?.select(".year")?.text()?.toInt()?.minus(543)
        val songkhlaList: MutableList<LocalRubber>? = ArrayList()
        val suratList: MutableList<LocalRubber>? = ArrayList()
        val nakornList: MutableList<LocalRubber>? = ArrayList()
        val yalaList: MutableList<LocalRubber>? = ArrayList()
        for (x in elements?.indices!!) {
            if (x > 3) {
                if (months.contains(elements[x].allElements[1].text())) {
                    month = elements[x].allElements[1].text()
                }
                if (!months.contains(elements[x].allElements[1].text())) {
                    val songkla: LocalRubber? = LocalRubber()
                    songkla?.localPrice = elements[x].getElementsByClass("num2")[0].text().replace(",", "")
                    songkla?.latexPrice = elements[x].getElementsByClass("num2")[2].text().replace(",", "")
                    songkla?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val surat: LocalRubber? = LocalRubber()
                    surat?.localPrice = elements[x].getElementsByClass("num2")[3].text().replace(",", "")
                    surat?.latexPrice = elements[x].getElementsByClass("num2")[4].text().replace(",", "")
                    surat?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val nakorn: LocalRubber? = LocalRubber()
                    nakorn?.localPrice = elements[x].getElementsByClass("num2")[5].text().replace(",", "")
                    nakorn?.latexPrice = elements[x].getElementsByClass("num2")[6].text().replace(",", "")
                    nakorn?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val yala: LocalRubber? = LocalRubber()
                    yala?.localPrice = elements[x].getElementsByClass("num2")[7].text().replace(",", "")
                    yala?.latexPrice = elements[x].getElementsByClass("num2")[8].text().replace(",", "")
                    yala?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())

                    songkhlaList?.add(songkla as LocalRubber)
                    suratList?.add(surat as LocalRubber)
                    nakornList?.add(nakorn as LocalRubber)
                    yalaList?.add(yala as LocalRubber)
                }
            }
        }
        rubberModel?.songkhlaData = songkhlaList?.filter { it.localPrice?.toDouble() != 0.0 ; it.latexPrice?.toDouble() != 0.0  } as MutableList<LocalRubber>
        rubberModel?.suratData = suratList?.filter { it.localPrice?.toDouble() != 0.0 ; it.latexPrice?.toDouble() != 0.0} as MutableList<LocalRubber>
        rubberModel?.nakornData = nakornList?.filter { it.localPrice?.toDouble() != 0.0 ; it.latexPrice?.toDouble() != 0.0} as MutableList<LocalRubber>
        rubberModel?.yalaData = yalaList?.filter { it.localPrice?.toDouble() != 0.0 } as MutableList<LocalRubber>

        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        val jsonInString = mapper.writeValueAsString(rubberModel)
        File("local_price.json").writeText(jsonInString)
        print("Local Price Service Successful\n")
    }


}


package com.thegumza

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import model.Rubber
import model.RubberModel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.util.*

object RSSPriceTask : TimerTask() {
    var months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฏาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    override fun run() {
        val client = OkHttpClient()

        val request = Request.Builder()
                .url("http://www.rubberthai.com/yang/HisRSS.php")
                .get()
                .addHeader("cache-control", "no-cache")
                .build()

        val response = client.newCall(request).execute()
        val doc: Document? = Jsoup.parse(response.body().string())
        val elements: Elements? = doc?.getElementsByTag("tbody")?.first()?.getElementsByTag("tr")
        var month: String? = null
        val year = doc?.select(".year")?.text()?.toInt()?.minus(543)
        val rubberModel: RubberModel? = RubberModel()
        val songkhlaList: MutableList<Rubber>? = ArrayList()
        val suratList: MutableList<Rubber>? = ArrayList()
        val nakornList: MutableList<Rubber>? = ArrayList()
        val yalaList: MutableList<Rubber>? = ArrayList()
        for (x in elements?.indices!!) {
            if (x > 4) {
                if (months.contains(elements[x].allElements[1].text())) {
                    month = elements[x].allElements[1].text()
                }
                if (!months.contains(elements[x].allElements[1].text())) {
                    val songkla: Rubber? = Rubber()
                    songkla?.price = elements[x].getElementsByClass("num2")[0].text().replace(",", "")
                    songkla?.volume = elements[x].getElementsByClass("num2")[1].text().replace(",", "")
                    songkla?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val surat: Rubber? = Rubber()
                    surat?.price = elements[x].getElementsByClass("num2")[2].text().replace(",", "")
                    surat?.volume = elements[x].getElementsByClass("num2")[3].text().replace(",", "")
                    surat?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val nakorn: Rubber? = Rubber()
                    nakorn?.price = elements[x].getElementsByClass("num2")[4].text().replace(",", "")
                    nakorn?.volume = elements[x].getElementsByClass("num2")[5].text().replace(",", "")
                    nakorn?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())
                    val yala: Rubber? = Rubber()
                    yala?.price = elements[x].getElementsByClass("num2")[6].text().replace(",", "")
                    yala?.volume = elements[x].getElementsByClass("num2")[7].text().replace(",", "")
                    yala?.date = String.format("%s.%02d.%02d", year.toString(), (months.indexOf(month) + 1), elements[x].allElements.select(".day").text().toInt())

                    songkhlaList?.add(songkla as Rubber)
                    suratList?.add(surat as Rubber)
                    nakornList?.add(nakorn as Rubber)
                    yalaList?.add(yala as Rubber)
                }
            }
        }
        rubberModel?.songkhlaData = songkhlaList?.filter { it.price?.toDouble() != 0.0 } as MutableList<Rubber>
        rubberModel?.suratData = suratList?.filter { it.price?.toDouble() != 0.0 } as MutableList<Rubber>
        rubberModel?.nakornData = nakornList?.filter { it.price?.toDouble() != 0.0 } as MutableList<Rubber>
        rubberModel?.yalaData = yalaList?.filter { it.price?.toDouble() != 0.0 } as MutableList<Rubber>

        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        val jsonInString = mapper.writeValueAsString(rubberModel)
        File("rss_price.json").writeText(jsonInString)
        print("RSS Price Service Successful\n")
    }

}


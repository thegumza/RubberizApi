package com.thegumza
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.util.*

class RubberApiService {

    companion object RunTask {

        @JvmStatic fun main(args: Array<String>) {

            val newsTask = NewsTask
            val ussTask = USSPriceTask
            val rssTask = RSSPriceTask
            val localTask = LocalPriceTask

            newsTask.run()
            ussTask.run()
            rssTask.run()
            localTask.run()

        }

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

                            songkhlaList?.add(songkla as LocalRubber)
                            suratList?.add(surat as LocalRubber)
                            nakornList?.add(nakorn as LocalRubber)
                        }
                    }
                }
                rubberModel?.songkhlaData = songkhlaList
                rubberModel?.suratData = suratList
                rubberModel?.nakornData = nakornList

                val mapper = ObjectMapper()
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
                val jsonInString = mapper.writeValueAsString(rubberModel)
                File("local_price.json").writeText(jsonInString)
                print("Local Price Service Successful\n")
            }


        }

        object NewsTask : TimerTask() {
            override fun run() {
                val client = OkHttpClient()

                val request = Request.Builder()
                        .url("http://www.rubberthai.com/index.php/newsyang/rubber-prices-and-economic-factors-involved4?start=0")
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build()

                val response = client.newCall(request).execute()
                val doc: Document? = Jsoup.parse(response.body().string())
                val news: Elements = doc?.getElementsByClass("catItemBody") as Elements


                val request2 = Request.Builder()
                        .url("http://www.rubberthai.com/index.php/newsyang/rubber-prices-and-economic-factors-involved4?start=15")
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build()

                val response2 = client.newCall(request2).execute()
                val doc2: Document? = Jsoup.parse(response2.body().string())
                val news2: Elements = doc2?.getElementsByClass("catItemBody") as Elements

                val newsList: MutableList<News> = ArrayList()
                parseNews(news, newsList)
                parseNews(news2, newsList)

                val mapper = ObjectMapper()
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
                val jsonInString = mapper.writeValueAsString(newsList)
                File("rubber_news.json").writeText(jsonInString)
                print("News Service Successful\n")
            }

            private fun parseNews(news: Elements, newsList: MutableList<News>) {
                for (i in news) {
                    val model: News? = News()
                    model?.title = i.getElementsByClass("catItemTitle").text().substringBefore("(").replace("@", "")
                    model?.url = i.getElementsByTag("a").attr("href")
                    model?.content = i.getElementsByClass("catItemIntroText").text()
                    model?.date = i.getElementsByClass("catItemDateCreated").text()
                    model?.total_view = i.getElementsByClass("catItemHits").text().replace("times", "views")
                    newsList.add(model as News)
                }
            }


        }

        object USSPriceTask : TimerTask() {

            var months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฏาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")

            override fun run() {
                val client = OkHttpClient()

                val request = Request.Builder()
                        .url("http://www.rubberthai.com/yang/HisUSS.php")
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build()

                val response = client.newCall(request).execute()
                val doc: Document? = Jsoup.parse(response.body().string())
                val elements: Elements? = doc?.getElementsByTag("tbody")?.first()?.getElementsByTag("tr")
                //val dailyPrice = elements?.filter { it.getElementsByClass("month").first() != null || it.getElementsByClass("day").first() != null }
                val year = doc?.select(".year")?.text()?.toInt()?.minus(543)
                var month: String? = null
                val rubberModel: RubberModel? = RubberModel()
                val songkhlaList: MutableList<Rubber>? = ArrayList()
                val suratList: MutableList<Rubber>? = ArrayList()
                val nakornList: MutableList<Rubber>? = ArrayList()
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

                            songkhlaList?.add(songkla as Rubber)
                            suratList?.add(surat as Rubber)
                            nakornList?.add(nakorn as Rubber)
                        }
                    }
                }
                rubberModel?.songkhlaData = songkhlaList
                rubberModel?.suratData = suratList
                rubberModel?.nakornData = nakornList

                val mapper = ObjectMapper()
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
                val jsonInString = mapper.writeValueAsString(rubberModel)
                File("uss_price.json").writeText(jsonInString)
                print("USS Price Service Successful\n")
            }


        }

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

                            songkhlaList?.add(songkla as Rubber)
                            suratList?.add(surat as Rubber)
                            nakornList?.add(nakorn as Rubber)
                        }
                    }
                }
                rubberModel?.songkhlaData = songkhlaList
                rubberModel?.suratData = suratList
                rubberModel?.nakornData = nakornList

                val mapper = ObjectMapper()
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
                val jsonInString = mapper.writeValueAsString(rubberModel)
                File("rss_price.json").writeText(jsonInString)
                print("RSS Price Service Successful\n")
            }

        }

    }
}
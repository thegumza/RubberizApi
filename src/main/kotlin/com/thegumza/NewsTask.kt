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


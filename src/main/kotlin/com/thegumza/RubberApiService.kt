package com.thegumza

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


    }
}
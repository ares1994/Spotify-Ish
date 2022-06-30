package com.arepade.spotifyish.utils

import androidx.test.espresso.idling.CountingIdlingResource
import com.apollographql.apollo3.android.ApolloIdlingResource

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"


    val apolloIdlingResource = ApolloIdlingResource("apolloIdlingResource")


    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
package com.arepade.spotifyish.di

import android.app.Application
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.android.idlingResource
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.database.getDatabase
import com.arepade.spotifyish.repository.Repository
import com.arepade.spotifyish.repository.SpotifyIshRepository
import com.arepade.spotifyish.utils.EspressoIdlingResource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class)
object AppModule {

    @Provides
    fun provideAnalyticsService(
    ): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://graphbrainz.herokuapp.com/graphql")
            .idlingResource(EspressoIdlingResource.apolloIdlingResource)
            .build()
    }


    @Provides
    fun provideRoomDatabase(application: Application): ArtistDatabase {
        return getDatabase(application.applicationContext)
    }


    @Provides
    fun provideSpotifyishRepository(client: ApolloClient, database: ArtistDatabase): Repository {
        return SpotifyIshRepository(client, database)
    }
}
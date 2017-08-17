package com.yuschukivan.remindme.di.modules

import android.content.Context
import com.yuschukivan.remindme.common.utils.RealmConfig
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

/**
 * Created by Ivan on 6/6/2017.
 */
@Module
class RealmModule (private val context: Context) {

    @Provides
    @Singleton
    fun provideRealmInstance(): Realm {
        Realm.init(context)
        return Realm.getInstance(RealmConfig.realmConfig)
    }
}
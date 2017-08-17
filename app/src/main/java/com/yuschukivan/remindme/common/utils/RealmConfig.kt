package com.yuschukivan.remindme.common.utils

import io.realm.RealmConfiguration

/**
 * Created by Ivan on 6/6/2017.
 */
object RealmConfig {
    val realmConfig by lazy {
        RealmConfiguration.Builder()
                .name("New_Realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build()
    }
}
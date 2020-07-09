package com.hun.motorcontroller

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .build()
        )
    }
}
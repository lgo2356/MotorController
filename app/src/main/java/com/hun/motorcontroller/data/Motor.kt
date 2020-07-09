package com.hun.motorcontroller.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Motor : RealmObject() {
    @PrimaryKey
    var id: Long = -1
    var name: String = ""
}

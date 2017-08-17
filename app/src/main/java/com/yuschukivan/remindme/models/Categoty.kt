package com.yuschukivan.remindme.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Ivan on 6/6/2017.
 */
open class Categoty(
        @PrimaryKey
        var id:Long,
        var title: String
): RealmObject(){
    constructor(): this(0,"")

    override fun toString() = title

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.java != other::class.java) return false

        return (id == (other as Categoty).id)
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}
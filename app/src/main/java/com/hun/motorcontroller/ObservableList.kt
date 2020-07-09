package com.hun.motorcontroller

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ObservableList<T> {

    private val list: ArrayList<T> = ArrayList()
    private var onAdd: PublishSubject<T> = PublishSubject.create()

    fun add(value: T) {
        list.add(value)
        onAdd.onNext(value)
    }

    fun getObservable(): Observable<T> {
        return onAdd
    }

    fun size(): Int {
        return list.size
    }
}
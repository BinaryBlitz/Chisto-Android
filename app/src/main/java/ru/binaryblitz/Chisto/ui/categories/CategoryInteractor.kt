package ru.binaryblitz.Chisto.ui.categories

import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Category


interface CategoryInteractor{
    fun getCategories() : Observable<List<Category>>
}
package ru.binaryblitz.Chisto.ui.categories

import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem

interface CategoryInteractor{
    fun getCategories() : Observable<List<Category>>
    fun getCategoriesItems(id: Int) : Observable<List<CategoryItem>>
    fun getAllItems() : Observable<List<CategoryItem>>
}

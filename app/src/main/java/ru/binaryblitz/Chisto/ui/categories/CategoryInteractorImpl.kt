package ru.binaryblitz.Chisto.ui.categories
import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.network.ApiEndpoints


class CategoryInteractorImpl(val api: ApiEndpoints) : CategoryInteractor {
    override fun getAllItems(): Observable<List<CategoryItem>> {
        return api.allItems
    }

    override fun getCategoriesItems(id: Int): Observable<List<CategoryItem>> {
        return api.getItems(id)
    }

    override fun getCategories(): Observable<List<Category>> {
        return api.categories
    }
}
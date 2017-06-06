package ru.binaryblitz.Chisto.ui.categories
import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.network.ApiEndpoints


class CategoryInteractorImpl(val api: ApiEndpoints) : CategoryInteractor {
    override fun getCategories(): Observable<List<Category>> {
        return api.categories
    }
}
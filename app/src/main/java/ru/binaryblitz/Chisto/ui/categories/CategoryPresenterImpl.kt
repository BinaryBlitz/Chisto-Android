package ru.binaryblitz.Chisto.ui.categories

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.utils.ColorsList
import java.util.*


class CategoryPresenterImpl(val context: Context, val interactor: CategoryInteractor,
                            private var categoryView: CategoryView?) : CategoryPresenter {

    override fun setView(view: CategoryView) {
        categoryView = view
    }

    fun getCategories() {
        categoryView?.showProgress()
        interactor.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ categoryList ->
                    categoryView?.hideProgress()
                    categoryView?.showCategories(categoryList)
                }, { error ->
                    categoryView?.hideProgress()
                    categoryView?.showError(error.toString())
                })
    }

    fun getCategoriesItems(id: Int) {
        categoryView?.showProgress()
        interactor.getCategoriesItems(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ categoriesItems ->
                    categoryView?.hideProgress()
                    categoryView?.showCategoryInfo(categoriesItems)
                }, { error ->
                    categoryView?.hideProgress()
                    categoryView?.showError(error.toString())
                })
    }

    fun getAllItems() {
        categoryView?.showProgress()
        interactor.getAllItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ categoriesItems ->
                    categoryView?.hideProgress()
                    ColorsList.load(context)
                    categoryView?.showAllItems(categoriesItems)
                }, { error ->
                    categoryView?.hideProgress()
                    categoryView?.showError(error.toString())
                })
    }

    private fun sortCategories(collection: ArrayList<Category>) {
        Collections.sort(collection) { category, t ->
            if (category.featured && !t.featured) {
                -1
            } else if (!category.featured && t.featured) {
                1
            } else {
                category.name.compareTo(t.name)
            }
        }
    }

    private fun sortCategoryItems(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }

    private fun sortAllItems(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }

}

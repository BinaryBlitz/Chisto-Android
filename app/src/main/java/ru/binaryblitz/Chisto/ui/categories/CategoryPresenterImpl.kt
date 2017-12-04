package ru.binaryblitz.Chisto.ui.categories

import android.content.Context
import io.reactivex.functions.BiFunction
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.ResourceManager
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.utils.ColorsList
import ru.binaryblitz.Chisto.utils.Image
import timber.log.Timber
import java.util.*


class CategoryPresenterImpl(
        val context: Context,
        private val interactor: CategoryInteractor,
        private var view: CategoryView?,
        private val resourceManager: ResourceManager
) : CategoryPresenter {

    private lateinit var allItems: List<CategoryItem>

    override fun setView(view: CategoryView) {
        this.view = view
    }

    fun getCategories() {
        interactor.getCategories().zipWith(
                interactor.getAllItems(),
                BiFunction<List<Category>, List<CategoryItem>, List<Category>> { categories, items ->
                    allItems = items
                    addCategoryWithAllItems(categories, items)
                })
                .doOnSubscribe { view?.showProgress() }
                .doOnTerminate { view?.hideProgress() }
                .subscribe(
                        { categories ->
                            Timber.d(categories.toString())
                            view?.showCategories(categories)
                        },
                        { error ->
                            Timber.d(error.toString())
                            view?.showError(error.toString())
                        }
                )
    }

    fun getCategoriesItems(id: Int) {
        Timber.d(id.toString())
        if (id == All_CATEGORY_ID) {
            view?.showCategoryInfo(allItems)
        } else {
            interactor.getCategoriesItems(id)
                    .doOnSubscribe { view?.showProgress() }
                    .doOnTerminate { view?.hideProgress() }
                    .subscribe(
                            { categoriesItems -> view?.showCategoryInfo(categoriesItems) },
                            { error ->
                                view?.showError(error.toString())
                                Timber.d(error.toString())
                            }
                    )
        }
    }

    fun getAllItems() {
        interactor.getAllItems()
                .doOnSubscribe { view?.showProgress() }
                .doOnTerminate { view?.hideProgress() }
                .subscribe(
                        { categoriesItems ->
                            ColorsList.load(context)
                            view?.showAllItems(categoriesItems)
                        },
                        { error ->
                            Timber.d(error.toString())
                            view?.showError(error.toString())
                        }
                )
    }

    private fun addCategoryWithAllItems(
            categories: List<Category>,
            items: List<CategoryItem>
    ): List<Category> {
        val categoryWithAllItems = Category(
                id = -1,
                name = resourceManager.getString(R.string.all),
                description = "",
                iconUrl = Image.resIdToUri(context, R.drawable.icon_allcategory_action).toString(),
                color = ALL_CATEGORY_COLOR,
                featured = true,
                itemsCount = items.size,
                itemsPreview = items.map { it.name }.toList()
        )
        (categories as MutableList<Category>).add(0, categoryWithAllItems)
        return categories
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

    private companion object {
        private const val All_CATEGORY_ID = -1
        private const val ALL_CATEGORY_COLOR = "#42e295"
    }
}

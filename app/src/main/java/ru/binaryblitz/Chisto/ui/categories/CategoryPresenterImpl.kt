package ru.binaryblitz.Chisto.ui.categories

import android.content.Context
import io.reactivex.functions.BiFunction
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.ResourceManager
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.entities.CategoryItem.Companion.DEFAULT_CATEGORY_COLOR
import ru.binaryblitz.Chisto.utils.ColorsList
import ru.binaryblitz.Chisto.utils.Image
import timber.log.Timber


class CategoryPresenterImpl(
        val context: Context,
        private val interactor: CategoryInteractor,
        private var view: CategoryView?,
        private val resourceManager: ResourceManager
) : CategoryPresenter {

    private lateinit var allItems: List<CategoryItem>
    private lateinit var categories: List<Category>

    override fun setView(view: CategoryView) {
        this.view = view
    }

    fun getCategories() {
        interactor.getCategories().zipWith(
                interactor.getAllItems(),
                BiFunction<List<Category>, List<CategoryItem>, List<Category>> { categories, items ->
                    allItems = items
                    this.categories = categories
                    setAllItemsCategoryColor(categories, items)
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
        if (id == All_CATEGORY_ID) {
            view?.showCategoryInfo(allItems)
        } else {
            interactor.getCategoriesItems(id)
                    .doOnNext { setItemsCategoryColor(it, id) }
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

    private fun setItemsCategoryColor(items: List<CategoryItem>, id: Int) {
        items.forEach { item ->
            item.categoryColor = categories.find { it.id == id }?.color ?: DEFAULT_CATEGORY_COLOR
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

    private fun setAllItemsCategoryColor(
            categories: List<Category>,
            items: List<CategoryItem>
    ) {
        items.forEach { item ->
            item.categoryColor = categories.find { it.id == item.categoryId }?.color ?: DEFAULT_CATEGORY_COLOR
        }
    }

    private companion object {
        private const val All_CATEGORY_ID = -1
        private const val ALL_CATEGORY_COLOR = "#42e295"
    }
}

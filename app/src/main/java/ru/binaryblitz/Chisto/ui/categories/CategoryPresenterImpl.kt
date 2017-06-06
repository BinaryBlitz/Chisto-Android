package ru.binaryblitz.Chisto.ui.categories

import android.content.Context
import android.support.v4.util.Pair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.utils.ColorsList
import java.util.*


class CategoryPresenterImpl(val context: Context,  val interactor: CategoryInteractor, private var categoryView: CategoryView?) : CategoryPresenter {

    override fun setView(view: CategoryView) {
        categoryView = view
    }

    private fun getCategories() {
        interactor.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { categoryList ->
                            categoryView?.showCategories(categoryList)
                            save(categoryList)

                        },
                        { error -> categoryView?.showError(error.toString()) }
                )
    }


    private fun save(collection: List<Category>) {
        for (i in collection.indices) {
            val (id, icon, name, description, color) = collection[i]
            ColorsList.add(Pair(id, color.toInt()))
        }

        ColorsList.saveColors(context)
    }

//    categoryList(0, Category(0,
//    "https://chisto-staging.s3.amazonaws.com/uploads/category/icon/2/2b192e163d524db18d3b0d3abfdd3a2d.png",
//    context.getString(R.string.all_categories),
//    SpannableString("empty").toString(), R.color.greyColor, false))

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


}
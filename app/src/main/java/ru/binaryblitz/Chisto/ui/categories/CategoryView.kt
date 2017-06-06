package ru.binaryblitz.Chisto.ui.categories

import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.ui.base.BaseLCEView

interface CategoryView : BaseLCEView {
    fun showCategories(categories: List<Category>)
    fun showCategoryInfo(categoryItems: List<CategoryItem>)
}
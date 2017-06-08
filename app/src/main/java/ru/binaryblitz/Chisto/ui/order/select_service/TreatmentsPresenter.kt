package ru.binaryblitz.Chisto.ui.order.select_service

import ru.binaryblitz.Chisto.entities.Order

interface TreatmentsPresenter {
    fun setView(view: TreatmentsView)
    fun setCurrentOrder(order: Order)
}

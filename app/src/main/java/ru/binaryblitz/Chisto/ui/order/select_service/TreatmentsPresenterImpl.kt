package ru.binaryblitz.Chisto.ui.order.select_service

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.utils.OrderList

class TreatmentsPresenterImpl(val context: Context, val interactor: TreatmentsInteractor,
                              private var treatmentsView: TreatmentsView) : TreatmentsPresenter {

    private lateinit var order: Order

    override fun setView(view: TreatmentsView) {
        treatmentsView = view
    }

    fun getTreatments(id: Int) {
        treatmentsView.showProgress()
        interactor.getTreatments(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ treatments: List<Treatment> ->
                    treatmentsView.hideProgress()
                    treatmentsView.showTreatments(treatments)
                }, { error ->
                    treatmentsView.showError(error.toString())
                })
    }

    override fun setCurrentOrder(currentOrder: Order) {
        order = currentOrder
    }

    fun increaseOrderAmount(){
        order.count++
        treatmentsView.updateOrderAmount(order.count)
    }

    fun decreaseOrderAmount(){
        if (order.count <= 1) return
        order.count--
        treatmentsView.updateOrderAmount(order.count)
    }

    fun proceedOrder(){
        OrderList.add(order)
    }
}

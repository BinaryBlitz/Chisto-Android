package ru.binaryblitz.Chisto.ui.order.select_service

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.binaryblitz.Chisto.entities.Treatment

class TreatmentsPresenterImpl(val context: Context, val interactor: TreatmentsInteractor,
                              private var treatmentsView: TreatmentsView) : TreatmentsPresenter {
    val format = "#.#"

    val squareCentimetersInSquareMeters = 10000.0

    private var width: Int = 0
    private var length: Int = 0

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
}

package ru.binaryblitz.Chisto.ui.order.select_service

import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.network.ApiEndpoints

class TreatmentsInteractorImpl(val api: ApiEndpoints) : TreatmentsInteractor {
    override fun getTreatments(id: Int): Observable<List<Treatment>> {
        return api.getTreatments(id)
    }
}

package ru.binaryblitz.Chisto.ui.order.select_service

import io.reactivex.Observable
import ru.binaryblitz.Chisto.entities.Treatment

interface TreatmentsInteractor {
    fun getTreatments(id: Int): Observable<List<Treatment>>
}

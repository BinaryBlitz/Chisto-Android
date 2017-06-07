package ru.binaryblitz.Chisto.ui.order.select_service

import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.ui.base.BaseLCEView

interface TreatmentsView : BaseLCEView {
    fun showTreatments(treatments: List<Treatment>)
}

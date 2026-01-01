package dev.minios.pdaiv1.domain.usecase.report

import dev.minios.pdaiv1.domain.entity.ReportReason
import dev.minios.pdaiv1.domain.repository.ReportRepository

class SendReportUseCaseImpl(
    private val repository: ReportRepository,
) : SendReportUseCase {

    override fun invoke(text: String, reason: ReportReason, image: String) =
        repository.send(text, reason, image)

}

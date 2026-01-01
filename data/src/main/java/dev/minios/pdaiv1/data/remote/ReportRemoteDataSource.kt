package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.domain.datasource.ReportDataSource
import dev.minios.pdaiv1.domain.entity.ReportReason
import dev.minios.pdaiv1.network.api.pdai.ReportApi
import dev.minios.pdaiv1.network.request.ReportRequest
import io.reactivex.rxjava3.core.Completable

internal class ReportRemoteDataSource(private val api: ReportApi) : ReportDataSource.Remote {

    override fun send(
        text: String,
        reason: ReportReason,
        image: String,
        source: String,
        model: String
    ): Completable {
        val payload = ReportRequest(text, reason.toString(), image, source, model)
        return api.postReport(payload)
    }
}

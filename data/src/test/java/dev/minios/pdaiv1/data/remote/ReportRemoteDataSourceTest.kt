package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.domain.entity.ReportReason
import dev.minios.pdaiv1.network.api.pdai.ReportApi
import dev.minios.pdaiv1.network.request.ReportRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class ReportRemoteDataSourceTest {

    private val stubException = Throwable("Network error")
    private val stubApi = mockk<ReportApi>()

    private val remoteDataSource = ReportRemoteDataSource(stubApi)

    @Test
    fun `given attempt to send report, api returns success, expected complete value`() {
        val text = "Bug report text"
        val reason = ReportReason.Other
        val image = "base64image"
        val source = "HUGGING_FACE"
        val model = "stabilityai/sdxl-turbo"

        every {
            stubApi.postReport(any())
        } returns Completable.complete()

        remoteDataSource
            .send(text, reason, image, source, model)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify {
            stubApi.postReport(ReportRequest(text, reason.toString(), image, source, model))
        }
    }

    @Test
    fun `given attempt to send report, api returns error, expected error value`() {
        val text = "Feature request"
        val reason = ReportReason.InappropriateContent
        val image = ""
        val source = "LOCAL_MICROSOFT_ONNX"
        val model = "onnx-model"

        every {
            stubApi.postReport(any())
        } returns Completable.error(stubException)

        remoteDataSource
            .send(text, reason, image, source, model)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to send report with empty image, api receives correct payload`() {
        val text = "Some report"
        val reason = ReportReason.Other
        val image = ""
        val source = "AUTOMATIC1111"
        val model = ""

        every {
            stubApi.postReport(any())
        } returns Completable.complete()

        remoteDataSource
            .send(text, reason, image, source, model)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify {
            stubApi.postReport(ReportRequest(text, reason.toString(), image, source, model))
        }
    }
}

package dev.minios.pdaiv1.domain.usecase.report

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.entity.ReportReason
import dev.minios.pdaiv1.domain.repository.ReportRepository
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class SendReportUseCaseImplTest {

    private val stubRepository = mock<ReportRepository>()

    private val useCase = SendReportUseCaseImpl(stubRepository)

    @Test
    fun `given repository send successful, expected complete`() {
        val text = "Test report text"
        val reason = ReportReason.Other
        val image = "base64image"

        whenever(stubRepository.send(text, reason, image))
            .thenReturn(Completable.complete())

        useCase(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository send failed, expected error`() {
        val text = "Test report text"
        val reason = ReportReason.InappropriateContent
        val image = ""
        val stubException = Throwable("Network error")

        whenever(stubRepository.send(text, reason, image))
            .thenReturn(Completable.error(stubException))

        useCase(text, reason, image)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}

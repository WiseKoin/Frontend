package com.wisekoin.viewmodel

import com.wisekoin.data.model.FeedbackCategory
import com.wisekoin.data.model.FeedbackSubmission
import com.wisekoin.data.repository.FeedbackRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedbackViewModelTest {
    @Test
    fun `blank message is rejected before submission`() {
        val repository = FakeFeedbackRepository()
        val viewModel = FeedbackViewModel(repository, Dispatchers.Unconfined)

        viewModel.submit()

        assertEquals(
            FeedbackValidationError.MESSAGE_REQUIRED,
            viewModel.uiState.value.validationError
        )
        assertNull(repository.submission)
    }

    @Test
    fun `invalid optional email is rejected before submission`() {
        val repository = FakeFeedbackRepository()
        val viewModel = FeedbackViewModel(repository, Dispatchers.Unconfined)
        viewModel.updateMessage("The balance card overlaps the navigation bar.")
        viewModel.updateEmail("not-an-email")

        viewModel.submit()

        assertEquals(
            FeedbackValidationError.INVALID_EMAIL,
            viewModel.uiState.value.validationError
        )
        assertNull(repository.submission)
    }

    @Test
    fun `valid feedback is normalized and submitted`() {
        val repository = FakeFeedbackRepository()
        val viewModel = FeedbackViewModel(repository, Dispatchers.Unconfined)
        viewModel.prefillContact("  Linh Tran  ", "  linh@example.com  ")
        viewModel.selectCategory(FeedbackCategory.IDEA)
        viewModel.updateMessage("  Add a monthly spending comparison chart.  ")

        viewModel.submit()

        assertEquals(FeedbackSubmissionState.SENT, viewModel.uiState.value.submissionState)
        assertEquals("", viewModel.uiState.value.message)
        assertEquals(
            FeedbackSubmission(
                category = FeedbackCategory.IDEA,
                message = "Add a monthly spending comparison chart.",
                name = "Linh Tran",
                email = "linh@example.com"
            ),
            repository.submission
        )
    }

    @Test
    fun `repository failure exposes a retryable error state`() {
        val repository = FakeFeedbackRepository(Result.failure(IllegalStateException("offline")))
        val viewModel = FeedbackViewModel(repository, Dispatchers.Unconfined)
        viewModel.updateMessage("The app closed while I was saving a transaction.")

        viewModel.submit()

        assertEquals(FeedbackSubmissionState.ERROR, viewModel.uiState.value.submissionState)
    }

    @Test
    fun `changes and duplicate submissions are ignored while sending`() {
        val repository = PendingFeedbackRepository()
        val viewModel = FeedbackViewModel(repository, Dispatchers.Unconfined)
        viewModel.updateMessage("The category selector should not change while sending.")

        viewModel.submit()
        viewModel.selectCategory(FeedbackCategory.QUESTION)
        viewModel.updateMessage("Changed while sending")
        viewModel.submit()

        assertEquals(FeedbackSubmissionState.SUBMITTING, viewModel.uiState.value.submissionState)
        assertEquals(FeedbackCategory.BUG, viewModel.uiState.value.category)
        assertEquals(1, repository.submissionCount)

        repository.complete(Result.success("feedback-id"))
        assertEquals(FeedbackSubmissionState.SENT, viewModel.uiState.value.submissionState)
    }

    private class FakeFeedbackRepository(
        private val result: Result<String> = Result.success("feedback-id")
    ) : FeedbackRepository {
        var submission: FeedbackSubmission? = null

        override suspend fun submit(feedback: FeedbackSubmission): Result<String> {
            submission = feedback
            return result
        }
    }

    private class PendingFeedbackRepository : FeedbackRepository {
        private val result = CompletableDeferred<Result<String>>()
        var submissionCount = 0

        override suspend fun submit(feedback: FeedbackSubmission): Result<String> {
            submissionCount += 1
            return result.await()
        }

        fun complete(value: Result<String>) {
            result.complete(value)
        }
    }
}

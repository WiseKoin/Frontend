package com.wisekoin.data.repository

import com.wisekoin.data.model.FeedbackSubmission
import io.sentry.Sentry
import io.sentry.protocol.Feedback
import io.sentry.protocol.SentryId
import kotlinx.coroutines.CancellationException

interface FeedbackRepository {
    suspend fun submit(feedback: FeedbackSubmission): Result<String>
}

class SentryFeedbackRepository : FeedbackRepository {
    override suspend fun submit(feedback: FeedbackSubmission): Result<String> = try {
        check(Sentry.isEnabled()) { "Sentry feedback is unavailable." }

        val sentryFeedback = Feedback(
            "[${feedback.category.sentryValue}] ${feedback.message.trim()}"
        ).apply {
            name = feedback.name?.trim()?.takeIf(String::isNotEmpty)
            contactEmail = feedback.email?.trim()?.takeIf(String::isNotEmpty)
        }

        val feedbackId = Sentry.feedback().capture(sentryFeedback)
        check(feedbackId != SentryId.EMPTY_ID) { "Sentry did not accept the feedback." }
        Result.success(feedbackId.toString())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}

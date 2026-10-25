package com.wisekoin.data.model

enum class FeedbackCategory(val sentryValue: String) {
    BUG("bug"),
    IDEA("idea"),
    QUESTION("question")
}

data class FeedbackSubmission(
    val category: FeedbackCategory,
    val message: String,
    val name: String?,
    val email: String?
)

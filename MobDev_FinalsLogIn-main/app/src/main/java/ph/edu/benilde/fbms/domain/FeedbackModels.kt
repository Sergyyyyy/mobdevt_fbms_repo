package ph.edu.benilde.fbms.domain

/** UI-facing survey, decoupled from the wire DTO. */
data class Survey(
    val id: String,
    val title: String,
    val questions: List<Question>
)

enum class QuestionType { RATING, YES_NO, MULTIPLE_CHOICE, SHORT_TEXT, LONG_TEXT }

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val required: Boolean,
    val order: Int,
    val options: List<String> = emptyList()
)

/**
 * Mirrors the API's `answer: oneOf[integer, boolean, string]` shape.
 * Every question type maps to exactly one of these, which keeps both the
 * renderer and the submission code exhaustive (the `when` below won't
 * compile if a case is missing).
 */
sealed class AnswerValue {
    data class Rating(val value: Int) : AnswerValue()       // 1..5
    data class YesNo(val value: Boolean) : AnswerValue()
    data class Text(val value: String) : AnswerValue()       // multiple_choice / short_text / long_text

    fun toRawValue(): Any = when (this) {
        is Rating -> value
        is YesNo -> value
        is Text -> value
    }
}

/** In-progress answers keyed by questionId, as the visitor fills out the form. */
typealias AnswerMap = Map<String, AnswerValue>

fun Question.isAnswered(answers: AnswerMap): Boolean {
    val answer = answers[id] ?: return false
    return when (answer) {
        is AnswerValue.Text -> !required || answer.value.isNotBlank()
        else -> true
    }
}

fun Survey.unansweredRequiredQuestions(answers: AnswerMap): List<Question> =
    questions.filter { it.required && !it.isAnswered(answers) }

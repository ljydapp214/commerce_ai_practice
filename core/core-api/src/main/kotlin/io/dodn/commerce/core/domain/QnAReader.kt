package io.dodn.commerce.core.domain

import io.dodn.commerce.core.enums.EntityStatus
import io.dodn.commerce.core.support.OffsetLimit
import io.dodn.commerce.core.support.Page
import io.dodn.commerce.storage.db.core.AnswerRepository
import io.dodn.commerce.storage.db.core.QuestionRepository
import org.springframework.stereotype.Component

@Component
class QnAReader(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
) {
    fun read(productId: Long, offsetLimit: OffsetLimit): Page<QnA> {
        val questions = questionRepository.findByProductIdAndStatus(
            productId,
            EntityStatus.ACTIVE,
            offsetLimit.toPageable(),
        )

        val answers = answerRepository.findByQuestionIdIn(questions.content.map { it.id })
            .filter { it.isActive() }
            .associateBy { it.questionId }

        return Page(
            questions.content.map {
                QnA(
                    question = Question(
                        id = it.id,
                        userId = it.userId,
                        title = it.title,
                        content = it.content,
                    ),
                    answer = answers[it.id]?.let { answer ->
                        Answer(answer.id, answer.adminId, answer.content)
                    } ?: Answer.EMPTY,
                )
            },
            questions.hasNext(),
        )
    }
}

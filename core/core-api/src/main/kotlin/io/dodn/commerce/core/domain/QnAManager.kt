package io.dodn.commerce.core.domain

import io.dodn.commerce.core.support.error.CoreException
import io.dodn.commerce.core.support.error.ErrorType
import io.dodn.commerce.storage.db.core.QuestionEntity
import io.dodn.commerce.storage.db.core.QuestionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class QnAManager(
    private val questionRepository: QuestionRepository,
) {
    fun addQuestion(user: User, productId: Long, content: QuestionContent): Long {
        val saved = questionRepository.save(
            QuestionEntity(
                userId = user.id,
                productId = productId,
                title = content.title,
                content = content.content,
            ),
        )
        return saved.id
    }

    @Transactional
    fun updateQuestion(user: User, questionId: Long, content: QuestionContent): Long {
        val found = questionRepository.findByIdAndUserId(questionId, user.id)?.takeIf { it.isActive() } ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        found.updateContent(content.title, content.content)
        return found.id
    }

    @Transactional
    fun removeQuestion(user: User, questionId: Long): Long {
        val found = questionRepository.findByIdAndUserId(questionId, user.id)?.takeIf { it.isActive() } ?: throw CoreException(ErrorType.NOT_FOUND_DATA)
        found.delete()
        return found.id
    }
}

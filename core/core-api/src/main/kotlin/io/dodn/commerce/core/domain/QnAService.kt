package io.dodn.commerce.core.domain

import io.dodn.commerce.core.support.OffsetLimit
import io.dodn.commerce.core.support.Page
import org.springframework.stereotype.Service

@Service
class QnAService(
    private val qnAReader: QnAReader,
    private val qnAManager: QnAManager,
) {
    fun findQnA(productId: Long, offsetLimit: OffsetLimit): Page<QnA> = qnAReader.read(productId, offsetLimit)

    fun addQuestion(user: User, productId: Long, content: QuestionContent): Long = qnAManager.addQuestion(user, productId, content)

    fun updateQuestion(user: User, questionId: Long, content: QuestionContent): Long = qnAManager.updateQuestion(user, questionId, content)

    fun removeQuestion(user: User, questionId: Long): Long = qnAManager.removeQuestion(user, questionId)

    /**
     * NOTE: 답변은어드민 쪽 기능임
     * fun addAnswer(user: User, questionId: Long, content: String): Long {...}
     * fun updateAnswer(user: User, answerId: Long, content: String): Long {...}
     * fun removeAnswer(user: User, answerId: Long): Long {...}
     */
}

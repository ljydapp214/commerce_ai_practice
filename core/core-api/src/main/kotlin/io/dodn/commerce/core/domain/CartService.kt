package io.dodn.commerce.core.domain

import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartReader: CartReader,
    private val cartManager: CartManager,
) {
    fun getCart(user: User): Cart = cartReader.read(user)

    fun addCartItem(user: User, item: AddCartItem): Long = cartManager.add(user, item)

    fun modifyCartItem(user: User, item: ModifyCartItem): Long = cartManager.modify(user, item)

    fun deleteCartItem(user: User, cartItemId: Long) = cartManager.delete(user, cartItemId)
}

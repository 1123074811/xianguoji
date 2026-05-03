package com.xianguoji.server.module.cart.service;

import com.xianguoji.server.module.cart.dto.CartAddDto;
import com.xianguoji.server.module.cart.dto.CartSelectedDto;
import com.xianguoji.server.module.cart.vo.CartListVO;

public interface CartService {

    CartListVO getCartList(Long uid);

    void addToCart(Long uid, CartAddDto dto);

    void updateQuantity(Long uid, Long cartId, Integer quantity);

    void updateSelected(Long uid, CartSelectedDto dto);

    void deleteItem(Long uid, Long cartId);

    void clearCart(Long uid);

    int getCartCount(Long uid);
}

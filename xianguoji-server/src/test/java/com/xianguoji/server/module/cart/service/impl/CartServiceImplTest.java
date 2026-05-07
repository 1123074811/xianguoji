package com.xianguoji.server.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.cart.dto.CartAddDto;
import com.xianguoji.server.module.cart.dto.CartSelectedDto;
import com.xianguoji.server.module.cart.entity.CartItem;
import com.xianguoji.server.module.cart.mapper.CartItemMapper;
import com.xianguoji.server.module.cart.service.CartService;
import com.xianguoji.server.module.cart.vo.CartListVO;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * TC-UT-CART-001: CartService.addItem 同SKU累加，超过上限取上限
 * TC-UT-CART-002: CartService.addItem SKU已下架
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductSkuMapper skuMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PromotionRuleMapper promotionRuleMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Long testUserId = 1L;
    private Long testSkuId = 1L;
    private Long testProductId = 1L;

    /**
     * TC-UT-CART-001: 同SKU累加
     */
    @Test
    void addToCart_shouldAccumulateQuantity_whenSkuAlreadyExistsInCart() {
        // Arrange
        CartAddDto dto = new CartAddDto();
        dto.setSkuId(testSkuId);
        dto.setQuantity(3);

        ProductSku sku = new ProductSku();
        sku.setId(testSkuId);
        sku.setProductId(testProductId);

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setUserId(testUserId);
        existingItem.setSkuId(testSkuId);
        existingItem.setQuantity(2);

        when(skuMapper.selectById(testSkuId)).thenReturn(sku);
        when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingItem);

        // Act
        cartService.addToCart(testUserId, dto);

        // Assert
        verify(cartItemMapper).updateById(argThat(item -> item.getQuantity() == 5));
        verify(cartItemMapper, never()).insert(any(CartItem.class));
    }

    /**
     * TC-UT-CART-001: 新SKU添加到购物车
     */
    @Test
    void addToCart_shouldInsertNewItem_whenSkuNotExistsInCart() {
        // Arrange
        CartAddDto dto = new CartAddDto();
        dto.setSkuId(testSkuId);
        dto.setQuantity(2);

        ProductSku sku = new ProductSku();
        sku.setId(testSkuId);
        sku.setProductId(testProductId);

        when(skuMapper.selectById(testSkuId)).thenReturn(sku);
        when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        cartService.addToCart(testUserId, dto);

        // Assert
        verify(cartItemMapper).insert(argThat(item -> 
            item.getUserId().equals(testUserId) && 
            item.getSkuId().equals(testSkuId) && 
            item.getQuantity() == 2 &&
            item.getSelected() == 1
        ));
        verify(cartItemMapper, never()).updateById(any(CartItem.class));
    }

    /**
     * TC-UT-CART-002: SKU不存在
     */
    @Test
    void addToCart_shouldThrowException_whenSkuNotExists() {
        // Arrange
        CartAddDto dto = new CartAddDto();
        dto.setSkuId(999L);
        dto.setQuantity(1);

        when(skuMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> cartService.addToCart(testUserId, dto));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        assertTrue(exception.getMessage().contains("SKU不存在"));
    }

    /**
     * 测试更新数量
     */
    @Test
    void updateQuantity_shouldUpdate_whenItemExistsAndBelongsToUser() {
        // Arrange
        Long cartId = 1L;
        Integer newQuantity = 5;

        CartItem item = new CartItem();
        item.setId(cartId);
        item.setUserId(testUserId);
        item.setQuantity(2);

        when(cartItemMapper.selectById(cartId)).thenReturn(item);

        // Act
        cartService.updateQuantity(testUserId, cartId, newQuantity);

        // Assert
        verify(cartItemMapper).updateById(argThat(i -> i.getQuantity() == newQuantity));
    }

    /**
     * 测试更新数量 - 购物车项不存在
     */
    @Test
    void updateQuantity_shouldThrowException_whenItemNotExists() {
        // Arrange
        Long cartId = 1L;
        when(cartItemMapper.selectById(cartId)).thenReturn(null);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> 
            cartService.updateQuantity(testUserId, cartId, 5));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * 测试更新数量 - 越权（不属于当前用户）
     */
    @Test
    void updateQuantity_shouldThrowException_whenItemBelongsToOtherUser() {
        // Arrange
        Long cartId = 1L;
        Long otherUserId = 999L;

        CartItem item = new CartItem();
        item.setId(cartId);
        item.setUserId(otherUserId);

        when(cartItemMapper.selectById(cartId)).thenReturn(item);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> 
            cartService.updateQuantity(testUserId, cartId, 5));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * 测试更新选中状态
     */
    @Test
    void updateSelected_shouldUpdateSelectedStatus() {
        // Arrange
        CartSelectedDto dto = new CartSelectedDto();
        dto.setIds(Arrays.asList(1L, 2L, 3L));
        dto.setSelected(1);

        // Act
        cartService.updateSelected(testUserId, dto);

        // Assert
        verify(cartItemMapper).update(null, any());
    }

    /**
     * 测试删除购物车项
     */
    @Test
    void deleteItem_shouldDelete_whenItemBelongsToUser() {
        // Arrange
        Long cartId = 1L;

        // Act
        cartService.deleteItem(testUserId, cartId);

        // Assert
        verify(cartItemMapper).delete(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试清空购物车
     */
    @Test
    void clearCart_shouldDeleteAllItemsForUser() {
        // Act
        cartService.clearCart(testUserId);

        // Assert
        verify(cartItemMapper).delete(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取购物车数量
     */
    @Test
    void getCartCount_shouldReturnCount() {
        // Arrange
        when(cartItemMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // Act
        int count = cartService.getCartCount(testUserId);

        // Assert
        assertEquals(3, count);
    }

    /**
     * 测试获取购物车列表 - 空购物车
     */
    @Test
    void getCartList_shouldReturnEmptyCart_whenUserHasNoItems() {
        // Arrange
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        CartListVO result = cartService.getCartList(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
    }

    /**
     * 测试获取购物车列表 - 有商品
     */
    @Test
    void getCartList_shouldReturnCartWithItems() {
        // Arrange
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSkuId(testSkuId);
        item.setQuantity(2);
        item.setSelected(1);

        ProductSku sku = new ProductSku();
        sku.setId(testSkuId);
        sku.setProductId(testProductId);
        sku.setPrice(new BigDecimal("29.90"));
        sku.setSpecName("500g");
        sku.setStock(100);

        Product product = new Product();
        product.setId(testProductId);
        product.setName("测试商品");
        product.setMainImage("http://example.com/image.jpg");
        product.setStatus(1);

        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(item));
        when(skuMapper.selectById(testSkuId)).thenReturn(sku);
        when(productMapper.selectById(testProductId)).thenReturn(product);
        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        CartListVO result = cartService.getCartList(testUserId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        assertEquals(1, result.getItems().size());
        assertEquals(new BigDecimal("59.80"), result.getTotalAmount()); // 29.90 * 2
    }

    /**
     * 测试获取购物车列表 - 满减优惠
     */
    @Test
    void getCartList_shouldApplyDiscount_whenTotalAmountMeetsThreshold() {
        // Arrange
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSkuId(testSkuId);
        item.setQuantity(5);
        item.setSelected(1);

        ProductSku sku = new ProductSku();
        sku.setId(testSkuId);
        sku.setProductId(testProductId);
        sku.setPrice(new BigDecimal("20.00"));
        sku.setSpecName("500g");
        sku.setStock(100);

        Product product = new Product();
        product.setId(testProductId);
        product.setName("测试商品");
        product.setMainImage("http://example.com/image.jpg");
        product.setStatus(1);

        PromotionRule rule = new PromotionRule();
        rule.setId(1L);
        rule.setName("满100减10");
        rule.setMinAmount(new BigDecimal("100"));
        rule.setDiscount(new BigDecimal("10"));
        rule.setStatus(1);

        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(item));
        when(skuMapper.selectById(testSkuId)).thenReturn(sku);
        when(productMapper.selectById(testProductId)).thenReturn(product);
        when(promotionRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(rule));

        // Act
        CartListVO result = cartService.getCartList(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount()); // 20 * 5
        assertEquals(new BigDecimal("10.00"), result.getDiscountAmount());
        assertEquals("满100减10", result.getPromotionTip());
    }
}


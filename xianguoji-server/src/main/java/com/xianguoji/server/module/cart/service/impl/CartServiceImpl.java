package com.xianguoji.server.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.cart.dto.CartAddDto;
import com.xianguoji.server.module.cart.dto.CartSelectedDto;
import com.xianguoji.server.module.cart.entity.CartItem;
import com.xianguoji.server.module.cart.mapper.CartItemMapper;
import com.xianguoji.server.module.cart.service.CartService;
import com.xianguoji.server.module.cart.vo.CartItemVO;
import com.xianguoji.server.module.cart.vo.CartListVO;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final PromotionRuleMapper promotionRuleMapper;

    @Override
    public CartListVO getCartList(Long uid) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, uid).orderByDesc(CartItem::getUpdatedAt));

        List<CartItemVO> voList = items.stream().map(this::toCartItemVO).toList();

        BigDecimal totalAmount = voList.stream()
                .filter(i -> i.getSelected() == 1)
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 满减计算
        BigDecimal discountAmount = BigDecimal.ZERO;
        String promotionTip = "";
        List<PromotionRule> rules = promotionRuleMapper.selectList(
                new LambdaQueryWrapper<PromotionRule>().eq(PromotionRule::getStatus, 1).orderByDesc(PromotionRule::getMinAmount));
        for (PromotionRule rule : rules) {
            if (totalAmount.compareTo(rule.getMinAmount()) >= 0) {
                discountAmount = rule.getDiscount();
                promotionTip = rule.getName();
                break;
            }
        }

        return CartListVO.builder()
                .items(voList)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .promotionTip(promotionTip)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long uid, CartAddDto dto) {
        ProductSku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null) throw new BizException(ResultCode.NOT_FOUND, "SKU不存在");

        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, uid)
                        .eq(CartItem::getSkuId, dto.getSkuId()));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartItemMapper.updateById(existing);
        } else {
            CartItem item = new CartItem();
            item.setUserId(uid);
            item.setProductId(sku.getProductId());
            item.setSkuId(dto.getSkuId());
            item.setQuantity(dto.getQuantity());
            item.setSelected(1);
            cartItemMapper.insert(item);
        }
    }

    @Override
    public void updateQuantity(Long uid, Long cartId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(cartId);
        if (item == null || !item.getUserId().equals(uid)) {
            throw new BizException(ResultCode.NOT_FOUND, "购物车项不存在");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSelected(Long uid, CartSelectedDto dto) {
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, uid)
                .in(CartItem::getId, dto.getIds())
                .set(CartItem::getSelected, dto.getSelected()));
    }

    @Override
    public void deleteItem(Long uid, Long cartId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, cartId)
                .eq(CartItem::getUserId, uid));
    }

    @Override
    public void clearCart(Long uid) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, uid));
    }

    @Override
    public int getCartCount(Long uid) {
        return Math.toIntExact(cartItemMapper.selectCount(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, uid)));
    }

    private CartItemVO toCartItemVO(CartItem item) {
        ProductSku sku = skuMapper.selectById(item.getSkuId());
        Product product = productMapper.selectById(item.getProductId());

        BigDecimal price = sku != null ? sku.getPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemVO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .productName(product != null ? product.getName() : "商品已删除")
                .mainImage(product != null ? product.getMainImage() : "")
                .specName(sku != null ? sku.getSpecName() : "")
                .price(price)
                .originalPrice(sku != null ? sku.getOriginalPrice() : null)
                .stock(sku != null ? sku.getStock() : 0)
                .productStatus(product != null ? product.getStatus() : 0)
                .quantity(item.getQuantity())
                .selected(item.getSelected())
                .subtotal(subtotal)
                .build();
    }
}

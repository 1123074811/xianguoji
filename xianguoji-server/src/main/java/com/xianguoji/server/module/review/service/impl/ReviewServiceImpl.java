package com.xianguoji.server.module.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.review.dto.ReviewAddDto;
import com.xianguoji.server.module.review.entity.Review;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import com.xianguoji.server.module.review.service.ReviewService;
import com.xianguoji.server.module.review.vo.ReviewSummaryVO;
import com.xianguoji.server.module.review.vo.ReviewVO;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    @Override
    public List<OrderItem> getPendingReviews(Long uid) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getIsReviewed, 0)
                        .inSql(OrderItem::getOrderId,
                                "SELECT id FROM `order` WHERE user_id = " + uid + " AND status = 5"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long uid, ReviewAddDto dto) {
        OrderItem oi = orderItemMapper.selectById(dto.getOrderItemId());
        if (oi == null) throw new BizException(ResultCode.NOT_FOUND, "订单项不存在");
        if (oi.getIsReviewed() == 1) throw new BizException(ResultCode.BIZ_ERROR, "已评价");

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setOrderItemId(dto.getOrderItemId());
        review.setProductId(oi.getProductId());
        review.setUserId(uid);
        review.setRating(dto.getRating());
        review.setFreshnessRating(dto.getFreshnessRating());
        review.setValueRating(dto.getValueRating());
        review.setPackageRating(dto.getPackageRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        review.setIsHidden(0);
        reviewMapper.insert(review);

        oi.setIsReviewed(1);
        orderItemMapper.updateById(oi);
    }

    @Override
    public PageVO<ReviewVO> getProductReviews(Long productId, String filter, Integer page, Integer size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .eq(Review::getIsHidden, 0);
        if ("withImage".equals(filter)) {
            wrapper.isNotNull(Review::getImages).apply("JSON_LENGTH(images) > 0");
        } else if ("good".equals(filter)) {
            wrapper.ge(Review::getRating, 4);
        } else if ("middle".equals(filter)) {
            wrapper.eq(Review::getRating, 3);
        } else if ("bad".equals(filter)) {
            wrapper.le(Review::getRating, 2);
        }
        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> p = reviewMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReviewVO> voList = p.getRecords().stream().map(this::toReviewVO).toList();
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public ReviewSummaryVO getReviewSummary(Long productId) {
        Long total = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId).eq(Review::getIsHidden, 0));
        Long good = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId).eq(Review::getIsHidden, 0).ge(Review::getRating, 4));
        Long middle = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId).eq(Review::getIsHidden, 0).eq(Review::getRating, 3));
        Long bad = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId).eq(Review::getIsHidden, 0).le(Review::getRating, 2));
        Long withImage = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getProductId, productId).eq(Review::getIsHidden, 0)
                        .isNotNull(Review::getImages).apply("JSON_LENGTH(images) > 0"));

        return ReviewSummaryVO.builder()
                .totalCount(total)
                .avgRating(null) // TODO: 聚合查询
                .goodCount(good)
                .middleCount(middle)
                .badCount(bad)
                .withImageCount(withImage)
                .build();
    }

    @Override
    public PageVO<ReviewVO> adminReviewPage(String filter, Integer page, Integer size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if ("pendingReply".equals(filter)) wrapper.isNull(Review::getMerchantReply);
        else if ("withImage".equals(filter)) wrapper.isNotNull(Review::getImages).apply("JSON_LENGTH(images) > 0");
        else if ("bad".equals(filter)) wrapper.le(Review::getRating, 2);
        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> p = reviewMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReviewVO> voList = p.getRecords().stream().map(this::toReviewVO).toList();
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public void replyReview(Long id, String reply) {
        Review review = reviewMapper.selectById(id);
        if (review == null) throw new BizException(ResultCode.NOT_FOUND);
        review.setMerchantReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    public void toggleHidden(Long id, Integer hidden) {
        Review review = reviewMapper.selectById(id);
        if (review == null) throw new BizException(ResultCode.NOT_FOUND);
        review.setIsHidden(hidden);
        reviewMapper.updateById(review);
    }

    private ReviewVO toReviewVO(Review r) {
        String userName = "匿名用户";
        String userAvatar = "";
        if (r.getIsAnonymous() == 0) {
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                userName = user.getNickname();
                userAvatar = user.getAvatar();
            }
        }
        return ReviewVO.builder()
                .id(r.getId())
                .productId(r.getProductId())
                .userName(userName)
                .userAvatar(userAvatar)
                .rating(r.getRating())
                .freshnessRating(r.getFreshnessRating())
                .valueRating(r.getValueRating())
                .packageRating(r.getPackageRating())
                .content(r.getContent())
                .images(r.getImages())
                .isAnonymous(r.getIsAnonymous())
                .merchantReply(r.getMerchantReply())
                .repliedAt(r.getRepliedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

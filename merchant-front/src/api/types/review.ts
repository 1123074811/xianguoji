/**
 * 后端 ReviewVO（com.xianguoji.server.module.review.vo.ReviewVO）
 * 商家端列表与公共评价列表共用同一个 VO
 */
export interface AdminReviewVO {
  id: number;
  productId: number;
  userName: string;
  userAvatar: string | null;
  rating: number;
  freshnessRating: number | null;
  valueRating: number | null;
  packageRating: number | null;
  content: string;
  images: string[];
  isAnonymous: 0 | 1;
  merchantReply: string | null;
  repliedAt: string | null;
  createdAt: string;
}

export interface ReviewVO {
  id: number;
  productId: number;
  userName: string;
  userAvatar: string;
  rating: number;
  freshnessRating?: number;
  valueRating?: number;
  packageRating?: number;
  content: string;
  images: string[];
  isAnonymous: number;
  merchantReply?: string;
  repliedAt?: string;
  createdAt: string;
}
export interface ReviewSummaryVO {
  totalCount: number;
  avgRating: number;
  withImageCount: number;
  goodRate: number;
}
export interface ReviewSubmitDto {
  orderId: number;
  orderItemId: number;
  rating: number;
  freshnessRating?: number;
  valueRating?: number;
  packageRating?: number;
  content: string;
  images?: string[];
  isAnonymous?: number;
}

export interface PendingReviewItemVO {
  id: number;
  orderId: number;
  productId: number;
  skuId: number;
  productName: string;
  specName: string;
  image: string;
  price: string;
  originalPrice: string;
  quantity: number;
  subtotal: string;
  isReviewed: number;
}

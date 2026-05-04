export interface ProductVO {
  id: number;
  name: string;
  subtitle: string;
  categoryId: number;
  mainImage: string;
  minPrice: string;
  maxPrice: string;
  totalStock: number;
  sales: number;
  isRecommend: 0 | 1;
  supportDelivery: 0 | 1;
  supportPickup: 0 | 1;
}
export interface ProductDetailVO extends ProductVO {
  videoUrl?: string;
  description: string;
  skuList: SkuVO[];
  carouselImages: string[];
  detailImages: string[];
  reviewSummary: { totalCount: number; avgRating: number; withImageCount: number; };
  isFavorite: boolean;
}
export interface SkuVO {
  id: number;
  specName: string;
  price: string;
  originalPrice: string;
  stock: number;
  isDefault: 0 | 1;
}
export interface CategoryTreeVO {
  id: number;
  name: string;
  icon: string;
  sort: number;
  children: CategoryTreeVO[];
}
export interface BannerVO {
  id: number;
  title: string;
  image: string;
  linkType: number;
  linkValue: string;
}

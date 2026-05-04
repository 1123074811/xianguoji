export interface AdminProductVO {
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
  status: 0 | 1;
  createTime: string;
  updateTime: string;
}
export interface AdminProductDetailVO extends AdminProductVO {
  description: string;
  videoUrl?: string;
  skuList: AdminSkuVO[];
  carouselImages: string[];
  detailImages: string[];
}
export interface AdminSkuVO {
  id: number;
  specName: string;
  price: string;
  originalPrice: string;
  stock: number;
  isDefault: 0 | 1;
}
export interface AdminCategoryVO {
  id: number;
  name: string;
  icon: string;
  sort: number;
  parentId: number;
  children?: AdminCategoryVO[];
}
export interface AdminBannerVO {
  id: number;
  title: string;
  image: string;
  linkType: number;
  linkValue: string;
  sort: number;
  status: 0 | 1;
}
export interface ProductCreateDto {
  name: string;
  subtitle: string;
  categoryId: number;
  mainImage: string;
  description: string;
  videoUrl?: string;
  carouselImages: string[];
  detailImages: string[];
  isRecommend?: 0 | 1;
  supportDelivery?: 0 | 1;
  supportPickup?: 0 | 1;
  skuList: Omit<AdminSkuVO, 'id'>[];
}

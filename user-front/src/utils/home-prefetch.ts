import { catalogApi } from '@/api/modules/catalog';
import { promoApi } from '@/api/modules/promo';
import { shopApi } from '@/api/modules/shop';
import type { BannerVO, CategoryTreeVO, ProductVO } from '@/api/types/catalog';
import type { CouponVO, GroupBuyActivityVO } from '@/api/types/promo';
import type { DeliverySettingVO, ShopVO } from '@/api/types/shop';

const HOME_PREFETCH_KEY = 'home_prefetch_data';
const HOME_PREFETCH_TTL = 60 * 1000;

export interface HomePrefetchData {
  categories: CategoryTreeVO[];
  banners: BannerVO[];
  coupons: CouponVO[];
  groupBuys: GroupBuyActivityVO[];
  recommendedGoods: ProductVO[];
  shopInfo: ShopVO | null;
  deliverySetting: DeliverySettingVO | null;
  cachedAt: number;
}

export async function prefetchHomeData() {
  const [categories, banners, coupons, groupBuyPage, recommendedGoods, shopInfo, deliverySetting] = await Promise.all([
    catalogApi.categoryTree(),
    catalogApi.bannerList(),
    promoApi.couponList(),
    promoApi.groupBuyPage({ page: 1, size: 6 }),
    catalogApi.recommend(),
    shopApi.shopInfo().catch(() => null),
    shopApi.deliverySetting().catch(() => null),
  ]);

  const data: HomePrefetchData = {
    categories,
    banners,
    coupons,
    groupBuys: groupBuyPage.list || [],
    recommendedGoods,
    shopInfo,
    deliverySetting,
    cachedAt: Date.now(),
  };

  uni.setStorageSync(HOME_PREFETCH_KEY, data);
  return data;
}

export function readHomePrefetchData() {
  try {
    const data = uni.getStorageSync(HOME_PREFETCH_KEY) as HomePrefetchData | '';
    if (!data || Date.now() - data.cachedAt > HOME_PREFETCH_TTL) return null;
    return data;
  } catch (e) {
    return null;
  }
}

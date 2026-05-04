export interface CartItemVO {
  id: number;
  productId: number;
  skuId: number;
  productName: string;
  mainImage: string;
  specName: string;
  price: string;
  originalPrice: string;
  stock: number;
  productStatus: number;
  quantity: number;
  selected: 0 | 1;
  subtotal: string;
}
export interface CartListVO {
  items: CartItemVO[];
  totalAmount: string;
  discountAmount: string;
  promotionTip: string;
}

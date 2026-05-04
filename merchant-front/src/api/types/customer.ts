/**
 * 后端 stat.controller.CustomerController.CustomerVO 内部类
 */
export interface CustomerVO {
  id: number;
  nickname: string;
  avatar: string | null;
  phone: string;
  tag: string;
  orderCount: number;
  totalSpend: string;
  lastOrderTime: string | null;
  registerTime: string;
}

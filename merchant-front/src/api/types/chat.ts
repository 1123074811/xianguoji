export interface ChatMessageVO {
  id: number;
  senderType: 0 | 1;
  msgType: 'text' | 'product' | 'image';
  content: string;
  productCard?: ProductCardVO;
  images?: string[];
  isRead: 0 | 1;
  senderAvatar?: string;
  createdAt: string;
}

export interface ProductCardVO {
  productId: number;
  name: string;
  mainImage: string;
  price: string;
  specName: string;
}

export interface ChatUserVO {
  userId: number;
  nickname: string;
  avatar: string;
  lastMessage: string;
  lastTime: string;
  unreadCount: number;
}

export interface ChatSendDto {
  msgType: 'text' | 'product' | 'image';
  content?: string;
  productId?: number;
  images?: string[];
}

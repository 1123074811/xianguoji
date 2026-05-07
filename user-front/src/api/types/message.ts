export interface MessageVO {
  id: number;
  title: string;
  content: string;
  type: number;
  isRead: 0 | 1;
  createdAt: string;
  linkUrl?: string;
}
export interface MessageUnreadCounts {
  all: number;
  system: number;
  promotion: number;
  logistics: number;
  chat: number;
}
export interface FeedbackVO {
  id: number;
  type: string;
  content: string;
  images: string[];
  contact?: string;
  replyContent?: string;
  replyTime?: string;
  status: number;
  createdAt: string;
}
export interface FeedbackSubmitDto {
  type?: string;
  content: string;
  images?: string[];
  contact?: string;
}

export interface MessageVO {
  id: number;
  title: string;
  content: string;
  type: number;
  isRead: 0 | 1;
  createTime: string;
}
export interface FeedbackVO {
  id: number;
  content: string;
  images: string[];
  replyContent?: string;
  replyTime?: string;
  status: number;
  createTime: string;
}
export interface FeedbackSubmitDto {
  content: string;
  images?: string[];
}

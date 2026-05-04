export interface AdminFeedbackVO {
  id: number;
  userId: number;
  nickname: string;
  content: string;
  images: string[];
  replyContent?: string;
  replyTime?: string;
  status: number;
  createTime: string;
}
export interface NotifySettingVO {
  id: number;
  type: number;
  typeName: string;
  enabled: boolean;
}

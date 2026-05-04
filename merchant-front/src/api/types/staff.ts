export interface StaffVO {
  id: number;
  username: string;
  nickname: string;
  phone: string;
  staffRole: 'owner' | 'admin' | 'packer' | 'courier';
  status: 0 | 1;
  createTime: string;
}
export interface StaffCreateDto {
  username: string;
  password: string;
  nickname: string;
  phone: string;
  staffRole: 'admin' | 'packer' | 'courier';
}

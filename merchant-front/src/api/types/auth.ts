/**
 * 后端 LoginVO（com.xianguoji.server.module.auth.vo.LoginVO）
 * - userInfo 字段名与用户端共用 UserInfoVO；商家登录时 role='staff'，staffRole 表示商家角色
 * - 后端不返回 username/permissions；权限按 staffRole 在前端做粗粒度控制
 */
export interface AdminLoginVO {
  token: string;
  expireAt: string;
  userInfo: AdminUserInfoVO;
}

export interface AdminUserInfoVO {
  id: number;
  nickname: string;
  avatar: string | null;
  phone: string;
  role: 'staff';
  staffRole: 'owner' | 'admin' | 'packer' | 'courier';
}

export interface AdminLoginDto {
  username: string;
  password: string;
  captchaKey: string;
  captchaCode: string;
}

export interface CaptchaVO {
  captchaKey: string;
  captchaImage: string;
}

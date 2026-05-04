export interface R<T = any> { code: number; msg: string; data: T; ts: number; }
export interface PageVO<T> { total: number; list: T[]; page: number; size: number; }
export interface PageQry { page?: number; size?: number; }

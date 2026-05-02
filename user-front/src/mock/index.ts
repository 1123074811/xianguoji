import goods from './goods';
import categories from './categories';
import userInfo from './userInfo';

export const getMockData = async <T>(fileName: string): Promise<T> => {
  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 300));
  
  const mockFiles: Record<string, any> = {
    'goods.json': goods,
    'categories.json': categories,
    'userInfo.json': userInfo
  };

  return mockFiles[fileName] as T;
};

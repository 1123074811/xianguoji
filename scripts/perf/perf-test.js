// k6 性能测试脚本 - 基于 TEST_PLAN_DETAIL.md §5.1
// 运行: k6 run scripts/perf/perf-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8080';

// ============================================================
// TC-PERF-001: 首页/分类/详情 P95 < 500/600ms
// ============================================================
export const options = {
  scenarios: {
    // TC-PERF-001: 首页列表 1k并发
    catalog_read: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '2m', target: 1000 },
        { duration: '30s', target: 0 },
      ],
      thresholds: {
        http_req_duration: ['p(95)<600'],
        http_req_failed: ['rate<0.01'],
      },
    },
  },
};

export default function () {
  // TC-PERF-001a: 分类树
  const catRes = http.get(`${BASE_URL}/api/pub/catalog/category/tree`);
  check(catRes, {
    'TC-PERF-001a 分类树 status 200': (r) => r.status === 200,
    'TC-PERF-001a 分类树 < 600ms': (r) => r.timings.duration < 600,
  });

  // TC-PERF-001b: 商品列表
  const prodRes = http.get(`${BASE_URL}/api/pub/catalog/product/page?page=1&size=20`);
  check(prodRes, {
    'TC-PERF-001b 商品列表 status 200': (r) => r.status === 200,
    'TC-PERF-001b 商品列表 < 500ms': (r) => r.timings.duration < 500,
  });

  // TC-PERF-001c: 商品详情
  const detailRes = http.get(`${BASE_URL}/api/pub/catalog/product/1`);
  check(detailRes, {
    'TC-PERF-001c 商品详情 status 200': (r) => r.status === 200 || r.status === 404,
  });

  // TC-PERF-009: Banner (缓存命中测试)
  const bannerRes = http.get(`${BASE_URL}/api/pub/catalog/banner/list`);
  check(bannerRes, {
    'TC-PERF-009 Banner < 100ms (缓存命中)': (r) => r.timings.duration < 100,
  });

  sleep(1);
}

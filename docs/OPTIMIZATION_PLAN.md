# 鲜果记后端优化方案（借鉴苍穹外卖 + 黑马点评）

本文档面向**自动化实现 AI**。每个任务遵循统一格式：

- **现状定位**：指出待改代码（含文件路径与行号）
- **目标**：要达到什么效果
- **方案**：具体改造方式（含代码骨架）
- **验收**：可验证的判断条件

> 阅读顺序即推荐实现顺序。P0 是数据正确性/并发安全，P1 是性能/缓存，P2 是工程化提升。
> 所有新增公共组件统一放到 `xianguoji-server/src/main/java/com/xianguoji/server/common/` 下对应子包。

---

## P0 - 并发安全与数据一致性

### P0-1. 拼团 currentSize 原子自增 + 超员防护

**现状定位**
- `module/promo/service/impl/GroupBuyServiceImpl.java:157` join 流程中：
  ```java
  instance.setCurrentSize(instance.getCurrentSize() + 1);
  if (instance.getCurrentSize() >= instance.getTargetSize()) { ... }
  instanceMapper.updateById(instance);
  ```
  Java 内存中 +1 再 update，并发抢团下会**超员**（targetSize=3 但 4 人成团）。

**目标**：保证 currentSize 严格原子自增，且不超过 targetSize。

**方案**（推荐 A，B 备选）

A. 单 SQL 原子更新（最小改动）：
```java
int rows = instanceMapper.update(null,
    new LambdaUpdateWrapper<GroupBuyInstance>()
        .eq(GroupBuyInstance::getId, instanceId)
        .eq(GroupBuyInstance::getStatus, 1)
        .lt(GroupBuyInstance::getCurrentSize, instance.getTargetSize())
        .setSql("current_size = current_size + 1"));
if (rows == 0) throw new BizException(ResultCode.GROUP_BUY_ENDED, "拼团已满或已结束");
// 重新查 currentSize 判断是否成团
GroupBuyInstance fresh = instanceMapper.selectById(instanceId);
if (fresh.getCurrentSize().equals(fresh.getTargetSize())) {
    instanceMapper.update(null, new LambdaUpdateWrapper<GroupBuyInstance>()
        .eq(GroupBuyInstance::getId, instanceId)
        .eq(GroupBuyInstance::getStatus, 1)  // 防止重复成团
        .set(GroupBuyInstance::getStatus, 2)
        .set(GroupBuyInstance::getSuccessAt, LocalDateTime.now()));
}
```

B. Redisson 分布式锁：`lock("groupbuy:" + instanceId)`，锁内做 `select for update` + 原子计数。

**验收**
- 写并发测试：100 线程同抢同一 `targetSize=3` 的团，最终 participant 总数 = 3，instance.currentSize = 3。
- 第 4+ 人请求得到 `GROUP_BUY_ENDED` 业务异常。

---

### P0-2. 库存扣减引入 Redis 预扣 + DB 回写（高并发场景）

**现状定位**
- `module/order/service/impl/OrderServiceImpl.java:184` 直接 `UPDATE sku SET stock=stock-?`，DB 行锁，QPS 上限受限。
- 同样在 `GroupBuyServiceImpl.java:85,142` 重复出现。

**目标**：参考黑马点评秒杀方案，活动/抢购场景下用 Redis Lua 预扣库存，DB 异步落账，让普通下单仍走 DB 乐观锁。

**方案**

1. 新增 `common/util/StockRedisHelper.java`：
   - 商品上架/库存变动时，把 `stock:sku:{skuId}` 写入 Redis（注意写入时机：管理后台改库存、定时任务重置）。
   - Lua 脚本（参考黑马 `seckill.lua`）：
     ```lua
     -- KEYS[1]=stock:sku:{id}, ARGV[1]=qty
     local stock = tonumber(redis.call('get', KEYS[1]))
     if not stock or stock < tonumber(ARGV[1]) then return 0 end
     redis.call('decrby', KEYS[1], ARGV[1])
     return 1
     ```
2. `OrderServiceImpl.submit` 改为：
   - 普通下单：保留原 DB 乐观锁（兜底，避免 Redis 不可用导致下不了单）
   - 拼团/限时活动单：走 Redis 预扣 → 入消息队列（先用 Spring Events / @Async + Disruptor，后续可换 Stream）→ 异步线程消费写订单
3. 失败补偿：Redis 扣成功但 DB 写订单失败 → 回滚 `incrby`。

**验收**
- 1000 并发抢 100 库存活动，最终成功订单数恰好 100，无超卖。
- 普通下单仍正常走 DB 流程（关 Redis 后能下单）。

---

### P0-3. 优惠券领取防超发（参考黑马点评券）

**现状定位**
- `module/promo/controller/CouponController.java` 领券接口（请阅读源码确认）当前是 select + insert，没有总量限制原子保护。

**目标**：单券一人一张 + 全局限量，绝对不超发。

**方案**

1. Coupon 表加字段：`total_stock`（总发放量）、`per_user_limit`（每人限领）。
2. 领券 Lua 脚本：
   ```lua
   -- KEYS[1]=coupon:stock:{id}, KEYS[2]=coupon:user:{id}:{uid}
   local stock = tonumber(redis.call('get', KEYS[1]))
   if not stock or stock <= 0 then return 1 end          -- 库存不足
   if redis.call('exists', KEYS[2]) == 1 then return 2 end -- 已领过
   redis.call('decr', KEYS[1])
   redis.call('set', KEYS[2], 1)
   return 0
   ```
3. 领券 service：Lua 通过 → 写 `user_coupon` 表（异步或同步皆可，同步更稳）。
4. 失败回滚：若 DB 写失败，`incr` Redis 库存、`del` 用户标记。

**验收**
- 5000 用户抢 100 张券，最终 user_coupon 表恰好 100 行，每个 userId 唯一。

---

### P0-4. 定时任务取消订单的并发安全

**现状定位**：`common/scheduler/ScheduledTasks.java:50` `cancelUnpaidOrders`：
```java
List<Order> orders = orderMapper.selectList(...status=0 ...);
for (Order o : orders) { o.setStatus(CANCELLED); orderMapper.updateById(o); }
```
查询和 update 之间用户可能完成支付，导致**已支付订单被错误取消**。

**目标**：以 status 作为乐观锁条件。

**方案**：改成条件 update：
```java
int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
    .eq(Order::getId, order.getId())
    .eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())  // 关键
    .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
    .set(Order::getCancelReason, "超时未付款，系统自动取消"));
if (rows == 0) continue; // 用户刚支付，跳过
// 然后才执行释放库存、退券
```
同时把扫描 SQL 加 `LIMIT 200` 分批，避免一次拉太多。

**验收**：单测模拟 status 在 update 前被改为已支付，定时任务不会覆盖。

---

### P0-5. updateUserTags N+1 改单 SQL

**现状**：`ScheduledTasks.java:117` 对每个 regular 用户单独 count 订单。1 万用户 = 1 万次查询。

**方案**：单条 update：
```sql
UPDATE users SET tag = 'silent'
WHERE tag = 'regular'
  AND id NOT IN (
    SELECT DISTINCT user_id FROM orders WHERE created_at > #{threshold}
  )
```
若 IN 集大可改 LEFT JOIN：
```sql
UPDATE users u
LEFT JOIN (SELECT DISTINCT user_id FROM orders WHERE created_at > ?) o
  ON o.user_id = u.id
SET u.tag = 'silent'
WHERE u.tag = 'regular' AND o.user_id IS NULL
```
在 `UserMapper` 写自定义 XML 方法 `updateSilentTag(LocalDateTime threshold)`。

**验收**：扫描 10w 用户 < 1s。

---

## P1 - 缓存体系（黑马点评核心思想）

### P1-1. 引入 Spring Cache + Redis（高频读路径）

**目标**：店铺营业状态、商品分类、首页 banner、商品详情、套餐列表等热点数据走 Redis 缓存。

**实现要点**

1. `pom.xml` 加 `spring-boot-starter-cache`。
2. 新增 `common/config/CacheConfig.java`：
   ```java
   @EnableCaching
   @Configuration
   public class CacheConfig {
       @Bean
       public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
           RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
               .entryTtl(Duration.ofMinutes(30))
               .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
               .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
               .computePrefixWith(name -> "xgj:cache:" + name + ":");
           // 不同业务不同 TTL
           Map<String, RedisCacheConfiguration> per = new HashMap<>();
           per.put("shop", cfg.entryTtl(Duration.ofHours(2)));
           per.put("category", cfg.entryTtl(Duration.ofHours(6)));
           per.put("product", cfg.entryTtl(Duration.ofMinutes(10)));
           return RedisCacheManager.builder(cf).cacheDefaults(cfg).withInitialCacheConfigurations(per).build();
       }
   }
   ```
3. 注解使用规则：
   - `@Cacheable(value="product", key="#id")` 在商品详情查询
   - `@CacheEvict(value="product", key="#dto.id")` 在商品更新/上下架
   - `@CacheEvict(value="category", allEntries=true)` 在分类增删改

4. 替换以下位置（请 AI 在代码中扫描并加注解）：
   - `OrderServiceImpl.java:85` `checkShopOpen()` → `shopService.getShopStatus()` 加 `@Cacheable(value="shop", key="'status'")`
   - `module/catalog` 商品分类列表、商品详情
   - 首页 banner 列表

**验收**：
- 商品详情接口压测 P99 由 ~30ms 降到 < 5ms。
- 修改商品后，3s 内（最迟）端上能看到新值。

---

### P1-2. 缓存穿透防护（缓存空值）

**目标**：参考黑马点评，避免恶意请求不存在 ID 击穿到 DB。

**方案**：在商品/店铺查询的 service 中：
- 查 Redis：null → 查 DB → DB 也 null → 写入空对象（值为字符串 `""`，TTL 2 分钟）
- 之后 Redis 命中 `""` 直接返回 `NotFound`

封装到 `common/util/CacheClient.java`（黑马点评同名工具类思想）：
```java
public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                      Function<ID, R> dbFallback, Long ttl, TimeUnit unit) {
    String key = keyPrefix + id;
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(json)) return JSONUtil.toBean(json, type);
    if (json != null) return null; // 命中空值
    R r = dbFallback.apply(id);
    if (r == null) {
        stringRedisTemplate.opsForValue().set(key, "", 2, TimeUnit.MINUTES);
        return null;
    }
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), ttl, unit);
    return r;
}
```

**验收**：相同不存在 ID 重复 100 次请求，DB 查询日志只出现 1 次。

---

### P1-3. 缓存击穿防护（互斥锁 + 逻辑过期）

**目标**：首页热点商品、爆款 SKU 在缓存失效瞬间不打穿到 DB。

**方案**

A. 互斥锁版本（普通商品用）：
```java
public R queryWithMutex(...) {
    // 1. 查缓存命中即返回
    // 2. 未命中 → tryLock("lock:product:{id}")
    // 3. 抢到锁 → 查 DB 回填；抢不到锁 → sleep(50ms) 递归重试
    // 4. finally unlock
}
```
锁实现用 `setnx ex` 或直接 Redisson `RLock`。

B. 逻辑过期版本（首页/秒杀活动用）：
- 缓存值结构 `{data: T, expireTime: long}`，永不真正过期。
- 查询时若 `expireTime < now`，开异步线程拿锁重建，**当前请求返回旧值**。
- 配合 P1-5 预热。

在 `CacheClient` 中提供两个方法：`queryWithMutex` / `queryWithLogicalExpire`。

**验收**：删 Redis key 后 100 并发请求，DB 查询次数 = 1。

---

### P1-4. 缓存雪崩（随机 TTL + 多级降级）

**方案**
1. CacheConfig 中 TTL 加随机抖动（在 cacheManager 之上包一层装饰，每次 put 时 TTL ±20%）。
2. 关键路径（商品详情、店铺状态）做本地 Caffeine + Redis 二级缓存：
   - 引入 `caffeine`，`@Cacheable` 走 Caffeine（TTL 30s），Caffeine miss 再查 Redis。
3. Redis 全挂时降级直查 DB（CacheClient 内 try/catch RedisConnectionFailureException）。

**验收**：手动 `redis-cli flushall` 后系统仍可服务，QPS 下降但不雪崩。

---

### P1-5. 首页/活动数据预热

**方案**：新增 `common/scheduler/CacheWarmupTask.java`：
- 应用启动后 `@PostConstruct` + 每天凌晨 2 点定时刷新：
  - 营业中商品分类树
  - 首页 banner、推荐位
  - 进行中的拼团/促销活动
- 使用逻辑过期写入。

---

### P1-6. 商品销量榜单（SortedSet）

**现状**：`syncProductFields` 把 sales 冗余到 product 表，热销榜实时性低且写放大。

**方案**：引入 Redis ZSet：
- key = `product:rank:sales:daily:{yyyyMMdd}`，member = productId，score = 当日销量
- 下单成功事件中 `zincrby`
- 排行榜接口直接 `zrevrange 0 19 withscores`
- 每日 TTL 7 天，定时归档到 DB

**验收**：商家端「今日热销 Top10」接口 < 5ms。

---

### P1-7. 商品 UV 统计（HyperLogLog）

**方案**：商品详情接口埋点 `pfadd uv:product:{id}:{yyyyMMdd} {uid或ip}`；统计接口 `pfcount`。误差 < 1%，单 key 仅 12KB。

---

### P1-8. 自提点附近排序（GEO）

**现状**：自提点列表估计是按 createdAt 排序。

**方案**：
- 后台新增/编辑自提点时 `geoadd geo:pickup {lng} {lat} {pickupId}`
- 用户接口传当前坐标：`georadius geo:pickup {lng} {lat} 50 km WITHCOORD WITHDIST ASC`
- 你前端已有腾讯地图 API 接坐标，直接对接。

**验收**：返回结果按距离从近到远，含 distance 字段。

---

## P2 - 工程化改造

### P2-1. AOP AutoFill 扩展为 createBy / updateBy

**现状**：`MyBatisPlusConfig.java:14` 已实现 `createdAt/updatedAt` 自动填，但 `created_by/updated_by` 没填。

**方案**：
1. 实体增加字段：
   ```java
   @TableField(fill = FieldFill.INSERT)        private Long createdBy;
   @TableField(fill = FieldFill.INSERT_UPDATE) private Long updatedBy;
   ```
2. `MyBatisPlusConfig` 扩展：
   ```java
   public void insertFill(MetaObject mo) {
       strictInsertFill(mo, "createdAt", LocalDateTime.class, LocalDateTime.now());
       strictInsertFill(mo, "updatedAt", LocalDateTime.class, LocalDateTime.now());
       Long opId = LoginContext.sid() != null ? LoginContext.sid() : LoginContext.uid();
       if (opId != null) {
           strictInsertFill(mo, "createdBy", Long.class, opId);
           strictInsertFill(mo, "updatedBy", Long.class, opId);
       }
   }
   ```
3. DDL 迁移脚本放 `database/migration/V20260505__add_audit_columns.sql`。

---

### P2-2. ThreadLocal 在 @Async / 子线程的传递

**现状**：`LoginContext` 用普通 ThreadLocal。后续若引入异步消费（P0-2）会丢用户上下文。

**方案**：替换为 `TransmittableThreadLocal`（阿里 TTL 库）：
```xml
<dependency>
  <groupId>com.alibaba</groupId>
  <artifactId>transmittable-thread-local</artifactId>
  <version>2.14.5</version>
</dependency>
```
```java
private static final ThreadLocal<LoginUser> CTX = new TransmittableThreadLocal<>();
```
配合 `TtlExecutors.getTtlExecutor(...)` 包装线程池。

---

### P2-3. PageHelper 风格分页封装

**现状**：每个 service 都 `new Page<>(qry.getPage(), qry.getSize())` 重复。

**方案**：在 `PageQry` 加默认值（page=1,size=10）+ 工具方法：
```java
public abstract class PageQry {
    private Integer page = 1;
    private Integer size = 10;
    public <T> Page<T> toPage() { return new Page<>(page, size); }
}
public class PageVO<T> {
    public static <S, T> PageVO<T> of(IPage<S> page, Function<S, T> mapper) {
        return new PageVO<>(page.getTotal(),
            page.getRecords().stream().map(mapper).toList(),
            (int) page.getCurrent(), (int) page.getSize());
    }
}
```
所有 service 改成：
```java
Page<Order> p = orderMapper.selectPage(qry.toPage(), wrapper);
return PageVO.of(p, this::toOrderVO);
```

---

### P2-4. 分布式锁组件（Redisson）

**目标**：替换 P0-1 的乐观锁补丁、领券、秒杀场景中的并发控制。

**方案**：
1. `pom.xml` 加 `redisson-spring-boot-starter`。
2. 配置 `RedissonConfig` 复用现有 Redis 连接信息。
3. 封装注解 `@DistributedLock(key="'groupbuy:'+#instanceId", waitTime=3, leaseTime=10)` + AOP，使用方式同 `@RateLimit`。
4. 业务侧调用：
   ```java
   @DistributedLock(key="'coupon:receive:'+#couponId+':'+#uid")
   public void receive(Long uid, Long couponId) { ... }
   ```

---

### P2-5. 业务事件解耦（Spring Events → 后续 Stream）

**现状**：`OrderServiceImpl.submit` 串行做：扣库存、清购物车、发 WS、刷销量。耦合重，失败处理难。

**方案**：
1. 定义 `OrderCreatedEvent(orderId, uid, items)`，submit 完成后 `applicationEventPublisher.publishEvent(...)`。
2. 多个 `@TransactionalEventListener(phase=AFTER_COMMIT)`：
   - `WsNotificationListener`：推商家端
   - `ProductSyncListener`：去重后批量刷 product 销量
   - `RankZsetListener`：刷热销 zset
3. 异步执行用 `@Async("orderEventExecutor")`，配合 P2-2 TTL 线程池。
4. 后续可平滑切到 Redis Stream / RabbitMQ。

**验收**：submit 主链路耗时下降；某个 Listener 异常不影响主单。

---

### P2-6. EasyExcel 报表抽取

**现状**：`module/order/controller/OrderExportController.java` 与 `module/report` 各自实现导出。

**方案**：
1. 新增 `common/excel/ExcelExportTemplate.java`：泛型方法 `<T> void export(HttpServletResponse, String fileName, Class<T>, List<T> data)`。
2. 统一处理 response header、文件名编码、`finally response.flushBuffer()`。
3. 业务 controller 只负责查数据 + 转 VO。

---

### P2-7. 全局接口签名/防重放（管理端关键操作）

**方案**：
- 新注解 `@Idempotent(key="'order:cancel:'+#orderNo")`，AOP 用 Redis `setnx` + TTL 10s 实现幂等。
- 应用于：取消订单、接单、拒单、退款审核。

---

### P2-8. 前端 token 续期 + 黑名单

**现状**：JWT 一次签发后无法主动失效。

**方案**：
1. 登录后把 jti 存 Redis `jwt:active:{uid}`，TTL = JWT 过期时间。
2. `LoginInterceptor` 增加 `redis.exists(...)` 校验，未命中视为已登出。
3. 退出登录接口 `del jwt:active:{uid}`。
4. 「踢人下线」管理端功能直接删 key。

---

### P2-9. 资源路径与配置外置

**现状**：`WebMvcConfig.java:37` 写死 `file:D:/xianguoji/upload/`。

**方案**：
- `application.yml` 增 `xgj.upload.local-path: ${XGJ_UPLOAD_PATH:./upload}`
- `OssProperties` / `WebMvcConfig` 注入读取。

---

### P2-10. 链路追踪 + 慢日志

**方案**：
1. 加 `spring-boot-starter-actuator` + `micrometer-tracing-bridge-otel`（可选）。
2. 写 `common/aspect/SlowLogAspect.java`：拦截 `@RestController`，> 500ms 打 warn。
3. MyBatis 慢 SQL：`mybatis-plus.global-config.banner=false` + `IllegalSQLInterceptor`（可选）+ p6spy。

---

## 实施分期

| 阶段 | 内容 | 预估工作量（AI 实现） |
|---|---|---|
| Sprint 1 | P0-1 ~ P0-5 | 0.5d |
| Sprint 2 | P1-1 ~ P1-2（Spring Cache + 穿透防护） | 0.5d |
| Sprint 3 | P1-3 ~ P1-5（击穿/雪崩/预热） | 1d |
| Sprint 4 | P1-6 ~ P1-8（ZSet/HLL/GEO） | 0.5d |
| Sprint 5 | P2-1 ~ P2-4（AutoFill/TTL/分页/Redisson） | 0.5d |
| Sprint 6 | P2-5 ~ P2-10（事件/Excel/幂等/JWT/配置/可观测） | 1d |

## 通用约束

1. 所有 Redis key 加业务前缀 `xgj:{module}:{biz}:{id}`，禁止裸字符串。
2. Lua 脚本统一放 `resources/lua/*.lua`，`@PostConstruct` 加载为 `DefaultRedisScript<T>` Bean。
3. 新增公共注解一律放 `common/annotation/`，AOP 切面放 `common/aspect/`。
4. 每个改动须附最小 JUnit / 集成测试（在 `src/test/java`）。
5. DB 变更必须出现在 `database/migration/` 目录，命名 `V{yyyyMMdd}__{desc}.sql`。
6. 不要破坏现有 API 形状（`R<T>` 结构、`PageVO<T>` 字段名）。

## 风险与回滚

- 缓存改造默认全部加开关 `xgj.cache.enabled`，默认 true，出问题可一键关。
- Redis 预扣库存改造保留 DB 路径，应用启动加配置 `xgj.stock.use-redis: false` 可关闭。
- 所有定时任务带 `@ConditionalOnProperty`，可灰度。

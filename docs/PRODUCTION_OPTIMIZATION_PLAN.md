# 鲜果记 生产发布前 优化与安全加固方案（v1.0）

> 面向自动化实现 AI 的可执行清单。
> 安全部分参考姊妹项目 `blog_forum`（已落地的 XSS / SQL 注入 / 热点限流 / IP 风控 / SSRF / 验证码等）作为蓝本，结合本项目当前架构（Spring Boot 3.2.5 + 拦截器式鉴权 + MyBatis-Plus + Redis + Redisson + 微信小程序前端）做差异化落地。
>
> 阅读顺序即推荐实现顺序：**S 安全攻击防御** → **P0 上线必修** → **P1 稳定性/性能** → **P2 工程化** → **F 前端** → **O 运维**。
> 所有新增公共组件统一放到 `xianguoji-server/src/main/java/com/xianguoji/server/common/` 对应子包；不要新建顶层包。
>
> 每条任务格式：**现状定位 / 目标 / 方案（含代码骨架与文件路径）/ 验收**。

---

## 目录

- [S. 安全攻击防御（重点）](#s-安全攻击防御重点)
  - [S-1. XSS 过滤器 + 富文本白名单](#s-1-xss-过滤器--富文本白名单)
  - [S-2. SQL 注入纵深防御](#s-2-sql-注入纵深防御)
  - [S-3. 热点路径分级限流 + IP 风控](#s-3-热点路径分级限流--ip-风控)
  - [S-4. 暴力破解 / 撞库防御（登录、验证码、短信）](#s-4-暴力破解--撞库防御登录验证码短信)
  - [S-5. SSRF 防御（OSS 回源、微信回调、外链解析）](#s-5-ssrf-防御oss-回源微信回调外链解析)
  - [S-6. CSRF 防御（管理后台 Cookie 场景）](#s-6-csrf-防御管理后台-cookie-场景)
  - [S-7. JWT 加固（KID 轮换、绑定指纹、刷新分离）](#s-7-jwt-加固kid-轮换绑定指纹刷新分离)
  - [S-8. CORS 白名单收敛](#s-8-cors-白名单收敛)
  - [S-9. 安全响应头 + HTTPS 强制](#s-9-安全响应头--https-强制)
  - [S-10. 文件上传深度校验](#s-10-文件上传深度校验)
  - [S-11. Druid / Swagger / Actuator 暴露面收敛](#s-11-druid--swagger--actuator-暴露面收敛)
  - [S-12. 越权防护（IDOR / 水平垂直越权）](#s-12-越权防护idor--水平垂直越权)
  - [S-13. 越权重放 / 幂等 / 重发攻击](#s-13-越权重放--幂等--重发攻击)
  - [S-14. 敏感日志脱敏](#s-14-敏感日志脱敏)
  - [S-15. 请求体大小 / 慢连接 / 反爬虫](#s-15-请求体大小--慢连接--反爬虫)
  - [S-16. 依赖与镜像漏洞扫描](#s-16-依赖与镜像漏洞扫描)
  - [S-17. 安全事件告警](#s-17-安全事件告警)
- [P0. 上线必修（数据正确性 / 配置）](#p0-上线必修数据正确性--配置)
- [P1. 稳定性 / 性能](#p1-稳定性--性能)
- [P2. 工程化](#p2-工程化)
- [F. 前端（用户端 / 商家端）](#f-前端用户端--商家端)
- [O. 运维 / 部署](#o-运维--部署)
- [附录 A：production profile 模板](#附录-aproduction-profile-模板)
- [附录 B：所需新增依赖](#附录-b所需新增依赖)
- [附录 C：实现优先级矩阵](#附录-c实现优先级矩阵)

---

## S. 安全攻击防御（重点）

> 当前 `xianguoji-server` 的 web 防护栈仅有：`TraceFilter` + `LoginInterceptor`(JWT) + `@RateLimit` 切面（仅按 IP+key 简单计数）+ `JwtBlacklistManager`。**未覆盖** XSS、SQL 注入、热点路径限流、IP 黑名单、SSRF、CSRF、安全响应头、上传深检、敏感日志脱敏。本节按攻击面逐项落地，借鉴 `blog_forum/blog-backend/src/main/java/com/blog/{filter,aspect,validator,util,config}` 的实现。

---

### S-1. XSS 过滤器 + 富文本白名单

**现状定位**
- 项目对所有用户输入（昵称、地址 detail、评论 content、商品描述、客服反馈）**未做 XSS 转义/白名单**；前端用 `wot-design-uni`/H5 渲染时若直出 `v-html` 即触发。
- 项目已引入 `org.jsoup:jsoup:1.17.2`（见 `pom.xml:163`），但**未使用**。

**目标**
- 普通文本字段：HTML 实体编码（默认）。
- 富文本字段（商品描述 `goods_detail`、公告 `announcement_content`）：用 Jsoup `Safelist.relaxed()` 白名单清洗，剥离 `<script>` / 事件属性 / `javascript:` 协议。

**方案**
1. 新建 `common/security/XssHttpServletRequestWrapper.java`（参考 `blog-backend/.../filter/XssHttpServletRequestWrapper.java`）：
   - 重写 `getParameter / getParameterValues / getHeader`：对 String 调用 `cleanXss()`（默认实体编码 `& < > " ' /`）。
   - 重写 `getInputStream / getReader`：对 `application/json` 请求体递归清洗 Map/List 中的 String 值；非 JSON 直接转义。
   - 缓存 body 为 `byte[]`，避免下游再次读取流时为空。
2. 新建 `common/security/XssFilter.java`（`OncePerRequestFilter`，`@Order(Ordered.HIGHEST_PRECEDENCE + 10)`，紧跟 `TraceFilter` 之后）：
   - 路径排除：`/api/pub/captcha`、`/api/admin/upload`、`/api/u/upload`、`/swagger-ui/**`、`/v3/api-docs/**`、`/actuator/**`、`/static/**`、`/ws/**`。
   - 内容排除：`Content-Type` 为 `multipart/form-data`、`image/*` 时直接放行（不读 body）。
3. 新建 `common/security/RichTextSanitizer.java`：
   ```java
   public final class RichTextSanitizer {
       private static final Safelist SAFELIST = Safelist.relaxed()
           .addAttributes(":all", "style", "class")
           .removeProtocols("a", "href", "ftp", "file")
           .removeProtocols("img", "src", "ftp", "file");
       public static String clean(String html) {
           if (html == null) return null;
           return Jsoup.clean(html, "", SAFELIST,
               new Document.OutputSettings().prettyPrint(false));
       }
   }
   ```
   - 在富文本字段的 `Service` 入口（如 `GoodsServiceImpl.create/update`、`AnnouncementServiceImpl.publish`）显式调用 `RichTextSanitizer.clean(detail)`，**不要**让 XssFilter 双重转义破坏富文本结构（XssFilter 对这类路径走排除）。
4. 关键路径排除策略：在 `XssFilter` 中读取 `xianguoji.security.xss.rich-text-paths` 配置（如 `/api/admin/goods/**,/api/admin/announcement/**`），命中时跳过 body 转义，仅保留 header/param 转义。

**验收**
- POST `/api/u/review` body `{"content":"<script>alert(1)</script>"}` → DB 中存为 `&lt;script&gt;alert(1)&lt;/script&gt;`。
- POST `/api/admin/goods` 的 `detail` 字段含 `<img src=x onerror=alert(1)>` → DB 中 `onerror` 被剥离，`<img>` 保留。
- 单元测试 `XssFilterTest` 至少覆盖：纯文本 / JSON 嵌套对象 / Form 表单 / 富文本路径。

---

### S-2. SQL 注入纵深防御

**现状定位**
- 项目使用 MyBatis-Plus 参数绑定，常规查询安全；但**排序字段、动态表名、`like` 拼接、`${}` 占位**仍是风险点。需要：
  - 全局 `Mapper` 检查（grep `\${`）；
  - Controller 层对原始查询字符串（`keyword / orderBy / sort`）做白名单/正则校验。

**目标**
- 杜绝 `${}` 出现在用户输入路径上；对所有 `keyword/sort` 类参数做注入特征拦截。

**方案**
1. 新建 `common/validator/SqlInjectionValidator.java`（搬运 `blog-backend/.../validator/SqlInjectionValidator.java`），含：
   - `containsSqlInjection(String)`：正则匹配 `\b(SELECT|INSERT|...|UNION|DECLARE|EXEC)\b`、`--`、`/*`、`;DROP`、`OR 1=1` 等。
   - `cleanLikeWildcard(String)`：转义 `%` `_` `\`，避免 like 风暴。
2. 新建 `common/aspect/SqlInjectionAspect.java`：
   - 切点：`execution(* com.xianguoji.server.module..controller..*.*(..))` 且方法名 `get*/list*/page*/search*/query*`；
   - 遍历 `String` 参数（包含 DTO 内的 String 字段，递归一层）执行 `containsSqlInjection` 校验，命中抛 `BizException(PARAM_ERROR, "参数包含非法字符")` 并 `log.warn("[SQLI] uri={} ip={} value={}")`。
3. 全仓 grep `\${` 在 `*.xml` 中的出现：
   ```bash
   grep -rn "\${" xianguoji-server/src/main/resources/mapper/
   ```
   - 仅允许出现在 ORDER BY / 表名场景，且其值必须经过 **白名单枚举**（在 Service 层用 `Set<String>` 校验后再下传）。
4. 排序字段白名单：在 `module/*/dto/*PageQuery.java` 的 `sortBy` setter 中校验 `Set.of("createTime","price","sales")`，否则抛参数错误。

**验收**
- GET `/api/admin/order/page?keyword=' OR 1=1--` 返回 `{code:400}`，日志输出 `[SQLI]`。
- 全仓搜索 `\${` 在 mapper xml 中出现的位置都有白名单单测（`*MapperTest`）。

---

### S-3. 热点路径分级限流 + IP 风控

**现状定位**
- 仅有方法级 `@RateLimit`（`common/aspect/RateLimitAspect.java:37` 用 `request.getRemoteAddr()`，**不识别反向代理 X-Real-IP / X-Forwarded-For**，在 Nginx 后会失效）；没有针对网关层的全局热点限流，难抵 CC/DDoS。
- 没有 IP 黑名单 / 威胁等级机制。

**目标**
- 反向代理友好的 IP 提取；
- 全局按路径前缀的滑动窗口限流（窗口/阈值可配）；
- 触发即升级威胁等级，三级即封禁 24h（写入 Redis）。

**方案**
1. 新建 `common/util/IpUtil.java`：
   ```java
   public static String getClientIp(HttpServletRequest req) {
       for (String h : List.of("X-Forwarded-For","X-Real-IP","Proxy-Client-IP","WL-Proxy-Client-IP")) {
           String v = req.getHeader(h);
           if (v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v)) {
               return v.split(",")[0].trim();
           }
       }
       return req.getRemoteAddr();
   }
   ```
   - 把 `RateLimitAspect:37` 改为 `IpUtil.getClientIp(request)`。
2. 新建 `common/service/SecurityEventService.java`：封装 `incrementWithTtl(key, sec)` / `getThreatLevel(ip)` / `setThreatLevel(ip, level, ttlSec)` / `banIp(ip, reason, Duration)`，全部走 Redis（key 前缀 `xgj:sec:`）。
3. 新建 `common/security/HotspotRateLimitFilter.java`（参考 `blog-backend/.../filter/HotspotRateLimitFilter.java`）：
   - 路径规则（10 秒窗口）：
     | 前缀 | 阈值 |
     | --- | --- |
     | `/api/auth` | 30 |
     | `/api/u/order` | 60 |
     | `/api/u/cart` | 80 |
     | `/api/admin` | 30 |
     | `/api`（兜底） | 300 |
   - 三级威胁机制：1 级仅返回 429；2 级返回告警提示；3 级 `banIp(ip, "hotspot", 24h)`。
   - 白名单 IP 通过 `xianguoji.security.hotspot.whitelist` 配置（运维公网出口 / 健康检查机器）。
4. 新建 `common/security/IpBanFilter.java`：在过滤链最前（`@Order(Ordered.HIGHEST_PRECEDENCE + 5)`，**TraceFilter 之后、Xss 之前**）检查 Redis `xgj:sec:ban:{ip}`，命中直接返回 403 + `{"code":403,"msg":"您的访问已被限制"}`。

**验收**
- 用同一 IP 1 秒内连续打 `/api/auth/sms-code` 50 次 → 第 31 次起返回 429；继续打到 90 次 → IP 被写入 `xgj:sec:ban:`，再访问任意接口直接 403。
- Nginx `proxy_set_header X-Real-IP $remote_addr;` 配置后，日志中 `IpUtil.getClientIp` 输出正确公网 IP。

---

### S-4. 暴力破解 / 撞库防御（登录、验证码、短信）

**现状定位**
- `module/auth/controller/CaptchaController.java`：图形验证码已具备（5 分钟 TTL，`captcha:{key}`），但**未在登录/注册/找回密码接口强制校验**。
- 短信验证码：`SmsUtil.java` 是 mock；上线前必须接真实通道并加发送频控。
- 登录失败次数无锁定。

**目标**
- 密码登录：失败 5 次 → 账户/IP 锁定 15min；强制图形验证码。
- 短信验证码：同手机号 60s/次、同 IP 1min/5 次、同手机号 1 天 10 次。
- 登录成功后**重置失败计数**。

**方案**
1. 在 `AuthServiceImpl.passwordLogin / smsLogin / wxLogin` 入口先校验 `captchaKey`+`captchaCode`；校验通过即 `DEL captcha:{key}`（一次性）。
2. 新建 `common/security/LoginAttemptManager.java`：
   - `recordFail(account, ip)`：`INCR fail:{account}` + `INCR fail:{ip}`，TTL 15min；任一 ≥ 5 抛 `LOGIN_LOCKED`。
   - `reset(account, ip)`：登录成功调用。
3. 短信频控（在 `SmsUtil.send` 之前，业务层调用）：
   ```
   sms:phone:{phone}:60s   ≤ 1
   sms:phone:{phone}:day   ≤ 10
   sms:ip:{ip}:60s         ≤ 5
   ```
   超出抛 `BizException(SMS_TOO_FREQUENT)`。
4. 短信内容：`【鲜果记】您的验证码是 ${code}，5 分钟内有效，请勿泄露给他人。` 不含可点击链接。

**验收**
- 同账号连续 5 次错密码 → 第 6 次返回 `账户已锁定，请稍后再试`，Redis 中存在 `xgj:auth:lock:{account}` 且 TTL ≈ 900s。
- 同手机号 60 秒内第二次请求 `/api/pub/sms-code` → 返回 `验证码发送过于频繁`。

---

### S-5. SSRF 防御（OSS 回源、微信回调、外链解析）

**现状定位**
- `OssService.upload` 走的是 SDK，无 SSRF 风险；但若后续有"按 URL 拉取图片入库"、"小程序消息模板回调外部 URL"等接口必须防 SSRF。
- 微信支付/登录的回调 URL 来自配置可控，但仍需校验签名。

**目标**
- 任何由用户提供 URL 触发后端 HTTP 请求的接口，都必须先校验：
  1. scheme 仅 `http/https`；
  2. host 解析后**不在内网网段**（10/8、172.16/12、192.168/16、127/8、169.254/16、IPv6 fc00::/7、`localhost`、`*.local`、`*.internal`）；
  3. 不允许 `userinfo` 段；
  4. 端口 ≤ 65535，禁用 22/25/3306/6379/9200 等敏感端口。

**方案**
1. 搬运 `blog-backend/.../util/UrlSecurityUtil.java` → `xianguoji-server/.../common/util/UrlSecurityUtil.java`。
2. 全仓搜索后端发起 HTTP 调用的位置（`OkHttp / RestTemplate / HttpURLConnection / Jsoup.connect`），统一在调用前 `UrlSecurityUtil.validatePublicHttpUrl(url)`；当前需修改的位置：
   - `common/util/WechatUtil.java`：仅允许 `https://api.weixin.qq.com` 前缀。
   - 任何后续添加的"拉取外链头像/图片"接口。
3. 微信回调验签：在 `module/order/controller/WechatPayCallbackController` 中校验 `Wechatpay-Signature`，不通过返回 401。

**验收**
- POST `/api/admin/import?url=http://169.254.169.254/latest/meta-data/` → 400 `Private or local network addresses are not allowed`。
- POST `/api/admin/import?url=file:///etc/passwd` → 400 `Only HTTP/HTTPS URLs are allowed`。

---

### S-6. CSRF 防御（管理后台 Cookie 场景）

**现状定位**
- 用户端是 JWT Bearer + 小程序，无 Cookie，**天然免疫 CSRF**。
- 商家端 `merchant-front` 若使用 Cookie 登录则需要 CSRF。当前商家端也用 JWT（同 `LoginInterceptor`），暂可豁免。

**方案**
- 维持 JWT 方案，**禁止**任何接口同时接受 `Cookie` 与 `Authorization`。在 `LoginInterceptor.extractToken` 仅读 `Authorization` 与 `token` header，**移除**任何对 Cookie 的兼容。
- 若未来接入后台管理 Web 需 Cookie：参考 `blog-backend/.../config/CsrfConfig.java`，使用 `CookieCsrfTokenRepository.withHttpOnlyFalse()` + 双提交 Cookie。

**验收**
- 后端代码全仓搜不到从 Cookie 取登录态的实现。

---

### S-7. JWT 加固（KID 轮换、绑定指纹、刷新分离）

**现状定位**
- `JwtUtil.java`：HMAC-SHA256，单密钥，无 `kid`，无刷新 token，TTL 用户端 168h（7 天）偏长，管理端 12h；密钥强度依赖 `JWT_SECRET` 环境变量长度。
- `JwtBlacklistManager` 用全 token 字符串作为 Redis key，非常长，建议改用 `jti`。

**目标**
- **JTI 替代全 token 作黑名单 key**，节省内存；
- **Access + Refresh 双 token**：access 短（用户端 2h、管理端 30min），refresh 长（7 天）单独走 `/api/auth/refresh`；
- JWT 中加入 `kid`（密钥 ID），支持密钥滚动；
- access token 绑定**设备指纹哈希**（小程序 openid 前缀 + UA 摘要），续签时核对，防 token 盗用横向移动。

**方案**
1. 改 `xianguoji.jwt.secret` 为 `secrets`（map 形式）：
   ```yaml
   xianguoji:
     jwt:
       active-kid: v2
       secrets:
         v1: ${JWT_SECRET_V1}
         v2: ${JWT_SECRET_V2}
   ```
   - `JwtUtil.key()` 改为读取 `kid → secret` 映射；签发用 `active-kid`；解析时根据 header `kid` 查找。
2. 签发增加 `jti = UUID.randomUUID()`、`fp = sha256(openid+ua).take(16)`：
   ```java
   String jti = UUID.randomUUID().toString();
   Jwts.builder().header().keyId(activeKid).and()
       .id(jti).claims(claims)...
   ```
3. `JwtBlacklistManager` 改为以 `jti` 为 key：`xgj:jwt:bl:{jti}`，TTL = remainMs。
4. 新增 `/api/auth/refresh`（不走 LoginInterceptor，但 RateLimit），用 refresh token 换发新 access；refresh token 旋转（每次换发同时下发新 refresh，旧 refresh 立即拉黑）。
5. TTL 调整：
   ```yaml
   xianguoji.jwt.user-access-ttl-minutes: 120
   xianguoji.jwt.user-refresh-ttl-hours: 168
   xianguoji.jwt.admin-access-ttl-minutes: 30
   xianguoji.jwt.admin-refresh-ttl-hours: 12
   ```

**验收**
- 解密任意签发的 access token 头部含 `"kid":"v2"`；payload 含 `jti`、`fp`、`type:"access"`。
- 调用 `/api/auth/logout` 后 → `xgj:jwt:bl:{jti}` 存在；再带原 token 访问 `/api/u/me` 返回 401。
- 把 `active-kid` 切到 v3 滚动密钥后，旧 v2 签发的 token 仍能正常解析直至过期。

---

### S-8. CORS 白名单收敛

**现状定位**
- `common/config/WebMvcConfig.java:31`：
  ```java
  .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
  ```
  生产环境必须删除 `localhost`，改为公网域名白名单。

**方案**
- `application-prod.yml` 增加 `xianguoji.security.cors.allowed-origins: https://m.xianguoji.com,https://admin.xianguoji.com`；
- `WebMvcConfig.addCorsMappings` 改为读取该配置 split；非 prod 才允许 `localhost`：
  ```java
  @Value("${xianguoji.security.cors.allowed-origins:}") String allowed;
  registry.addMapping("/**")
      .allowedOrigins(allowed.isBlank() ? new String[]{} : allowed.split(","))
      .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
      .exposedHeaders("X-Token-Renewal","X-Trace-Id")
      .allowCredentials(true).maxAge(3600);
  ```
- **不要使用 `*`** + `allowCredentials(true)`（浏览器拒绝）。

**验收**
- 生产环境 `curl -H "Origin: https://evil.com"` 不会返回 `Access-Control-Allow-Origin`。

---

### S-9. 安全响应头 + HTTPS 强制

**现状定位**：当前响应头无任何安全头。

**方案**：新建 `common/security/SecurityHeadersFilter.java`（`OncePerRequestFilter`，最高优先级），写入：
```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
Content-Security-Policy: default-src 'self'; img-src 'self' data: https://*.xianguoji.com https://*.aliyuncs.com; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self' https://api.xianguoji.com wss://api.xianguoji.com
Cache-Control: no-store   // 仅敏感接口
```
- HTTPS 强制：在 Nginx 层配置 301 跳转；后端 `server.forward-headers-strategy: framework` + `server.tomcat.remoteip.protocol-header: x-forwarded-proto`。

**验收**
- `curl -I https://api.xianguoji.com/api/pub/captcha` 包含上述全部头。

---

### S-10. 文件上传深度校验

**现状定位**
- `application.yml:11`：`max-file-size: 100MB`，太大；用户端头像/评价图通常 ≤ 5MB。
- `OssService.upload` 仅按上传方传入的 `ext / contentType` 拼名，**未校验真实 magic number**。

**方案**
1. `application.yml` → `max-file-size: 10MB`、`max-request-size: 10MB`；管理端商品图单独走 30MB 限制。
2. 在 `OssService.upload` 入口加：
   ```java
   private static final Set<String> ALLOWED_EXT = Set.of("jpg","jpeg","png","webp","gif");
   private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg","image/png","image/webp","image/gif");
   ```
   - 校验 `ext` 在白名单内；
   - 用 Hutool `FileTypeUtil.getType(in)` 或 `Tika`（如不引入则用前 8 字节 magic number 判断）匹配真实类型；
   - 校验文件大小 `file.getSize() <= maxBytes`；
   - 文件名一律用 `UUID.randomUUID()`，**绝不**用用户传入名（防路径穿越和 XSS）。
3. 关闭 `WebMvcConfig.addResourceHandlers` 的 `/static/**` 直接暴露：
   - 生产改用 OSS + CDN 签名 URL；如需本地静态服务，仅允许 `image/*` 通过 `MimeTypeFilter`，禁止 `.html .js .svg`（SVG 可执行 JS）。
4. EXIF 信息剥离：图片入库前用 `metadata-extractor` 或 `Thumbnailator` 处理，去除地理位置等敏感 EXIF。

**验收**
- 上传 1KB 的伪装 PNG（实际是 PHP）→ 后端拒绝，返回 `不支持的文件类型`。
- 上传 12MB 图片 → 413 / 自定义 `文件大小超出限制`。

---

### S-11. Druid / Swagger / Actuator 暴露面收敛

**现状定位**
- `application.yml:26`：`druid.stat-view-servlet.enabled: true`，凭据为占位符 `CHANGE_ME`，**禁止上线开启**。
- Knife4j `enable: true`、Swagger UI 暴露在 `/swagger-ui.html`。
- 项目未引入 actuator，但若引入务必收敛。

**方案**
1. `application-prod.yml` 显式覆盖：
   ```yaml
   spring:
     datasource:
       druid:
         stat-view-servlet:
           enabled: false
           allow: 127.0.0.1
   springdoc:
     api-docs:
       enabled: false
     swagger-ui:
       enabled: false
   knife4j:
     enable: false
     production: true
   ```
2. 若运维确实需要内网监控页：放在内网 ALB 后并强制 IP 白名单 + 双因子。
3. 若引入 `spring-boot-starter-actuator`：
   ```yaml
   management:
     endpoints.web.exposure.include: health,info
     endpoint.health.show-details: never
     server.port: 9090   # 与业务端口分离，仅内网放通
   ```

**验收**
- 生产 `curl https://api.xianguoji.com/druid/` → 404。
- `curl https://api.xianguoji.com/swagger-ui.html` → 404。

---

### S-12. 越权防护（IDOR / 水平垂直越权）

**现状定位**
- `LoginInterceptor` 仅做"是否登录/是否管理员"，**未做资源归属校验**。例如：用户端订单详情接口若仅按 `orderId` 查询，恶意用户递增 ID 即可看他人订单。

**目标**
- 所有"按主键查询/修改/删除"的用户端接口必须校验 `resource.userId == LoginContext.getUid()`；
- 管理端按门店 / 角色范围过滤，例如"店员"只能看自己门店订单。

**方案**
1. 自定义 `@OwnedBy` 注解：
   ```java
   @Target(METHOD) @Retention(RUNTIME)
   public @interface OwnedBy {
       /** 资源所属字段，如 "userId" */
       String field() default "userId";
       /** 资源类型，用于反射加载 service 查询主体 */
       Class<?> entity();
       /** 主键参数名，默认从第一个 path variable 取 */
       String idParam() default "id";
   }
   ```
   - 切面 `OwnershipAspect`：方法执行前根据 `entity()` + `idParam()` 查 `userId/staffId/shopId`，与 `LoginContext` 比对，不一致抛 `ACCESS_DENIED`。
2. 优先在以下 Controller 加注解：
   - `OrderController.detail / cancel / refund`
   - `AddressController.update / delete`
   - `CartController.delete`
   - `ReviewController.update / delete`
   - 商家端 `ShopController.update / staff/*`（按 `shopId` 归属）
3. 横向越权回归测试：登录 A，请求 B 的订单 ID → 403。

**验收**
- 单元测试 `OwnershipAspectTest` 覆盖：用户访问他人订单、店员访问他店订单 → `ACCESS_DENIED`。

---

### S-13. 越权重放 / 幂等 / 重发攻击

**现状定位**
- 已有 `@Idempotent` 注解（`common/annotation/Idempotent.java`）+ `IdempotentAspect`，需确认覆盖：下单、支付回调、退款、领券、拼团、签到。
- 微信支付回调若被重放需校验 `out_trade_no` 状态机，防止重复入账。

**方案**
1. 全仓 grep `@PostMapping` 找写操作，列出仍未加 `@Idempotent` 的接口，逐一补齐 `key="${order.id}:${uid}"` SpEL。
2. 微信回调 `WechatPayCallbackService.handle`：
   ```java
   if (order.getStatus() != PAID_PENDING) return SUCCESS;  // 幂等
   ```
   配合 `Redisson` 分布式锁 `lock:order:pay:{orderNo}`。
3. `Date now` 和 `nonceStr` 校验：拒绝偏移 > 5 分钟的回调（防重放）。

**验收**
- 同一 `outTradeNo` 重发 5 次微信回调 → 仅一次入账；订单状态机不退化。

---

### S-14. 敏感日志脱敏

**现状定位**
- `application.yml:75`：`logging.level.com.xianguoji: debug`；
- 生产 `application-prod.yml:11` 已改为 `info`，但请求日志、Druid sql 日志、异常堆栈中仍可能输出手机号 / 地址 / token。

**方案**
1. 自定义 Logback `MaskingPatternLayout`：
   - 手机号：`(1[3-9])\d{4}(\d{4})` → `$1****$2`
   - 身份证：保留前 6 位末 4 位
   - token: `Authorization=Bearer\s+\S+` → `Bearer ***`
   - 收件地址：`detail` 字段日志直接剔除。
2. 改 `logback-spring.xml` 引用该 layout；保留 `traceId`。
3. `GlobalExceptionHandler.handleException` 输出错误时 **不要** 把 request body 直接打日志，仅打 method + uri + traceId + 异常类。

**验收**
- 任一请求日志 grep `1[3-9]\d{9}` 无完整命中。

---

### S-15. 请求体大小 / 慢连接 / 反爬虫

**方案**
- Nginx：`client_max_body_size 10m; client_body_timeout 10s; client_header_timeout 10s;` 防慢连接 DoS。
- 后端 `server.tomcat.connection-timeout: 10s`、`server.tomcat.max-swallow-size: 10MB`。
- robots.txt：根路径返回 `User-agent: *\nDisallow: /api/`。
- 检测 `User-Agent` 为空或 `python-requests/curl/wget` 时（非 health check 路径）记录但不立即封禁，累计阈值后联动 `IpBanFilter`。

---

### S-16. 依赖与镜像漏洞扫描

**方案**
1. `pom.xml` 增加 OWASP Dependency-Check 插件（CI 中执行，失败阈值 CVSS≥7）：
   ```xml
   <plugin>
     <groupId>org.owasp</groupId>
     <artifactId>dependency-check-maven</artifactId>
     <version>9.2.0</version>
     <configuration><failBuildOnCVSS>7</failBuildOnCVSS></configuration>
   </plugin>
   ```
2. 重点检查的依赖：
   - `jjwt:0.12.5`（CVE 状态确认）
   - `hutool-all:5.8.27`（注意只在服务端用，**禁止**用 `cn.hutool.core.util.ReflectUtil.invoke` 之类对外开放）
   - `mysql-connector-j`（必要时升级）
   - `aliyun-sdk-oss`（含旧 jaxb，用 `<exclusions>` 排掉冲突）
3. Docker 镜像基础层用 `eclipse-temurin:17-jre-alpine`，CI 跑 `trivy image`。

---

### S-17. 安全事件告警

**方案**
- `SecurityEventService.log(eventType, ip, detail)` 写 Redis Stream `xgj:sec:events`；
- 后台 `SecurityEventListener` 消费，触发以下规则即推送钉钉/飞书 webhook：
  - 单 IP 5 分钟 > 50 次 401/403；
  - 任意 IP 命中 `[SQLI]` / `[XSS]`；
  - JWT 黑名单命中（疑似盗用）；
  - 订单金额异常（参考 `module/report` 风控）；
- webhook URL 走 `xianguoji.security.alert.webhook`（生产）。

---

## P0. 上线必修（数据正确性 / 配置）

- [ ] **P0-1 配置外置化**：`application-prod.yml:5`、`application.yml:21,29,35,52,68,69` 全部改为强制读取环境变量，无 `CHANGE_ME` 默认值；启动时若关键 secret 缺失，`DotenvLoader` 抛 `IllegalStateException` 阻止启动。
- [ ] **P0-2 关闭 dev profile**：启动命令 `--spring.profiles.active=prod`，移除 `application.yml:8` 的 `active: dev`。
- [ ] **P0-3 数据库密码强度**：≥ 16 位混合；MySQL 用户 **禁用** `GRANT ALL`，仅授予 `xianguoji` 库 `SELECT,INSERT,UPDATE,DELETE,EXECUTE`；DDL 走运维。
- [ ] **P0-4 Redis 鉴权**：`requirepass` 必填，`bind 127.0.0.1`，**禁用** `FLUSHALL/FLUSHDB/CONFIG`（`rename-command`）。
- [ ] **P0-5 业务并发安全回归**：参照原有 `OPTIMIZATION_PLAN.md` 的 P0-1 ~ P0-N（拼团 currentSize、库存扣减、订单状态机、抢券原子性）逐项测试已落地后再发布。
- [ ] **P0-6 时间一致性**：服务器/MySQL/Redis 全部 `Asia/Shanghai`，开启 NTP，避免 JWT exp 漂移。
- [ ] **P0-7 数据库备份策略**：每日全量 + binlog 增量，保留 14 天；演练一次 `mysqldump → restore`。

---

## P1. 稳定性 / 性能

- [ ] **P1-1 Druid 监控 SQL 慢日志**：`spring.datasource.druid.filter.stat.log-slow-sql: true`，`slow-sql-millis: 1000`，配合 `TraceFilter` 的 `traceId`。
- [ ] **P1-2 连接池容量**：`max-active: 50`（按机器核数 *2），`min-idle: 10`，`validation-query: SELECT 1`。
- [ ] **P1-3 Redis 连接池 / 哨兵**：生产换 lettuce 连接池配置 + 哨兵或集群；现有 `RedissonConfig.java` 切到 `redisson-clusterServersConfig`。
- [ ] **P1-4 缓存穿透 / 雪崩**：`CacheConfig` 中所有商品/分类/门店缓存增加：
  - 空值缓存 60s（防穿透）；
  - TTL 加 `± 60s` 抖动（防雪崩）；
  - 热门商品用本地 Caffeine + 远程 Redis 二级缓存（项目已引入 caffeine）。
- [ ] **P1-5 数据库索引复盘**：以 `database/*.sql` 为准，逐表确认：`order(user_id, status, create_time)`、`goods(category_id, status)`、`address(user_id)`、`review(goods_id, status)` 复合索引齐全。
- [ ] **P1-6 慢接口 P95**：`TraceFilter` 已有慢日志（`SLOW_THRESHOLD_MS=1000`），加阈值告警接 `SecurityEventService`。
- [ ] **P1-7 异步执行**：`@Async` 配合自定义 `ThreadPoolTaskExecutor`（核心 8 / 最大 32 / 队列 200），`TransmittableThreadLocal` 已引入，确保 `LoginContext` 在异步任务中可见。
- [ ] **P1-8 微信支付幂等 + 对账**：每日定时任务对未确认订单回查 `/v3/pay/transactions/out-trade-no/{outTradeNo}` 补单。
- [ ] **P1-9 库存超卖**：扣减必须用 Redis Lua（项目 `resources/lua/` 已有），下单同 `Redisson` 锁 + 数据库唯一约束兜底。
- [ ] **P1-10 短链 / 预热**：首页接口接入 Caffeine 5min 缓存；分类首屏接口预热脚本。

---

## P2. 工程化

- [ ] **P2-1 全局异常细化**：在 `GlobalExceptionHandler` 增加 `MissingServletRequestParameterException / HttpRequestMethodNotSupportedException / MaxUploadSizeExceededException` 等，避免泄露 5xx 堆栈。
- [ ] **P2-2 统一 ResultCode**：参考 `blog-backend/.../exception/ErrorCode.java` 把 4010/4011/4030 等错误码补齐到 `common/result/ResultCode.java`，`R.fail` 序列化稳定。
- [ ] **P2-3 接口契约**：把 `docs/backend-spec.md` 与 swagger 注解对齐，引入 `@Schema` 描述。`prod` 关 swagger 后，文档导出为静态 markdown 入仓。
- [ ] **P2-4 单元测试**：核心路径补充 80% 覆盖率（订单 / 库存 / 支付回调 / 鉴权）；新增的 XSS/SQLi/限流 filter 必加测试。
- [ ] **P2-5 CI/CD**：GitHub Actions / Drone：lint → test → dependency-check → build → 推镜像 → 发布。
- [ ] **P2-6 配置中心**：可选 Nacos，否则用 K8s Secret/configmap，**严禁**把 `.env` 提交到 git。
- [ ] **P2-7 logback 滚动**：每日 + 100MB 滚动，保留 30 天压缩，单独输出 `security.log`、`slow.log`、`business.log`。

---

## F. 前端（用户端 / 商家端）

- [ ] **F-1 BASE_URL 切换**：`user-front/src/api/request.ts:3` 当前默认 `http://127.0.0.1:8080`，发布前用 `vite` 环境变量分离 `.env.production`：`VITE_API_BASE=https://api.xianguoji.com`，且小程序 `manifest.json` 把该域名加入 `request合法域名`。
- [ ] **F-2 强制 HTTPS / WSS**：小程序后台业务域名必须备案 + HTTPS；H5 同样 HSTS。
- [ ] **F-3 token 持久化加密**：`utils` 中 token 写 `uni.setStorageSync` 时**不要**写明文，可用 `crypto-js` AES + 设备指纹做轻量加密；切勿写到 `localStorage` 与 url 里。
- [ ] **F-4 v-html 审计**：用户端任何 `v-html` 必须经过后端富文本白名单清洗（S-1）；前端再用 `DOMPurify`（H5 端）二次过滤。
- [ ] **F-5 防抖 + 幂等**：下单/付款按钮 disable 至响应返回；网络异常重试只对幂等 GET。
- [ ] **F-6 错误日志上报**：H5 用 `Sentry`，小程序用 `wx.onError` 上报到 `/api/pub/log`（带 traceId）。
- [ ] **F-7 隐私合规**：小程序首启弹《用户协议+隐私政策》，确认后再 `wx.login`；接入"用户信息授权"按官方规范。
- [ ] **F-8 包体瘦身**：移除 `data` 目录中的开发用 mock 数据；图片走 OSS + WebP；`pagesA/B/C` 分包。
- [ ] **F-9 防抓包**：小程序勾选"业务域名 SSL Pinning"（如有自建网关层）；H5 关键接口签名（`X-Sign = HMAC(SK, ts+nonce+body)`，Sk 在用户登录后下发，TTL 与 token 一致）。
- [ ] **F-10 CSP**：H5 端在 `index.html` `<meta http-equiv="Content-Security-Policy" ...>`；与 S-9 后端 CSP 头对齐。

---

## O. 运维 / 部署

- [ ] **O-1 反向代理**：Nginx 强制 HTTPS（TLS 1.2+，禁 RC4/3DES），`ssl_protocols TLSv1.2 TLSv1.3`，`add_header Strict-Transport-Security ...`。
- [ ] **O-2 防火墙**：仅放通 80/443；MySQL 3306 / Redis 6379 / 应用 8080 全部封闭，出网走 NAT。
- [ ] **O-3 容器化**：
  ```dockerfile
  FROM eclipse-temurin:17-jre-alpine
  RUN addgroup -S app && adduser -S app -G app
  USER app
  COPY target/xianguoji-server-*.jar /app/app.jar
  ENV JAVA_OPTS="-XX:+UseG1GC -Xms512m -Xmx1g -Dfile.encoding=UTF-8"
  ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
  ```
  - 非 root 用户，readonly rootfs，资源限制 `--memory=2g --cpus=2`。
- [ ] **O-4 Web 入口加 WAF**：阿里云 WAF / Cloudflare，CC 防护 + 中国节点；`/api/auth`、`/api/u/order`、`/api/u/cart` 设独立精细规则。
- [ ] **O-5 监控**：
  - 业务：Prometheus + Grafana（QPS、P95、错误率、订单量）；
  - 安全：`xgj:sec:*` Redis Stream → 钉钉机器人；
  - 健康检查：`/api/pub/health`（不打日志）；
  - 日志：ELK / Loki + Grafana。
- [ ] **O-6 灰度 / 回滚**：蓝绿或灰度部署，回滚 < 5min；首次发布做"小流量 5% → 25% → 100%"。
- [ ] **O-7 应急预案**：DDoS / 数据泄露 / 支付重大故障三类预案 SOP，演练一次。
- [ ] **O-8 备份隔离**：数据库备份写**异地** OSS / S3，开启版本与不可变锁；定期演练恢复。

---

## 附录 A：production profile 模板

`application-prod.yml`（替换原文件）：

```yaml
server:
  port: 8080
  forward-headers-strategy: framework
  tomcat:
    connection-timeout: 10s
    max-swallow-size: 10MB
    threads:
      max: 200
      min-spare: 20
    accesslog:
      enabled: true
      directory: /var/log/xianguoji
      pattern: '%t %a %r %s %b %D'

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT:3306}/${MYSQL_DB}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=true&requireSSL=true&allowPublicKeyRetrieval=false
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
    druid:
      max-active: 50
      min-idle: 10
      validation-query: SELECT 1
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 1000
        wall:
          enabled: true
          config:
            multi-statement-allow: false
            none-base-statement-allow: false
      stat-view-servlet:
        enabled: false
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: ${REDIS_SSL:false}
      lettuce:
        pool:
          max-active: 32
          max-idle: 16
          min-idle: 4

xianguoji:
  jwt:
    active-kid: ${JWT_ACTIVE_KID}
    secrets:
      v1: ${JWT_SECRET_V1}
      v2: ${JWT_SECRET_V2}
    user-access-ttl-minutes: 120
    user-refresh-ttl-hours: 168
    admin-access-ttl-minutes: 30
    admin-refresh-ttl-hours: 12
  upload:
    base-dir: /data/xianguoji/upload
    domain: https://cdn.xianguoji.com
  oss:
    endpoint: ${ALIYUN_OSS_ENDPOINT}
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
    bucket: ${ALIYUN_OSS_BUCKET}
    domain: https://cdn.xianguoji.com
  security:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS}
    hotspot:
      whitelist: ${HOTSPOT_WHITELIST_IPS:}
    alert:
      webhook: ${ALERT_WEBHOOK_URL:}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
knife4j:
  enable: false
  production: true

logging:
  level:
    root: warn
    com.xianguoji: info
    com.alibaba.druid.filter.stat: info
  file:
    path: /var/log/xianguoji
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
```

---

## 附录 B：所需新增依赖

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<!-- 富文本白名单（已有 jsoup） -->
<!-- 文件类型识别（推荐 Tika core，不带 parsers，体积小） -->
<dependency>
  <groupId>org.apache.tika</groupId>
  <artifactId>tika-core</artifactId>
  <version>2.9.2</version>
</dependency>
<!-- BCrypt 已通过 spring-security-crypto 提供，无需新增 -->
<!-- OWASP Dependency-Check Plugin 见 S-16 -->
```

> **不要**新增 `spring-boot-starter-security`：当前用拦截器路线已够用，引入后会与现有 JWT 拦截链冲突，必须二选一。如选 Security，则全套替换为 `JwtAuthenticationFilter` + `SecurityFilterChain`（参考 `blog-backend/.../config/SecurityConfig.java` 与 `security/JwtAuthenticationFilter.java`）。

---

## 附录 C：实现优先级矩阵

| 等级 | 范围 | 上线门槛 |
| --- | --- | --- |
| **必须 (Blocker)** | S-1, S-2, S-3, S-4, S-5, S-8, S-9, S-10, S-11, S-12, P0-1~P0-7, F-1, F-2, O-1, O-2 | 不达成不允许发版 |
| **强烈建议 (High)** | S-7, S-13, S-14, S-15, P1-1~P1-9, F-3, F-5, O-3, O-4, O-5 | T+7 内补齐 |
| **持续改进 (Medium)** | S-6, S-16, S-17, P2-*, F-6~F-10, O-6~O-8 | T+30 滚动迭代 |

---

## 落地建议（给编码 AI 的上下文）

1. **顺序**：先做 S-1/S-2/S-3/S-8 这四件事（Filter / Aspect / CORS），它们对现有代码侵入最小，能立刻显著提升防护能力。
2. **改造 LoginInterceptor 即可**：项目用拦截器而非 Spring Security，新增的过滤器全部写成 `OncePerRequestFilter` + `@Order` 即可串入，不要引入 Security 改造拦截链。
3. **每个新 Filter / Aspect** 必须配套一个 `*Test` 单测；`MockMvc` 跑用例。
4. **不要**：删除/重构现有 `RateLimitAspect`、`JwtBlacklistManager`、`@LoginRequired/@AdminRequired`，只在其上增量。
5. **配置变更**：所有新增 key 集中到 `xianguoji.security.*`，并在 `application-dev.example.yml` 同步示例。
6. **回归用例**：执行完所有改造后跑一次"完整下单 → 支付 → 退款"链路冒烟，确保 XSS/SQLi 过滤器没有误伤业务字段（如订单备注里的 `&`、地址中的 `#`）。

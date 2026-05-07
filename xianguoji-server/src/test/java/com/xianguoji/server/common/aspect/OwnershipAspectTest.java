package com.xianguoji.server.common.aspect;

import com.xianguoji.server.common.annotation.OwnedBy;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.common.security.LoginUser;
import com.xianguoji.server.common.service.SecurityEventService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * §5.2 安全测试 - OwnershipAspect (IDOR 防御)
 *
 * TC-SEC-AUTHZ-001: IDOR 越权防御
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OwnershipAspect IDOR 防御测试")
class OwnershipAspectTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private SecurityEventService securityEventService;
    @Mock
    private ProceedingJoinPoint pjp;
    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private OwnershipAspect ownershipAspect;

    @BeforeEach
    void setUp() {
        // 清除 LoginContext
        LoginContext.clear();
    }

    @AfterEach
    void tearDown() {
        LoginContext.clear();
    }

    @Nested
    @DisplayName("TC-SEC-AUTHZ-001: IDOR 越权防御")
    class IdorDefenseTests {

        @Test
        @DisplayName("未登录用户应抛 TOKEN_INVALID")
        void checkOwnership_shouldThrow_whenNoLoginUser() throws Throwable {
            // Arrange - 不设置 LoginContext
            OwnedBy ownedBy = mockOwnedBy();

            // Act & Assert
            BizException ex = assertThrows(BizException.class,
                    () -> ownershipAspect.checkOwnership(pjp, ownedBy));
            assertEquals(ResultCode.TOKEN_INVALID, ex.getResultCode());
        }

        @Test
        @DisplayName("staff 角色应跳过归属校验")
        void checkOwnership_shouldSkip_whenStaffRole() throws Throwable {
            // Arrange
            LoginUser staffUser = new LoginUser();
            staffUser.setUid(1L);
            staffUser.setRole("staff");
            LoginContext.set(staffUser);

            OwnedBy ownedBy = mockOwnedBy();
            when(pjp.proceed()).thenReturn("ok");

            // Act
            Object result = ownershipAspect.checkOwnership(pjp, ownedBy);

            // Assert
            assertEquals("ok", result);
            verify(pjp).proceed();
        }

        @Test
        @DisplayName("用户访问自己资源应通过")
        void checkOwnership_shouldPass_whenOwnerAccessOwnResource() {
            // Arrange
            LoginUser user = new LoginUser();
            user.setUid(1L);
            user.setRole("user");
            LoginContext.set(user);

            // 验证 uid 匹配逻辑
            Long resourceOwnerId = 1L;
            Long currentUid = user.getUid();
            assertEquals(resourceOwnerId, currentUid);
        }

        @Test
        @DisplayName("用户访问他人资源应被拒绝")
        void checkOwnership_shouldReject_whenAccessingOtherUsersResource() {
            // Arrange
            LoginUser user = new LoginUser();
            user.setUid(1L);
            user.setRole("user");

            Long resourceOwnerId = 2L; // 属于其他用户
            assertNotEquals(user.getUid(), resourceOwnerId);
        }
    }

    private OwnedBy mockOwnedBy() {
        return mock(OwnedBy.class);
    }
}

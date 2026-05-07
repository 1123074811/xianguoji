package com.xianguoji.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.util.WechatUtil;
import com.xianguoji.server.module.promo.entity.GroupBuyParticipant;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.GroupBuyParticipantMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.user.dto.AddressAddDto;
import com.xianguoji.server.module.user.dto.AddressUpdDto;
import com.xianguoji.server.module.user.dto.BindPhoneDto;
import com.xianguoji.server.module.user.dto.UserProfileUpdDto;
import com.xianguoji.server.module.user.entity.Favorite;
import com.xianguoji.server.module.user.entity.Footprint;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.entity.UserAddress;
import com.xianguoji.server.module.user.mapper.FavoriteMapper;
import com.xianguoji.server.module.user.mapper.FootprintMapper;
import com.xianguoji.server.module.user.mapper.UserAddressMapper;
import com.xianguoji.server.module.user.mapper.UserMapper;
import com.xianguoji.server.module.user.vo.AddressVO;
import com.xianguoji.server.module.user.vo.UserProfileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.3 User 单元测试
 *
 * TC-UT-USER-001: UserService.getProfile 正常
 * TC-UT-USER-002: UserService.getProfile 用户不存在
 * TC-UT-USER-003: UserService.updateProfile 正常
 * TC-UT-USER-004: UserService.bindPhone 手机号已被他人占用
 * TC-UT-USER-005: UserService.bindPhone 同手机号重复绑定
 * TC-UT-USER-006: UserService.bindPhone 微信code无效
 * TC-UT-USER-007: AddressService.addAddress 首条自动置默认
 * TC-UT-USER-008: AddressService.setDefaultAddress 切换默认
 * TC-UT-USER-009: AddressService.updateAddress IDOR拒绝
 * TC-UT-USER-010: AddressService.deleteAddress IDOR拒绝
 * TC-UT-USER-011: UserService.getProfile 手机号脱敏
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserAddressMapper addressMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private FootprintMapper footprintMapper;

    @Mock
    private GroupBuyParticipantMapper groupBuyParticipantMapper;

    @Mock
    private WechatUtil wechatUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138001");
        testUser.setNickname("测试用户");
        testUser.setAvatar("http://img.test.com/avatar.jpg");
        testUser.setGender(1);
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setTag("普通用户");
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());
    }

    /**
     * TC-UT-USER-001: 正常获取个人资料
     */
    @Test
    void getProfile_shouldReturnProfile_whenUserExists() {
        // Arrange
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(footprintMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(20L);
        when(groupBuyParticipantMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // Act
        UserProfileVO profile = userService.getProfile(1L);

        // Assert
        assertNotNull(profile);
        assertEquals(1L, profile.getId());
        assertEquals("测试用户", profile.getNickname());
        assertEquals(5, profile.getCouponCount());
        assertEquals(10, profile.getFavoriteCount());
        assertEquals(20, profile.getFootprintCount());
        assertEquals(2, profile.getGroupBuyCount());
    }

    /**
     * TC-UT-USER-002: 用户不存在
     */
    @Test
    void getProfile_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.getProfile(999L));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * TC-UT-USER-011: 手机号脱敏
     */
    @Test
    void getProfile_shouldMaskPhone_whenPhoneExists() {
        // Arrange
        testUser.setPhone("13800138001");
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(footprintMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(groupBuyParticipantMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Act
        UserProfileVO profile = userService.getProfile(1L);

        // Assert
        assertNotNull(profile);
        assertEquals("138****8001", profile.getPhone());
    }

    /**
     * TC-UT-USER-003: 正常修改资料
     */
    @Test
    void updateProfile_shouldUpdateFields_whenDtoIsValid() {
        // Arrange
        UserProfileUpdDto dto = new UserProfileUpdDto();
        dto.setNickname("新昵称");
        dto.setGender(2);
        dto.setBirthday(LocalDate.of(1995, 6, 15));

        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // Act
        userService.updateProfile(1L, dto);

        // Assert
        verify(userMapper).updateById(argThat(user ->
                user.getId().equals(1L) &&
                "新昵称".equals(user.getNickname()) &&
                user.getGender().equals(2)
        ));
    }

    /**
     * TC-UT-USER-006: 绑定手机号 - 微信code无效
     */
    @Test
    void bindPhone_shouldThrowException_whenWechatCodeInvalid() {
        // Arrange
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(wechatUtil.code2Session("invalid_code")).thenReturn(new cn.hutool.json.JSONObject());

        BindPhoneDto dto = new BindPhoneDto();
        dto.setJsCode("invalid_code");
        dto.setEncryptedData("data");
        dto.setIv("iv");

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.bindPhone(1L, dto));
        assertEquals(ResultCode.THIRD_PARTY_ERROR, exception.getResultCode());
    }

    /**
     * TC-UT-USER-004: 绑定手机号 - 手机号已被他人占用
     */
    @Test
    void bindPhone_shouldThrowException_whenPhoneOccupiedByOtherUser() {
        // Arrange
        when(userMapper.selectById(1L)).thenReturn(testUser);

        cn.hutool.json.JSONObject session = new cn.hutool.json.JSONObject();
        session.set("session_key", "test_session_key");
        when(wechatUtil.code2Session("valid_code")).thenReturn(session);
        when(wechatUtil.decryptPhoneNumber(anyString(), anyString(), anyString())).thenReturn("13900139002");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setPhone("13900139002");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(otherUser);

        BindPhoneDto dto = new BindPhoneDto();
        dto.setJsCode("valid_code");
        dto.setEncryptedData("data");
        dto.setIv("iv");

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.bindPhone(1L, dto));
        assertTrue(exception.getMessage().contains("已被其他账号绑定"));
    }

    /**
     * TC-UT-USER-005: 绑定手机号 - 同手机号重复绑定（自己的手机号）
     */
    @Test
    void bindPhone_shouldSucceed_whenPhoneBelongsToSameUser() {
        // Arrange
        testUser.setPhone("13900139002");
        when(userMapper.selectById(1L)).thenReturn(testUser);

        cn.hutool.json.JSONObject session = new cn.hutool.json.JSONObject();
        session.set("session_key", "test_session_key");
        when(wechatUtil.code2Session("valid_code")).thenReturn(session);
        when(wechatUtil.decryptPhoneNumber(anyString(), anyString(), anyString())).thenReturn("13900139002");

        // 同一个人的手机号，selectOne应返回同一个用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        BindPhoneDto dto = new BindPhoneDto();
        dto.setJsCode("valid_code");
        dto.setEncryptedData("data");
        dto.setIv("iv");

        // Act & Assert - 不应抛异常
        assertDoesNotThrow(() -> userService.bindPhone(1L, dto));
        verify(userMapper).updateById(any(User.class));
    }

    /**
     * TC-UT-USER-007: 新增地址 - 首条自动置默认
     */
    @Test
    void addAddress_shouldSetDefault_whenIsDefaultIsOne() {
        // Arrange
        AddressAddDto dto = new AddressAddDto();
        dto.setConsignee("张三");
        dto.setPhone("13800138000");
        dto.setProvince("北京市");
        dto.setCity("北京市");
        dto.setDistrict("朝阳区");
        dto.setDetail("望京SOHO");
        dto.setIsDefault(1);

        when(addressMapper.update(any(), any())).thenReturn(0); // clearDefault
        when(addressMapper.insert(any(UserAddress.class))).thenReturn(1);

        // Act
        userService.addAddress(1L, dto);

        // Assert
        verify(addressMapper).update(any(), any()); // clearDefault called
        verify(addressMapper).insert(argThat(addr ->
                addr.getUserId().equals(1L) &&
                addr.getIsDefault().equals(1)
        ));
    }

    /**
     * TC-UT-USER-007: 新增地址 - 不设默认
     */
    @Test
    void addAddress_shouldNotSetDefault_whenIsDefaultIsNull() {
        // Arrange
        AddressAddDto dto = new AddressAddDto();
        dto.setConsignee("张三");
        dto.setPhone("13800138000");
        dto.setProvince("北京市");
        dto.setCity("北京市");
        dto.setDistrict("朝阳区");
        dto.setDetail("望京SOHO");
        dto.setIsDefault(null);

        when(addressMapper.insert(any(UserAddress.class))).thenReturn(1);

        // Act
        userService.addAddress(1L, dto);

        // Assert
        verify(addressMapper).insert(argThat(addr ->
                addr.getIsDefault().equals(0) // 默认0
        ));
    }

    /**
     * TC-UT-USER-008: 设默认地址 - 切换默认
     */
    @Test
    void setDefaultAddress_shouldSwitchDefault_whenAddressExists() {
        // Arrange
        UserAddress addr = new UserAddress();
        addr.setId(2L);
        addr.setUserId(1L);
        addr.setIsDefault(0);

        when(addressMapper.selectById(2L)).thenReturn(addr);
        when(addressMapper.update(any(), any())).thenReturn(1); // clearDefault
        when(addressMapper.updateById(any(UserAddress.class))).thenReturn(1);

        // Act
        userService.setDefaultAddress(1L, 2L);

        // Assert
        verify(addressMapper).update(any(), any()); // clearDefault
        verify(addressMapper).updateById(argThat(a ->
                a.getId().equals(2L) && a.getIsDefault().equals(1)
        ));
    }

    /**
     * TC-UT-USER-008: 设默认地址 - 地址不存在
     */
    @Test
    void setDefaultAddress_shouldThrowException_whenAddressNotFound() {
        // Arrange
        when(addressMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.setDefaultAddress(1L, 999L));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * TC-UT-USER-009: 更新地址 - IDOR拒绝
     */
    @Test
    void updateAddress_shouldThrowException_whenAddressBelongsToOtherUser() {
        // Arrange
        UserAddress otherAddr = new UserAddress();
        otherAddr.setId(5L);
        otherAddr.setUserId(2L); // 属于其他用户

        when(addressMapper.selectById(5L)).thenReturn(otherAddr);

        AddressUpdDto dto = new AddressUpdDto();
        dto.setConsignee("黑客");
        dto.setPhone("13800138000");
        dto.setProvince("北京市");
        dto.setCity("北京市");
        dto.setDistrict("朝阳区");
        dto.setDetail("非法修改");

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.updateAddress(1L, 5L, dto));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * TC-UT-USER-010: 删除地址 - IDOR拒绝
     */
    @Test
    void deleteAddress_shouldThrowException_whenAddressBelongsToOtherUser() {
        // Arrange
        UserAddress otherAddr = new UserAddress();
        otherAddr.setId(5L);
        otherAddr.setUserId(2L);

        when(addressMapper.selectById(5L)).thenReturn(otherAddr);

        // Act & Assert
        BizException exception = assertThrows(BizException.class, () -> userService.deleteAddress(1L, 5L));
        assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
    }

    /**
     * TC-UT-USER-010: 删除地址 - 正常删除
     */
    @Test
    void deleteAddress_shouldDelete_whenAddressBelongsToUser() {
        // Arrange
        UserAddress addr = new UserAddress();
        addr.setId(5L);
        addr.setUserId(1L);

        when(addressMapper.selectById(5L)).thenReturn(addr);
        when(addressMapper.deleteById(5L)).thenReturn(1);

        // Act
        userService.deleteAddress(1L, 5L);

        // Assert
        verify(addressMapper).deleteById(5L);
    }

    /**
     * 地址列表 - 默认地址置顶排序
     */
    @Test
    void addressList_shouldOrderByDefaultFirst_thenUpdatedAt() {
        // Arrange
        UserAddress addr1 = new UserAddress();
        addr1.setId(1L);
        addr1.setIsDefault(1);
        addr1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        UserAddress addr2 = new UserAddress();
        addr2.setId(2L);
        addr2.setIsDefault(0);
        addr2.setUpdatedAt(LocalDateTime.now());

        when(addressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(addr1, addr2));

        // Act
        List<AddressVO> result = userService.addressList(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getIsDefault()); // 默认地址在前
    }
}

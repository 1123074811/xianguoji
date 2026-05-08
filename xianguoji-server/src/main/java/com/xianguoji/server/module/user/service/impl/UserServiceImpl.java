package com.xianguoji.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.xianguoji.server.module.user.service.UserService;
import com.xianguoji.server.module.user.vo.AddressVO;
import com.xianguoji.server.module.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final UserCouponMapper userCouponMapper;
    private final FavoriteMapper favoriteMapper;
    private final FootprintMapper footprintMapper;
    private final GroupBuyParticipantMapper groupBuyParticipantMapper;
    private final WechatUtil wechatUtil;

    @Override
    public UserProfileVO getProfile(Long uid) {
        User user = userMapper.selectById(uid);
        if (user == null) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");

        Long couponCount = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, uid).eq(UserCoupon::getStatus, 0));
        Long favoriteCount = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, uid));
        Long footprintCount = footprintMapper.selectCount(
                new LambdaQueryWrapper<Footprint>().eq(Footprint::getUserId, uid));
        Long groupBuyCount = groupBuyParticipantMapper.selectCount(
                new LambdaQueryWrapper<GroupBuyParticipant>().eq(GroupBuyParticipant::getUserId, uid));

        return UserProfileVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(maskPhone(user.getPhone()))
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .tag(user.getTag())
                .registerTime(user.getRegisterTime())
                .couponCount(couponCount.intValue())
                .favoriteCount(favoriteCount.intValue())
                .footprintCount(footprintCount.intValue())
                .groupBuyCount(groupBuyCount.intValue())
                .build();
    }

    @Override
    public void updateProfile(Long uid, UserProfileUpdDto dto) {
        User user = new User();
        user.setId(uid);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        userMapper.updateById(user);
    }

    @Override
    public void bindPhone(Long uid, BindPhoneDto dto) {
        User user = userMapper.selectById(uid);
        if (user == null) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");

        // 使用微信新版 getPhoneNumber API，通过 code 直接换取手机号
        String phone = wechatUtil.getPhoneNumber(dto.getCode());

        // 检查手机号是否已被其他用户绑定
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (existing != null && !existing.getId().equals(uid)) {
            throw new BizException(ResultCode.BIZ_ERROR, "该手机号已被其他账号绑定");
        }

        // 绑定
        User upd = new User();
        upd.setId(uid);
        upd.setPhone(phone);
        userMapper.updateById(upd);
        log.info("[bindPhone] uid={} phone={} 绑定成功", uid, maskPhone(phone));
    }

    @Override
    public List<AddressVO> addressList(Long uid) {
        List<UserAddress> list = addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, uid)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getUpdatedAt));
        return list.stream().map(this::toAddressVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long uid, AddressAddDto dto) {
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault(uid);
        }
        UserAddress addr = new UserAddress();
        addr.setUserId(uid);
        addr.setConsignee(dto.getConsignee());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setTag(dto.getTag());
        addr.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        addr.setLongitude(dto.getLongitude());
        addr.setLatitude(dto.getLatitude());
        addressMapper.insert(addr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long uid, Long id, AddressUpdDto dto) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(uid)) {
            throw new BizException(ResultCode.NOT_FOUND, "地址不存在");
        }
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault(uid);
        }
        UserAddress addr = new UserAddress();
        addr.setId(id);
        addr.setConsignee(dto.getConsignee());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setTag(dto.getTag());
        addr.setIsDefault(dto.getIsDefault());
        addr.setLongitude(dto.getLongitude());
        addr.setLatitude(dto.getLatitude());
        addressMapper.updateById(addr);
    }

    @Override
    public void deleteAddress(Long uid, Long id) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(uid)) {
            throw new BizException(ResultCode.NOT_FOUND, "地址不存在");
        }
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long uid, Long id) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(uid)) {
            throw new BizException(ResultCode.NOT_FOUND, "地址不存在");
        }
        clearDefault(uid);
        UserAddress addr = new UserAddress();
        addr.setId(id);
        addr.setIsDefault(1);
        addressMapper.updateById(addr);
    }

    private void clearDefault(Long uid) {
        addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, uid)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0));
    }

    private AddressVO toAddressVO(UserAddress a) {
        return AddressVO.builder()
                .id(a.getId())
                .consignee(a.getConsignee())
                .phone(a.getPhone())
                .province(a.getProvince())
                .city(a.getCity())
                .district(a.getDistrict())
                .detail(a.getDetail())
                .tag(a.getTag())
                .isDefault(a.getIsDefault())
                .longitude(a.getLongitude())
                .latitude(a.getLatitude())
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

package com.xianguoji.server.module.user.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.user.dto.AddressAddDto;
import com.xianguoji.server.module.user.dto.AddressUpdDto;
import com.xianguoji.server.module.user.dto.BindPhoneDto;
import com.xianguoji.server.module.user.dto.UserProfileUpdDto;
import com.xianguoji.server.module.user.vo.AddressVO;
import com.xianguoji.server.module.user.vo.UserProfileVO;

import java.util.List;

public interface UserService {

    UserProfileVO getProfile(Long uid);

    void updateProfile(Long uid, UserProfileUpdDto dto);

    void bindPhone(Long uid, BindPhoneDto dto);

    List<AddressVO> addressList(Long uid);

    void addAddress(Long uid, AddressAddDto dto);

    void updateAddress(Long uid, Long id, AddressUpdDto dto);

    void deleteAddress(Long uid, Long id);

    void setDefaultAddress(Long uid, Long id);
}

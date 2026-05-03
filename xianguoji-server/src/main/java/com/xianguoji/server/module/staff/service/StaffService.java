package com.xianguoji.server.module.staff.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.staff.dto.StaffAddDto;
import com.xianguoji.server.module.staff.dto.StaffUpdDto;
import com.xianguoji.server.module.staff.vo.StaffVO;

public interface StaffService {

    PageVO<StaffVO> staffPage(Integer page, Integer size);

    void addStaff(StaffAddDto dto);

    void updateStaff(Long id, StaffUpdDto dto);

    void deleteStaff(Long id);

    void resetPassword(Long id, String newPassword);

    void changeSelfPassword(Long sid, String oldPassword, String newPassword);
}

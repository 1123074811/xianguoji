package com.xianguoji.server.module.staff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.staff.dto.StaffAddDto;
import com.xianguoji.server.module.staff.dto.StaffUpdDto;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.staff.service.StaffService;
import com.xianguoji.server.module.staff.vo.StaffVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Override
    public PageVO<StaffVO> staffPage(Integer page, Integer size) {
        Page<Staff> p = staffMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Staff>().orderByDesc(Staff::getCreatedAt));
        List<StaffVO> voList = p.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public void addStaff(StaffAddDto dto) {
        Staff existing = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, dto.getUsername()));
        if (existing != null) throw new BizException(ResultCode.CONFLICT, "账号已存在");

        Staff staff = new Staff();
        staff.setUsername(dto.getUsername());
        staff.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        staff.setName(dto.getName());
        staff.setPhone(dto.getPhone());
        staff.setAvatar(dto.getAvatar());
        staff.setRole(dto.getRole());
        staff.setPermissions(dto.getPermissions());
        staff.setStatus(1);
        staffMapper.insert(staff);
    }

    @Override
    public void updateStaff(Long id, StaffUpdDto dto) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) throw new BizException(ResultCode.NOT_FOUND);
        if (dto.getName() != null) staff.setName(dto.getName());
        if (dto.getPhone() != null) staff.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) staff.setAvatar(dto.getAvatar());
        if (dto.getRole() != null) staff.setRole(dto.getRole());
        if (dto.getPermissions() != null) staff.setPermissions(dto.getPermissions());
        if (dto.getStatus() != null) staff.setStatus(dto.getStatus());
        staffMapper.updateById(staff);
    }

    @Override
    public void deleteStaff(Long id) {
        staffMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) throw new BizException(ResultCode.NOT_FOUND);
        staff.setPasswordHash(passwordEncoder.encode(newPassword));
        staffMapper.updateById(staff);
    }

    @Override
    public void changeSelfPassword(Long sid, String oldPassword, String newPassword) {
        Staff staff = staffMapper.selectById(sid);
        if (staff == null) throw new BizException(ResultCode.NOT_FOUND);
        if (!passwordEncoder.matches(oldPassword, staff.getPasswordHash())) {
            throw new BizException(ResultCode.BIZ_ERROR, "原密码不正确");
        }
        staff.setPasswordHash(passwordEncoder.encode(newPassword));
        staffMapper.updateById(staff);
    }

    private StaffVO toVO(Staff s) {
        return StaffVO.builder()
                .id(s.getId()).username(s.getUsername()).name(s.getName())
                .phone(s.getPhone()).avatar(s.getAvatar()).role(s.getRole())
                .permissions(s.getPermissions()).status(s.getStatus())
                .lastLoginAt(s.getLastLoginAt()).createdAt(s.getCreatedAt())
                .build();
    }
}

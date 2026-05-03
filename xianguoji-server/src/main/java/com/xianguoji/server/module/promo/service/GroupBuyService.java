package com.xianguoji.server.module.promo.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.promo.dto.GroupBuyLaunchDto;
import com.xianguoji.server.module.promo.vo.GroupBuyActivityVO;
import com.xianguoji.server.module.promo.vo.GroupBuyInstanceVO;

public interface GroupBuyService {

    PageVO<GroupBuyActivityVO> getGroupBuyPage(Integer page, Integer size);

    Long launch(Long uid, GroupBuyLaunchDto dto);

    Long join(Long uid, Long instanceId, com.xianguoji.server.module.promo.dto.GroupBuyJoinDto dto);

    GroupBuyInstanceVO getInstanceDetail(Long instanceId);

    void scanExpiredInstances();
}

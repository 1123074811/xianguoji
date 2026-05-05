package com.xianguoji.server.common.cache;

import com.xianguoji.server.module.shop.entity.PickupPoint;
import com.xianguoji.server.module.shop.mapper.PickupPointMapper;
import com.xianguoji.server.module.shop.vo.PickupPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupGeoService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PickupPointMapper pickupPointMapper;

    private static final String GEO_KEY = "xgj:geo:pickup";

    /**
     * 初始化/重建 GEO 索引（自提点增删改时调用）
     */
    public void rebuildGeoIndex() {
        stringRedisTemplate.delete(GEO_KEY);
        List<PickupPoint> points = pickupPointMapper.selectList(null);
        GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
        for (PickupPoint p : points) {
            if (p.getLongitude() != null && p.getLatitude() != null
                    && p.getStatus() != null && p.getStatus() == 1) {
                geoOps.add(GEO_KEY,
                        new GeoLocation<String>(String.valueOf(p.getId()),
                                new Point(p.getLongitude().doubleValue(), p.getLatitude().doubleValue())));
            }
        }
        log.info("[PickupGeo] GEO索引重建完成, 共 {} 个自提点", points.size());
    }

    /**
     * 按距离排序返回自提点列表
     *
     * @param lng 用户经度
     * @param lat 用户纬度
     * @param radiusKm 搜索半径(km)，默认 50
     * @return 按距离升序排列的自提点
     */
    public List<PickupPointVO> sortByDistance(double lng, double lat, double radiusKm) {
        GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();

        Circle within = new Circle(new Point(lng, lat), new Distance(radiusKm, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs().includeDistance().sortAscending();

        GeoResults<GeoLocation<String>> results = geoOps.radius(GEO_KEY, within, args);

        if (results == null || results.getContent().isEmpty()) {
            // GEO 无数据 → 重建索引后重试
            rebuildGeoIndex();
            results = geoOps.radius(GEO_KEY, within, args);
        }

        List<PickupPointVO> list = new ArrayList<>();
        if (results != null) {
            for (var r : results.getContent()) {
                String idStr = r.getContent().getName();
                double distanceKm = r.getDistance().getValue();
                Long id = Long.valueOf(idStr);
                PickupPoint p = pickupPointMapper.selectById(id);
                if (p != null) {
                    PickupPointVO vo = PickupPointVO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .address(p.getAddress())
                            .phone(p.getPhone())
                            .businessHours(p.getBusinessHours())
                            .longitude(p.getLongitude())
                            .latitude(p.getLatitude())
                            .distance(Math.round(distanceKm * 1000.0) / 1000.0)
                            .build();
                    list.add(vo);
                }
            }
        }
        return list;
    }
}

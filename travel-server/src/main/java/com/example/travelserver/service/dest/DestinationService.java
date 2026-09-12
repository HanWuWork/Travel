package com.example.travelserver.service.dest;

import com.example.travelserver.vo.dest.AttractionVO;
import com.example.travelserver.vo.dest.CityVO;

import java.util.List;

/**
 * 目的地/景点库服务：城市与地点查询，供地图总览、景点库、周边探索等使用
 */
public interface DestinationService {

    /** 城市列表（按热门优先） */
    List<CityVO> listCities();

    /** 城市详情 */
    CityVO getCity(Long id);

    /** 按城市查询地点列表，type/keyword 可选 */
    List<AttractionVO> listAttractions(Long cityId, String type, String keyword);

    /** 地点详情 */
    AttractionVO getAttraction(Long id);

    /**
     * 附近地点：以指定坐标为中心，返回半径内地点（按距离升序）
     *
     * @param lat      纬度（WGS-84）
     * @param lng      经度（WGS-84）
     * @param radiusKm 搜索半径（公里）
     * @param type     类型筛选（可空）
     */
    List<AttractionVO> nearby(double lat, double lng, double radiusKm, String type);

    /**
     * 热门景点：全库按评分取前 N 个景点（用于首页推荐位，保证点击进入的是同一条数据）
     *
     * @param limit 数量
     */
    List<AttractionVO> hotSpots(int limit);
}

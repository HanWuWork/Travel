package com.example.travelserver.service.dest;

import com.example.travelserver.vo.travel.PlanPoiVO;

import java.util.List;

/**
 * 行程 POI 坐标匹配：把 AI 行程文本中提到的地点名与库内景点坐标进行模糊匹配
 */
public interface PoiMatchService {

    /**
     * 根据目的地城市名匹配城市；返回 null 表示库内无该城市
     */
    Long matchCityId(String destination);

    /**
     * 将若干地点名称与指定城市的景点做匹配，返回带坐标的 POI（按传入顺序编号）
     *
     * @param cityId 城市ID（可空，为空时尝试全库匹配）
     * @param names  地点名称列表
     * @return 匹配成功的 POI 列表
     */
    List<PlanPoiVO> matchPois(Long cityId, List<String> names);

    /**
     * 兜底：返回目的地城市评分最高的若干个景点坐标（用于行程未识别出 POI 时保证地图可用）
     *
     * @param destination 目的地名称
     * @param limit       最多返回数量
     */
    List<PlanPoiVO> topPois(String destination, int limit);
}

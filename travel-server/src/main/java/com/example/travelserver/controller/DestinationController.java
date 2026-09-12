package com.example.travelserver.controller;

import com.example.travelserver.service.dest.DestinationService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.dest.AttractionVO;
import com.example.travelserver.vo.dest.CityVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 目的地/景点库接口（公开，无需登录）
 */
@RestController
@RequestMapping("/api/dest")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    /** 城市列表（热门优先） */
    @GetMapping("/cities")
    public Result<List<CityVO>> listCities() {
        return Result.ok(destinationService.listCities());
    }

    /** 城市详情 */
    @GetMapping("/cities/{id}")
    public Result<CityVO> getCity(@PathVariable Long id) {
        return Result.ok(destinationService.getCity(id));
    }

    /** 城市下的地点列表（地图总览/景点库共用） */
    @GetMapping("/attractions")
    public Result<List<AttractionVO>> listAttractions(
            @RequestParam Long cityId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.ok(destinationService.listAttractions(cityId, type, keyword));
    }

    /** 地点详情 */
    @GetMapping("/attractions/{id}")
    public Result<AttractionVO> getAttraction(@PathVariable Long id) {
        return Result.ok(destinationService.getAttraction(id));
    }

    /** 附近地点（按距离升序，默认 20 公里内） */
    @GetMapping("/nearby")
    public Result<List<AttractionVO>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "20") double radiusKm,
            @RequestParam(required = false) String type) {
        return Result.ok(destinationService.nearby(lat, lng, radiusKm, type));
    }

    /** 热门景点（全库按评分排序，用于首页推荐位） */
    @GetMapping("/hot-spots")
    public Result<List<AttractionVO>> hotSpots(@RequestParam(defaultValue = "6") int limit) {
        return Result.ok(destinationService.hotSpots(limit));
    }
}

package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.PackingItem;
import com.example.travelserver.repository.PackingItemRepository;
import com.example.travelserver.service.user.PackingService;
import com.example.travelserver.vo.user.PackingItemVO;
import com.example.travelserver.vo.user.PackingSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PackingServiceImpl implements PackingService {

    /** 内置打包模板：分类 → 物品 */
    private static final Map<String, List<String>> TEMPLATE = new LinkedHashMap<>();

    static {
        TEMPLATE.put("证件", List.of("身份证", "护照/签证", "驾驶证", "学生证", "银行卡", "现金"));
        TEMPLATE.put("衣物", List.of("换洗衣物", "外套", "睡衣", "内衣袜子", "舒适鞋", "帽子/墨镜"));
        TEMPLATE.put("电子", List.of("手机", "充电器", "充电宝", "耳机", "数据线", "转换插头"));
        TEMPLATE.put("洗漱", List.of("牙刷/牙膏", "洗面奶", "防晒霜", "护肤品", "毛巾", "梳子"));
        TEMPLATE.put("药品", List.of("感冒药", "肠胃药", "创可贴", "晕车药", "驱蚊液", "个人常用药"));
        TEMPLATE.put("其他", List.of("雨伞", "水杯", "纸巾/湿巾", "口罩", "零食", "行李箱锁"));
    }

    private final PackingItemRepository packingRepository;

    public PackingServiceImpl(PackingItemRepository packingRepository) {
        this.packingRepository = packingRepository;
    }

    @Override
    public PackingSummaryVO summary(Long userId, Long tripId) {
        List<PackingItem> items = listItems(userId, tripId);
        PackingSummaryVO vo = new PackingSummaryVO();
        vo.setTotal(items.size());
        long packed = items.stream().filter(i -> Boolean.TRUE.equals(i.getPacked())).count();
        vo.setPacked(packed);
        vo.setPercent(items.isEmpty() ? 0 : (int) Math.round(packed * 100.0 / items.size()));

        Map<String, List<PackingItemVO>> grouped = new LinkedHashMap<>();
        for (PackingItem i : items) {
            grouped.computeIfAbsent(i.getCategory() == null ? "其他" : i.getCategory(), k -> new ArrayList<>())
                    .add(toVO(i));
        }
        // 按模板分类顺序输出，模板外的分类排在最后
        List<PackingSummaryVO.Group> groups = new ArrayList<>();
        for (String cat : TEMPLATE.keySet()) {
            if (grouped.containsKey(cat)) {
                groups.add(new PackingSummaryVO.Group(cat, grouped.remove(cat)));
            }
        }
        grouped.forEach((k, v) -> groups.add(new PackingSummaryVO.Group(k, v)));
        vo.setGroups(groups);
        return vo;
    }

    @Override
    @Transactional
    public PackingItemVO add(Long userId, Long tripId, String name, String category) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "物品名称不能为空");
        }
        PackingItem item = new PackingItem();
        item.setUserId(userId);
        item.setTripId(tripId);
        item.setName(name.trim());
        item.setCategory(category == null || category.isBlank() ? "其他" : category.trim());
        item.setPacked(false);
        item.setCreateTime(LocalDateTime.now());
        return toVO(packingRepository.save(item));
    }

    @Override
    @Transactional
    public PackingItemVO toggle(Long userId, Long itemId) {
        PackingItem item = mustGet(userId, itemId);
        item.setPacked(!Boolean.TRUE.equals(item.getPacked()));
        return toVO(packingRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long itemId) {
        packingRepository.delete(mustGet(userId, itemId));
    }

    @Override
    @Transactional
    public int applyTemplate(Long userId, Long tripId) {
        List<PackingItem> existing = listItems(userId, tripId);
        // 以「分类+名称」去重，避免重复生成
        var existKeys = existing.stream()
                .map(i -> (i.getCategory() == null ? "" : i.getCategory()) + "|" + i.getName())
                .collect(Collectors.toSet());

        int added = 0;
        for (Map.Entry<String, List<String>> entry : TEMPLATE.entrySet()) {
            for (String name : entry.getValue()) {
                if (existKeys.contains(entry.getKey() + "|" + name)) {
                    continue;
                }
                PackingItem item = new PackingItem();
                item.setUserId(userId);
                item.setTripId(tripId);
                item.setName(name);
                item.setCategory(entry.getKey());
                item.setPacked(false);
                item.setCreateTime(LocalDateTime.now());
                packingRepository.save(item);
                added++;
            }
        }
        return added;
    }

    @Override
    @Transactional
    public void clear(Long userId, Long tripId) {
        packingRepository.deleteAll(listItems(userId, tripId));
    }

    private List<PackingItem> listItems(Long userId, Long tripId) {
        return tripId == null
                ? packingRepository.findByUserIdAndTripIdIsNullOrderByIdAsc(userId)
                : packingRepository.findByUserIdAndTripIdOrderByIdAsc(userId, tripId);
    }

    private PackingItem mustGet(Long userId, Long itemId) {
        PackingItem item = packingRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "条目不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该条目");
        }
        return item;
    }

    private PackingItemVO toVO(PackingItem i) {
        PackingItemVO vo = new PackingItemVO();
        vo.setId(i.getId());
        vo.setTripId(i.getTripId());
        vo.setName(i.getName());
        vo.setCategory(i.getCategory());
        vo.setPacked(i.getPacked());
        return vo;
    }
}

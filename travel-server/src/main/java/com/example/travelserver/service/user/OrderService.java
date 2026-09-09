package com.example.travelserver.service.user;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.user.OrderRequest;
import com.example.travelserver.entity.TravelOrder;
import com.example.travelserver.repository.OrderRepository;
import com.example.travelserver.vo.user.OrderVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderVO create(OrderRequest request) {
        Long userId = UserContext.getUserId();
        TravelOrder order = new TravelOrder();
        order.setUserId(userId);
        order.setDestination(request.getDestination());
        order.setDays(request.getDays());
        order.setBudget(request.getBudget());
        order.setPlanJson(request.getPlanJson());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now());
        order = orderRepository.save(order);
        return toVO(order);
    }

    public List<OrderVO> listMyOrders() {
        Long userId = UserContext.getUserId();
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream().map(this::toVO).toList();
    }

    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        TravelOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        orderRepository.delete(order);
    }

    private OrderVO toVO(TravelOrder o) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setDestination(o.getDestination());
        vo.setDays(o.getDays());
        vo.setBudget(o.getBudget());
        vo.setPlanJson(o.getPlanJson());
        vo.setStatus(o.getStatus());
        vo.setCreateTime(o.getCreateTime());
        return vo;
    }
}

package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.auth.CurrentAdmin;
import com.yxtech.smartlife.common.Result;
import com.yxtech.smartlife.dto.AdminCancelOrderRequest;
import com.yxtech.smartlife.dto.AdminRentalOrderDTO;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.service.RentalTradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminRentalOrderController {

    private final RentalTradeService rentalTradeService;

    @GetMapping
    public Result<List<AdminRentalOrderDTO>> getOrders() {
        return Result.success(rentalTradeService.listAdminOrders().stream()
                .map(AdminRentalOrderDTO::fromAggregate)
                .toList());
    }

    @PostMapping("/{id}/cancel")
    public Result<AdminRentalOrderDTO> cancelOrder(
            @CurrentAdmin Admin admin,
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminCancelOrderRequest request
    ) {
        RentalOrder order = rentalTradeService.adminCancelOrder(id, admin.getId(), request.getReason());
        AdminRentalOrderDTO dto = rentalTradeService.listAdminOrders().stream()
                .filter(aggregate -> aggregate.order().getId().equals(order.getId()))
                .findFirst()
                .map(AdminRentalOrderDTO::fromAggregate)
                .orElseThrow();
        return Result.success(dto);
    }
}

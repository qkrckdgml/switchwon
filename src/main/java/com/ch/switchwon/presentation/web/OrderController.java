package com.ch.switchwon.presentation.web;

import com.ch.switchwon.application.port.in.CreateOrderUseCase;
import com.ch.switchwon.application.port.in.GetOrderListUseCase;
import com.ch.switchwon.domain.model.Currency;
import com.ch.switchwon.presentation.web.api.OrderApiContract;
import com.ch.switchwon.presentation.web.dto.ApiResponse;
import com.ch.switchwon.presentation.web.dto.OrderListResponse;
import com.ch.switchwon.presentation.web.dto.OrderRequest;
import com.ch.switchwon.presentation.web.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApiContract {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderListUseCase getOrderListUseCase;

    @Override
    public ApiResponse<OrderResponse> createOrder(
        @Valid @RequestBody OrderRequest request
    ) {
        Currency from = Currency.of(request.fromCurrency());
        Currency to = Currency.of(request.toCurrency());

        return ApiResponse.ok(
            OrderResponse.from(
                createOrderUseCase.createOrder(from, to, request.forexAmount())
            )
        );
    }

    @Override
    public ApiResponse<OrderListResponse> getOrderList() {
        return ApiResponse.ok(
            OrderListResponse.from(getOrderListUseCase.getOrders())
        );
    }
}
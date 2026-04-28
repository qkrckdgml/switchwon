package com.ch.switchwon.presentation.web.api;

import com.ch.switchwon.presentation.web.dto.ApiResponse;
import com.ch.switchwon.presentation.web.dto.OrderListResponse;
import com.ch.switchwon.presentation.web.dto.OrderRequest;
import com.ch.switchwon.presentation.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "주문", description = "외화 매수/매도 주문 API")
@RequestMapping("/order")
public interface OrderApiContract {

    @Operation(
        summary = "외화 주문 생성 (매수/매도)",
        description = """
            매수 (KRW -> 외화): fromCurrency=KRW, toCurrency=USD/JPY/CNY/EUR -> buyRate 적용
            매도 (외화 -> KRW): fromCurrency=USD/JPY/CNY/EUR, toCurrency=KRW -> sellRate 적용
        """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "주문 성공",
            content = @Content(
                schema = @Schema(implementation = OrderResponse.class),
                examples = {
                    @ExampleObject(
                        name = "매수 성공 (KRW -> USD)",
                        value = """
                            {
                              "code": "OK",
                              "message": "정상적으로 처리되었습니다.",
                              "returnObject": {
                                "id": 1,
                                "fromAmount": 294000,
                                "fromCurrency": "KRW",
                                "toAmount": 200.00,
                                "toCurrency": "USD",
                                "tradeRate": 1470.00,
                                "dateTime": "2026-04-28T10:01:00"
                              }
                            }
                        """
                    ),
                    @ExampleObject(
                        name = "매도 성공 (USD -> KRW)",
                        value = """
                            {
                              "code": "OK",
                              "message": "정상적으로 처리되었습니다.",
                              "returnObject": {
                                "id": 2,
                                "fromAmount": 133.00,
                                "fromCurrency": "USD",
                                "toAmount": 176890,
                                "toCurrency": "KRW",
                                "tradeRate": 1330.00,
                                "dateTime": "2026-04-28T10:01:00"
                              }
                            }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 유효성 오류 (forexAmount ≤ 0, 필수 필드 누락 등)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "VALIDATION_ERROR",
                    value = """
                        {
                          "code": "VALIDATION_ERROR",
                          "message": "forexAmount 는 0보다 커야 합니다."
                        }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "지원하지 않는 통화 코드 (USD/JPY/CNY/EUR/KRW 외)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_CURRENCY",
                    value = """
                        {
                          "code": "INVALID_CURRENCY",
                          "message": "지원하지 않는 통화 코드입니다: XYZ"
                        }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 주문 요청 (동일 통화, KRW 미포함 외화 <-> 외화 주문)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "INVALID_ORDER_REQUEST - 동일 통화",
                        value = """
                            {
                              "code": "INVALID_ORDER_REQUEST",
                              "message": "통화(from-to)는 같을 수 없습니다."
                            }
                        """
                    ),
                    @ExampleObject(
                        name = "INVALID_ORDER_REQUEST - 외화 <-> 외화",
                        value = """
                            {
                              "code": "INVALID_ORDER_REQUEST",
                              "message": "매수 또는 매도 주문 중 하나여야 합니다."
                            }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "환율 정보 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "EXCHANGE_RATE_NOT_FOUND",
                    value = """
                        {
                          "code": "EXCHANGE_RATE_NOT_FOUND",
                          "message": "환율 정보를 찾을 수 없습니다: USD"
                        }
                    """
                )
            )
        )
    })
    @PostMapping
    ApiResponse<OrderResponse> createOrder(
        @Valid @RequestBody OrderRequest request
    );

    @Operation(
        summary = "주문 목록 조회",
        description = "전체 주문 내역을 최신순으로 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = OrderListResponse.class),
            examples = @ExampleObject(
                name = "주문 목록 조회 성공",
                value = """
                    {
                      "code": "OK",
                      "message": "정상적으로 처리되었습니다.",
                      "returnObject": {
                        "orderList": [
                          {
                            "id": 2,
                            "fromAmount": 133.00,
                            "fromCurrency": "USD",
                            "toAmount": 176890,
                            "toCurrency": "KRW",
                            "tradeRate": 1330.00,
                            "dateTime": "2026-04-28T10:02:00"
                          },
                          {
                            "id": 1,
                            "fromAmount": 294000,
                            "fromCurrency": "KRW",
                            "toAmount": 200.00,
                            "toCurrency": "USD",
                            "tradeRate": 1470.00,
                            "dateTime": "2026-04-28T10:01:00"
                          }
                        ]
                      }
                    }
                """
            )
        )
    )
    @GetMapping("/list")
    ApiResponse<OrderListResponse> getOrderList();
}

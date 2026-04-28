package com.ch.switchwon.presentation.web.api;

import com.ch.switchwon.presentation.web.dto.ApiResponse;
import com.ch.switchwon.presentation.web.dto.ExchangeRateListResponse;
import com.ch.switchwon.presentation.web.dto.ExchangeRateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "환율", description = "실시간 환율 조회 API")
@RequestMapping("/exchange-rate")
public interface ExchangeRateApiContract {

    @Operation(
        summary = "전체 통화 최신 환율 조회",
        description = "통화의 최신 환율을 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = ExchangeRateListResponse.class),
            examples = @ExampleObject(
                name = "전체 환율 조회 성공",
                value = """
                    {
                      "code": "OK",
                      "message": "정상적으로 처리되었습니다.",
                      "returnObject": {
                        "exchangeRateList": [
                          {
                            "currency": "USD",
                            "buyRate": 1470.00,
                            "tradeStanRate": 1400.00,
                            "sellRate": 1330.00,
                            "dateTime": "2026-04-28T10:01:00"
                          },
                          {
                            "currency": "JPY",
                            "buyRate": 945.00,
                            "tradeStanRate": 900.00,
                            "sellRate": 855.00,
                            "dateTime": "2026-04-28T10:01:00"
                          },
                          {
                            "currency": "CNY",
                            "buyRate": 199.50,
                            "tradeStanRate": 190.00,
                            "sellRate": 180.50,
                            "dateTime": "2026-04-28T10:01:00"
                          },
                          {
                            "currency": "EUR",
                            "buyRate": 1575.00,
                            "tradeStanRate": 1500.00,
                            "sellRate": 1425.00,
                            "dateTime": "2026-04-28T10:01:00"
                          }
                        ]
                      }
                    }
                """
            )
        )
    )
    @GetMapping("/latest")
    ApiResponse<ExchangeRateListResponse> getLatestRates();

    @Operation(
        summary = "특정 통화 최신 환율 조회",
        description = "지정한 통화(USD/JPY/CNY/EUR)의 최신 환율을 반환합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                schema = @Schema(implementation = ExchangeRateResponse.class),
                examples = @ExampleObject(
                    name = "USD 환율 조회 성공",
                    value = """
                        {
                          "code": "OK",
                          "message": "정상적으로 처리되었습니다.",
                          "returnObject": {
                            "currency": "USD",
                            "buyRate": 1470.00,
                            "tradeStanRate": 1400.00,
                            "sellRate": 1330.00,
                            "dateTime": "2026-04-28T10:01:00"
                          }
                        }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "지원하지 않는 통화 코드 (USD/JPY/CNY/EUR 외)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_CURRENCY",
                    value = """
                        {
                          "code": "INVALID_CURRENCY",
                          "message": "지원하지 않는 통화 코드입니다: ABC"
                        }
                    """
                )
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
    @GetMapping("/latest/{currency}")
    ApiResponse<ExchangeRateResponse> getLatestRate(
        @PathVariable @Parameter(description = "통화 코드 (USD / JPY / CNY / EUR)", example = "USD") String currency
    );
}

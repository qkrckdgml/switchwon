# [Switchwon 백엔드 개발자 과제]
## 실시간 환율 기반 외환 주문 시스템 - 박창희

---

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5
- **Database**: H2

---

## API 명세

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 환율 API

| Method | 경로 | 설명 |
|--------|------|------|
| GET | `/exchange-rate/latest` | 전체 통화 최신 환율 조회 |
| GET | `/exchange-rate/latest/{currency}` | 특정 통화 최신 환율 조회 |

### 주문 API

| Method | 경로 | 설명 |
|--------|------|------|
| POST | `/order` | 외화 주문 생성 (매수/매도) |
| GET | `/order/list` | 전체 주문 목록 조회 (최신순) |

---

## 공통 응답 구조

### 성공

```json
{
  "code": "OK",
  "message": "정상적으로 처리되었습니다.",
  "returnObject": { ... }
}
```

### 오류

```json
{
  "code": "ERROR_CODE",
  "message": "오류 메시지"
}
```

---

## API 상세

### GET `/exchange-rate/latest`

전체 4개 통화의 최신 환율을 반환합니다.

**Response 예시**

```json
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
      }
    ]
  }
}
```

---

### GET `/exchange-rate/latest/{currency}`

**Path Variable**

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| currency | 통화 코드 (대소문자 무관) | `USD`, `jpy` |

**Response 예시**

```json
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
```

---

### POST `/order`

**Request Body**

```json
{
  "forexAmount": 200,
  "fromCurrency": "KRW",
  "toCurrency": "USD"
}
```

| 필드 | 타입 | 필수 | 설명              |
|------|------|------|-----------------|
| forexAmount | number | Y | 외화 기준 금액 (0 초과) |
| fromCurrency | string | Y | 통화 (from)       |
| toCurrency | string | Y | 통화 (to)         |

**Case A: 매수 (KRW -> 외화)** — buyRate 적용

```json
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
```

**Case B: 매도 (외화 -> KRW)** — sellRate 적용

```json
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
```

---

### GET `/order/list`

전체 주문 내역을 최신순으로 반환합니다.

**Response 예시**

```json
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
```

---

## 오류 코드

| HTTP | code | 설명                                            |
|------|------|-----------------------------------------------|
| 400 | `VALIDATION_ERROR` | 입력값 유효성 검사 실패 (forexAmount ≤ 0, 필수 필드 누락 등)   |
| 400 | `INVALID_CURRENCY` | 지원하지 않는 통화 코드 (USD / JPY / CNY / EUR / KRW 외) |
| 400 | `INVALID_ORDER_REQUEST` | 잘못된 주문 요청 (동일 통화, KRW 미포함 외화 <-> 외화 주문)       |
| 400 | `INVALID_REQUEST_BODY` | 요청 본문 형식 오류                                   |
| 404 | `EXCHANGE_RATE_NOT_FOUND` | 해당 통화의 환율 정보 없음                               |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류                                      |

---

## 비즈니스 규칙

### 환율

| 규칙 | 내용 |
|------|------|
| 갱신 주기 | 1분 단위 스케줄러 자동 갱신 |
| 매입율 (buyRate) | 매매기준율 × 1.05 |
| 매도율 (sellRate) | 매매기준율 × 0.95 |
| 정밀도 | 소수점 둘째 자리 반올림 |
| JPY 단위 | 100엔 기준 환산 |

### 주문

| 규칙 | 내용                         |
|------|----------------------------|
| 매수 | KRW -> 외화, buyRate 적용        |
| 매도 | 외화 -> KRW, sellRate 적용       |
| forexAmount | 항상 외화 기준 금액                |
| KRW 환산 금액 | 소수점 이하 버림(Floor)           |
| 제한 | 동일 통화 주문 불가, 외화 <-> 외화 주문 불가 |

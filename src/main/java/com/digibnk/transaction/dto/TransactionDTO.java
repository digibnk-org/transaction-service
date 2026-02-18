package com.digibnk.transaction.dto;

import com.digibnk.transaction.enums.TransactionStatus;
import com.digibnk.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private Long id;
    private String reference;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private Long sourceAccountId;
    private Long targetAccountId;
    private LocalDateTime createdAt;
}

package com.digibnk.transaction.event;

import com.digibnk.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {

    private Long transactionId;
    private String reference;
    private TransactionType type;
    private BigDecimal amount;
    private Long sourceAccountId;
    private Long targetAccountId;
    private String status;
    private LocalDateTime occurredAt;
}

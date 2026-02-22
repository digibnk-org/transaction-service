package com.digibnk.transaction.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private String status;
}

package com.digibnk.transaction.client;

import com.digibnk.common.dto.BaseResponse;
import com.digibnk.transaction.client.dto.AccountResponse;
import com.digibnk.transaction.client.dto.BalanceUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountFeignClientFallbackFactory implements FallbackFactory<AccountFeignClient> {

    @Override
    public AccountFeignClient create(Throwable cause) {
        log.error("AccountFeignClient fallback triggered. Cause: {}", cause.getMessage());

        return new AccountFeignClient() {

            @Override
            public BaseResponse<AccountResponse> getAccountById(Long id) {
                log.warn("Fallback: getAccountById({}) — account-service unreachable", id);
                throw new RuntimeException(
                        "Account service is currently unavailable. " +
                        "Cannot fetch account [" + id + "]. Cause: " + cause.getMessage());
            }

            @Override
            public BaseResponse<AccountResponse> updateBalance(Long id, BalanceUpdateRequest request) {
                log.warn("Fallback: updateBalance({}, {}) — account-service unreachable", id, request.getAmount());
                throw new RuntimeException(
                        "Account service is currently unavailable. " +
                        "Cannot update balance for account [" + id + "]. Cause: " + cause.getMessage());
            }
        };
    }
}

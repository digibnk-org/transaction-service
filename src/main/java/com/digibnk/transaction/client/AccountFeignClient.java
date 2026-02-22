package com.digibnk.transaction.client;

import com.digibnk.common.dto.BaseResponse;
import com.digibnk.transaction.client.dto.AccountResponse;
import com.digibnk.transaction.client.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", path = "/api/v1/accounts")
public interface AccountFeignClient {

    @GetMapping("/{id}")
    BaseResponse<AccountResponse> getAccountById(@PathVariable("id") Long id);

    @PatchMapping("/{id}/balance")
    BaseResponse<AccountResponse> updateBalance(@PathVariable("id") Long id,
                                                @RequestBody BalanceUpdateRequest request);
}

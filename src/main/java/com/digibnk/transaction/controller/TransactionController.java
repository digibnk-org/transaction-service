package com.digibnk.transaction.controller;

import com.digibnk.common.dto.BaseResponse;
import com.digibnk.transaction.dto.CreateTransactionDTO;
import com.digibnk.transaction.dto.TransactionDTO;
import com.digibnk.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionDTO>> createTransaction(@Valid @RequestBody CreateTransactionDTO createTransactionDTO) {
        TransactionDTO transaction = transactionService.createTransaction(createTransactionDTO);
        return new ResponseEntity<>(BaseResponse.success(transaction, "Transaction created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(transactionService.getTransactionById(id)));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BaseResponse<TransactionDTO>> getTransactionByReference(@PathVariable String reference) {
        return ResponseEntity.ok(BaseResponse.success(transactionService.getTransactionByReference(reference)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<TransactionDTO>>> getAllTransactions() {
        return ResponseEntity.ok(BaseResponse.success(transactionService.getAllTransactions()));
    }
}

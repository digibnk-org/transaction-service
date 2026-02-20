package com.digibnk.transaction.service;

import com.digibnk.transaction.dto.CreateTransactionDTO;
import com.digibnk.transaction.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    TransactionDTO createTransaction(CreateTransactionDTO createTransactionDTO);
    TransactionDTO getTransactionById(Long id);
    TransactionDTO getTransactionByReference(String reference);
    List<TransactionDTO> getAllTransactions();
}

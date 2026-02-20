package com.digibnk.transaction.service.impl;

import com.digibnk.common.exception.ResourceNotFoundException;
import com.digibnk.transaction.dto.CreateTransactionDTO;
import com.digibnk.transaction.dto.TransactionDTO;
import com.digibnk.transaction.entity.Transaction;
import com.digibnk.transaction.enums.TransactionStatus;
import com.digibnk.transaction.mapper.TransactionMapper;
import com.digibnk.transaction.repository.TransactionRepository;
import com.digibnk.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO createTransaction(CreateTransactionDTO createTransactionDTO) {
        log.info("Creating transaction of type: {}", createTransactionDTO.getType());
        
        // TODO: Validate accounts via Account Service (Feign Client)
        // TODO: Perform balance check and update via Account Service

        Transaction transaction = Transaction.builder()
                .amount(createTransactionDTO.getAmount())
                .transactionType(createTransactionDTO.getType())
                .sourceAccountId(createTransactionDTO.getSourceAccountId())
                .targetAccountId(createTransactionDTO.getTargetAccountId())
                .status(TransactionStatus.PENDING) // Should be COMPLETED if sync, or PENDING if async/saga
                .reference(UUID.randomUUID().toString())
                .build();

        transaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(transaction);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    @Override
    public TransactionDTO getTransactionByReference(String reference) {
        return transactionRepository.findByReference(reference)
                .map(transactionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}

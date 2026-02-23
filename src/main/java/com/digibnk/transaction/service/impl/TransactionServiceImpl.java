package com.digibnk.transaction.service.impl;

import com.digibnk.common.exception.ResourceNotFoundException;
import com.digibnk.transaction.client.AccountFeignClient;
import com.digibnk.transaction.client.dto.AccountResponse;
import com.digibnk.transaction.client.dto.BalanceUpdateRequest;
import com.digibnk.transaction.dto.CreateTransactionDTO;
import com.digibnk.transaction.dto.TransactionDTO;
import com.digibnk.transaction.entity.Transaction;
import com.digibnk.transaction.enums.TransactionStatus;
import com.digibnk.transaction.enums.TransactionType;
import com.digibnk.transaction.event.TransactionCreatedEvent;
import com.digibnk.transaction.kafka.TransactionEventProducer;
import com.digibnk.transaction.mapper.TransactionMapper;
import com.digibnk.transaction.repository.TransactionRepository;
import com.digibnk.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final AccountFeignClient accountFeignClient;
    private final TransactionEventProducer eventProducer;

    @Override
    @Transactional
    public TransactionDTO createTransaction(CreateTransactionDTO dto) {
        log.info("Creating transaction of type: {}", dto.getType());

        AccountResponse sourceAccount = null;
        AccountResponse targetAccount = null;

        if (dto.getSourceAccountId() != null) {
            sourceAccount = fetchAccount(dto.getSourceAccountId());
            validateAccountActive(sourceAccount);
        }

        if (dto.getTargetAccountId() != null) {
            targetAccount = fetchAccount(dto.getTargetAccountId());
            validateAccountActive(targetAccount);
        }

        switch (dto.getType()) {
            case WITHDRAWAL -> {
                if (sourceAccount == null) {
                    throw new IllegalArgumentException("Source account is required for WITHDRAWAL");
                }
                checkSufficientFunds(sourceAccount, dto.getAmount());
            }
            case DEPOSIT -> {
                if (targetAccount == null) {
                    throw new IllegalArgumentException("Target account is required for DEPOSIT");
                }
            }
            case TRANSFER -> {
                if (sourceAccount == null || targetAccount == null) {
                    throw new IllegalArgumentException("Both source and target accounts are required for TRANSFER");
                }
                checkSufficientFunds(sourceAccount, dto.getAmount());
            }
        }

        Transaction transaction = Transaction.builder()
                .amount(dto.getAmount())
                .transactionType(dto.getType())
                .sourceAccountId(dto.getSourceAccountId())
                .targetAccountId(dto.getTargetAccountId())
                .status(TransactionStatus.PENDING)
                .reference(UUID.randomUUID().toString())
                .build();
        transaction = transactionRepository.save(transaction);

        try {
            applyBalanceChanges(dto.getType(), dto.getAmount(),
                    dto.getSourceAccountId(), dto.getTargetAccountId());

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);
            log.info("Transaction {} completed successfully", transaction.getReference());

            // Publish event AFTER commit
            publishTransactionEvent(transaction);

        } catch (Exception ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Transaction {} failed: {}", transaction.getReference(), ex.getMessage());
            throw ex;
        }

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

    // ── helpers ────────────────────────────────────────────────────────────────

    private AccountResponse fetchAccount(Long accountId) {
        try {
            var response = accountFeignClient.getAccountById(accountId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Account not found with id: " + accountId);
            }
            return response.getData();
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch account {}: {}", accountId, ex.getMessage());
            throw new RuntimeException("Unable to verify account " + accountId + ": " + ex.getMessage(), ex);
        }
    }

    private void validateAccountActive(AccountResponse account) {
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("Account " + account.getId() + " is not active (status: " + account.getStatus() + ")");
        }
    }

    private void checkSufficientFunds(AccountResponse account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in account " + account.getId()
                    + ". Available: " + account.getBalance() + ", Required: " + amount);
        }
    }

    private void applyBalanceChanges(TransactionType type, BigDecimal amount,
                                     Long sourceAccountId, Long targetAccountId) {
        switch (type) {
            case WITHDRAWAL -> {
                accountFeignClient.updateBalance(sourceAccountId,
                        new BalanceUpdateRequest(amount.negate()));
            }
            case DEPOSIT -> {
                accountFeignClient.updateBalance(targetAccountId,
                        new BalanceUpdateRequest(amount));
            }
            case TRANSFER -> {
                accountFeignClient.updateBalance(sourceAccountId,
                        new BalanceUpdateRequest(amount.negate()));
                accountFeignClient.updateBalance(targetAccountId,
                        new BalanceUpdateRequest(amount));
            }
        }
    }

    private void publishTransactionEvent(Transaction transaction) {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .type(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .sourceAccountId(transaction.getSourceAccountId())
                .targetAccountId(transaction.getTargetAccountId())
                .status(transaction.getStatus().name())
                .occurredAt(LocalDateTime.now())
                .build();
        eventProducer.publishTransactionCreated(event);
    }
}

package com.digibnk.transaction.mapper;

import com.digibnk.transaction.dto.TransactionDTO;
import com.digibnk.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(source = "transactionType", target = "type")
    TransactionDTO toDTO(Transaction transaction);
}

package com.scheduleengine.payment.repository;

import com.scheduleengine.payment.domain.Transaction;
import com.scheduleengine.payment.domain.Transaction.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByPartyTypeAndPartyId(PartyType partyType, Long partyId);

  List<Transaction> findByPartyTypeAndPartyIdAndStatus(PartyType partyType, Long partyId, Transaction.Status status);

  List<Transaction> findByPartyTypeAndPartyIdAndDateBetween(PartyType partyType, Long partyId, LocalDate start, LocalDate end);
}

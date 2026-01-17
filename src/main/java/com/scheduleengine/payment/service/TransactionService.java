package com.scheduleengine.payment.service;

import com.scheduleengine.payment.domain.Transaction;
import com.scheduleengine.payment.repository.TransactionRepository;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.player.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final TeamService teamService;
    private final PlayerService playerService;

    public TransactionService(TransactionRepository repository,
                            TeamService teamService,
                            PlayerService playerService) {
        this.repository = repository;
        this.teamService = teamService;
        this.playerService = playerService;
    }

    public List<Transaction> findByParty(Transaction.PartyType type, Long partyId) {
        List<Transaction> transactions = repository.findByPartyTypeAndPartyId(type, partyId);
        populatePartyNames(transactions);
        return transactions;
    }

    public List<Transaction> findAll() {
        List<Transaction> transactions = repository.findAll();
        populatePartyNames(transactions);
        return transactions;
    }

    /**
     * Populate the transient partyName field with actual Team or Player name
     */
    private void populatePartyNames(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            if (tx.getPartyType() == Transaction.PartyType.TEAM) {
                teamService.findById(tx.getPartyId()).ifPresent(team -> {
                    tx.setPartyName(team.getName());
                    tx.setTeamName(team.getName());
                    if (team.getLeague() != null) {
                        tx.setLeagueName(team.getLeague().getName());
                    }
                });
            } else if (tx.getPartyType() == Transaction.PartyType.PLAYER) {
                playerService.findById(tx.getPartyId()).ifPresent(player -> {
                    tx.setPartyName(player.getFullName());
                    tx.setPlayerName(player.getFullName());
                    if (player.getTeam() != null) {
                        tx.setTeamName(player.getTeam().getName());
                        if (player.getTeam().getLeague() != null) {
                            tx.setLeagueName(player.getTeam().getLeague().getName());
                        }
                    }
                });
            }
            if (tx.getPartyName() == null) {
                tx.setPartyName("Unknown (" + tx.getPartyId() + ")");
            }
        }
    }

    @Transactional
    public Transaction save(Transaction tx) {
        validate(tx);
        return repository.save(tx);
    }

    public void validate(Transaction tx) {
        if (tx.getPartyType() == null) throw new IllegalArgumentException("partyType required");
        if (tx.getPartyId() == null) throw new IllegalArgumentException("partyId required");
        if (tx.getCategory() == null) throw new IllegalArgumentException("category required");
        if (tx.getDate() == null) throw new IllegalArgumentException("date required");
        if (tx.getAmount() == null || tx.getAmount() <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (tx.getStatus() == null) throw new IllegalArgumentException("status required");
    }

    @Transactional
    public void deleteById(Long id) { repository.deleteById(id); }

    // Simple CSV import: header row expected with columns: partyType,partyId,category,date,amount,status,notes
    @Transactional
    public List<Transaction> importCsv(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String header = br.readLine(); // skip header
        List<Transaction> imported = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 6) continue; // minimal fields
            Transaction tx = new Transaction();
            tx.setPartyType(Transaction.PartyType.valueOf(parts[0].trim().toUpperCase()));
            tx.setPartyId(Long.parseLong(parts[1].trim()));
            tx.setCategory(Transaction.Category.valueOf(parts[2].trim().toUpperCase()));
            tx.setDate(LocalDate.parse(parts[3].trim()));
            tx.setAmount(Double.parseDouble(parts[4].trim()));
            tx.setStatus(Transaction.Status.valueOf(parts[5].trim().toUpperCase()));
            if (parts.length > 6) tx.setNotes(parts[6].trim());
            validate(tx);
            imported.add(repository.save(tx));
        }
        return imported;
    }

    public String exportCsv(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("partyType,partyId,category,date,amount,status,notes\n");
        for (Transaction tx : transactions) {
            sb.append(tx.getPartyType()).append(',')
              .append(tx.getPartyId()).append(',')
              .append(tx.getCategory()).append(',')
              .append(tx.getDate()).append(',')
              .append(tx.getAmount()).append(',')
              .append(tx.getStatus()).append(',')
              .append(tx.getNotes() == null ? "" : tx.getNotes().replace(","," "))
              .append('\n');
        }
        return sb.toString();
    }
}

package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    // given method to save a user record
    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    // method to save a transaction record
    public void saveTransaction(TransactionRecord transactionRecord) {
        transactionRepository.save(transactionRecord);
    }

    // method to check if a user with a given id exists
    public boolean isUserExist(long id){
        return userRepository.existsById(id);
    }

    // method to return the UserRecord object given a user id
    public UserRecord getUserRecord(long id){
        return userRepository.findById(id);
    }
}

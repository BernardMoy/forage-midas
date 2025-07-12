package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    private final DatabaseConduit databaseConduit;

    // create database instance
    public Listener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    // listener to listen for the topic in application.yml file
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "listener")

    // listen for transaction objects
    public void listen(Transaction transaction) {
        System.out.println("Received transaction: " + transaction.toString());

        // get the sender and recipient id
        long senderID = transaction.getSenderId();
        long recipientID = transaction.getRecipientId();
        float amount = transaction.getAmount();

        // determine if the sender and recipient id exists
        if (!databaseConduit.isUserExist(senderID) || !databaseConduit.isUserExist(recipientID)) {
            System.out.println("Transaction declined: User does not exist");
            return;   // exit
        }

        // determine if the remaining balance is enough for the transaction
        UserRecord senderUser = databaseConduit.getUserRecord(senderID);
        UserRecord recipientUser = databaseConduit.getUserRecord(recipientID);

        if (senderUser.getBalance() < amount) {
            System.out.println("Transaction declined: User does not have enough balance");
            return;
        }

        // if valid, record the transaction to the database
        TransactionRecord transactionRecord = new TransactionRecord(senderID, recipientID, amount);
        databaseConduit.saveTransaction(transactionRecord);

        // and adjust the remaining balance of the sender and recipient
        senderUser.setBalance(senderUser.getBalance() - amount);
        recipientUser.setBalance(recipientUser.getBalance() + amount);
        databaseConduit.save(senderUser);
        databaseConduit.save(recipientUser);

        // print final debug message
        if (senderUser.getName().equals("waldorf")){
            System.out.println("Balance of waldorf: " + senderUser.getBalance());
        }
        if (recipientUser.getName().equals("waldorf")){
            System.out.println("Balance of waldorf: " + recipientUser.getBalance());
        }

    }
}

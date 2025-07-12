package com.jpmc.midascore;
import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Listener {

    private final DatabaseConduit databaseConduit;
    private RestTemplate restTemplate;

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

        // get the incentive pay from the API
        // first run  java -jar transaction-incentive-api.jar  in /services
        // create rest template
        restTemplate = new RestTemplate();
        ResponseEntity<Incentive> response = restTemplate.postForEntity(
                "http://localhost:8080/incentive",     // post url
                transaction,         // transaction is the object (to be serialized) to be posted
                Incentive.class      // Incentive class (serializable) is the return type
        );

        // get the incentive amount (Default to 0)
        Incentive incentive = response.getBody();
        float incentiveAmount = incentive == null ? 0 : incentive.getAmount();
        System.out.println("Incentive amount: " + incentiveAmount);


        // and adjust the remaining balance of the sender and recipient
        senderUser.setBalance(senderUser.getBalance() - amount);
        recipientUser.setBalance(recipientUser.getBalance() + amount + incentiveAmount);
        databaseConduit.save(senderUser);
        databaseConduit.save(recipientUser);

        // print final debug message for task 3
        if (senderUser.getName().equals("waldorf")){
            System.out.println("Balance of waldorf after sent: " + senderUser.getBalance());
        }
        if (recipientUser.getName().equals("waldorf")){
            System.out.println("Balance of waldorf after received: " + recipientUser.getBalance());
        }

        // for task 4
        if (senderUser.getName().equals("wilbur")){
            System.out.println("Balance of wilbur after sent: " + senderUser.getBalance());
        }
        if (recipientUser.getName().equals("wilbur")){
            System.out.println("Balance of wilbur after received: " + recipientUser.getBalance());
        }

    }
}

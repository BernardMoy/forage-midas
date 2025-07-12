package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    private final DatabaseConduit databaseConduit;
    public BalanceController(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    // get method to return a balance(amount) object, given a user id
    @GetMapping("/balance")
    public Balance getBalance(@RequestParam long userId) {   // case sensitive "Id"
        UserRecord user = databaseConduit.getUserRecord(userId);
        // if user is null, return a balance of 0
        float balance = user == null ? 0.0f : user.getBalance();
        return new Balance(balance);
    }
}

package Server.utils;

import java.time.Instant;


public class Transaction {
   private final double winCoin;
   private Instant timestamp;
   
   public Transaction(double winCoin) {
      this.winCoin = winCoin;
      timestamp = Instant.now();

   }

   public double getWinCoin() {
      return this.winCoin;
   }
   public String getWinCoinAsString() {
      return Double.toString(winCoin);
   }


   public Instant getTimestamp() {
      return this.timestamp;
   }



}

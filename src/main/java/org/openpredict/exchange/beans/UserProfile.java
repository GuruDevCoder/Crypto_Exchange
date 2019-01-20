package org.openpredict.exchange.beans;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;

import java.util.function.IntFunction;

@Slf4j
public final class UserProfile {

    public final long uid;

    // symbol -> portfolio records
    private IntObjectHashMap<SymbolPortfolioRecord> portfolio = new IntObjectHashMap<>();

    // transactionId -> amount
    public LongLongHashMap externalTransactions = new LongLongHashMap();

    // collected from accounts
    public long balance = 0L;

    // collected from portfolio
    // TODO change to cached guaranteed available funds based on current position?
    // public long fastMargin = 0L;

    public long commandsCounter = 0L;

    public UserProfile(long uid) {
        //log.debug("New {}", uid);
        this.uid = uid;
    }

    public SymbolPortfolioRecord getOrCreatePortfolio(int symbol) {
        SymbolPortfolioRecord record = portfolio.get(symbol);
        if (record == null) {
            record = new SymbolPortfolioRecord(symbol, uid);
            portfolio.put(symbol, record);
        }
        return record;
    }

    public long getMarginMinusDeposit(IntFunction<CoreSymbolSpecification> symbolSpecSupplier, int ignoreDepositForSymbol) {
        long profit = 0L;
        for (SymbolPortfolioRecord r : portfolio) {
            final CoreSymbolSpecification spec = symbolSpecSupplier.apply(r.symbol);
            profit += r.estimateProfit(spec);
            if (ignoreDepositForSymbol != r.symbol) {
                profit -= r.calculateRequiredDeposit(spec);
            }
        }
        return profit;
    }

    public void removeRecordIfEmpty(SymbolPortfolioRecord record) {
        if (record.isEmpty()) {
            balance += record.profit;
            portfolio.removeKey(record.symbol);
        }
    }

//    public void clear() {
////        log.debug("{} Portfolio size: {}, commands {}, fastBalance: {}", uid, portfolio.size(), commandsCounter.longValue(), fastBalance);
//        portfolio.forEach(SymbolPortfolioRecord::reset);
//        commandsCounter = 0;
//        // TODO clear margin?
//    }


    @Override
    public String toString() {
        return "UserProfile{" +
                "uid=" + uid +
                ", portfolios=" + portfolio.size() +
                ", balance=" + balance +
                ", commandsCounter=" + commandsCounter +
                '}';
    }
}

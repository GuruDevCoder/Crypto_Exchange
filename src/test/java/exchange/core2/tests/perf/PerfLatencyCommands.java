/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.tests.perf;

import exchange.core2.tests.util.ExchangeTestContainer;
import exchange.core2.tests.util.TestConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import static exchange.core2.tests.util.LatencyTestsModule.individualLatencyTest;

@Slf4j
public final class PerfLatencyCommands {

    /**
     * - one symbol (margin mode)
     * - ~1K active users (2K currency accounts)
     * - 1K pending limit-orders (in one order book)
     * 6-threads CPU can run this test
     */
    @Test
    public void testLatencyMargin() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(2 * 1024, 1, 1, msgsInGroup, null),
                1_000_000,
                1_000,
                2_000,
                TestConstants.CURRENCIES_FUTURES,
                1,
                ExchangeTestContainer.AllowedSymbolTypes.FUTURES_CONTRACT,
                false);
    }

    @Test
    public void testLatencyMarginHugeIoc() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(2 * 1024, 1, 1, msgsInGroup, null),
                1_000_000,
                1_000,
                2_000,
                TestConstants.CURRENCIES_FUTURES,
                1,
                ExchangeTestContainer.AllowedSymbolTypes.FUTURES_CONTRACT,
                true);
    }


    /**
     * - one symbol (exchange mode)
     * - ~1K active users (2K currency accounts)
     * - 1K pending limit-orders (in one order book)
     * 6-threads CPU can run this test
     */
    @Test
    public void testLatencyExchange() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(2 * 1024, 1, 1, msgsInGroup, null),
                1_000_000,
                1_000,
                2_000,
                TestConstants.CURRENCIES_EXCHANGE,
                1,
                ExchangeTestContainer.AllowedSymbolTypes.CURRENCY_EXCHANGE_PAIR,
                false);
    }

    @Test
    public void testLatencyExchangeHugeIoc() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(2 * 1024, 1, 1, msgsInGroup, null),
                1_000_000,
                1_000,
                2_000,
                TestConstants.CURRENCIES_EXCHANGE,
                1,
                ExchangeTestContainer.AllowedSymbolTypes.CURRENCY_EXCHANGE_PAIR,
                true);
    }


    /**
     * - 1M active users (3M currency accounts)
     * - 1M pending limit-orders
     * - 1M+ messages per second throughput
     * - 100K symbols
     * - less than 1 millisecond 99.99% latency
     * 12-threads CPU and 32GiB RAM is required for running this test in 2+4 configuration.
     */
    @Test
    public void testLatencyMultiSymbolMedium() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(32 * 1024, 4, 2, msgsInGroup, null),
                3_000_000,
                1_000_000,
                3_300_000,
                TestConstants.ALL_CURRENCIES,
                100_000,
                ExchangeTestContainer.AllowedSymbolTypes.BOTH,
                false);
    }

    @Test
    public void testLatencyMultiSymbolMediumHugeIOC() {
        individualLatencyTest(msgsInGroup -> new ExchangeTestContainer(32 * 1024, 4, 2, msgsInGroup, null),
                3_000_000,
                1_000_000,
                3_300_000,
                TestConstants.ALL_CURRENCIES,
                100_000,
                ExchangeTestContainer.AllowedSymbolTypes.BOTH,
                true);
    }


    /**
     * - 10M active users (33M currency accounts)
     * - 30M pending limit-orders
     * - 200K symbols
     * - 1M+ messages per second throughput
     * 12-threads CPU and 32GiB RAM is required for running this test in 2+4 configuration.
     */
    @Test
    @Ignore
    public void testLatencyMultiSymbolLarge() {
        individualLatencyTest(
                msgsInGroup -> new ExchangeTestContainer(64 * 1024, 4, 2, msgsInGroup, null),
                40_000_000,
                30_000_000,
                33_000_000,
                TestConstants.ALL_CURRENCIES,
                200_000,
                ExchangeTestContainer.AllowedSymbolTypes.BOTH,
                false);
    }

}
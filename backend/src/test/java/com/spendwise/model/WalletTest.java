package com.spendwise.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendwise.core.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletTest {

    private Wallet wallet(int id, double balance) {
        return new Wallet(id, 1, "Dompet " + id, WalletType.CASH, balance);
    }

    @Test
    void depositIncreasesBalance() {
        Wallet w = wallet(1, 100);
        w.deposit(50.5);
        assertEquals(150.5, w.getBalance(), 0.001);
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        assertThrows(BusinessException.class, () -> wallet(1, 100).deposit(0));
        assertThrows(BusinessException.class, () -> wallet(1, 100).deposit(-5));
    }

    @Test
    void withdrawReturnsFalseWhenInsufficientAndKeepsBalance() {
        Wallet w = wallet(1, 100);
        assertFalse(w.withdraw(100.01));
        assertEquals(100, w.getBalance(), 0.001);
    }

    @Test
    void withdrawAllowsExactBalance() {
        Wallet w = wallet(1, 100);
        assertTrue(w.withdraw(100));
        assertEquals(0, w.getBalance(), 0.001);
    }

    @Test
    void transferMovesMoneyBetweenWallets() {
        Wallet a = wallet(1, 300);
        Wallet b = wallet(2, 50);
        assertTrue(a.transferTo(b, 200));
        assertEquals(100, a.getBalance(), 0.001);
        assertEquals(250, b.getBalance(), 0.001);
    }

    @Test
    void transferFailsWhenInsufficientWithoutChangingTarget() {
        Wallet a = wallet(1, 100);
        Wallet b = wallet(2, 50);
        assertFalse(a.transferTo(b, 200));
        assertEquals(100, a.getBalance(), 0.001);
        assertEquals(50, b.getBalance(), 0.001);
    }

    @Test
    void transferToSameWalletIsRejected() {
        Wallet a = wallet(1, 100);
        assertThrows(BusinessException.class, () -> a.transferTo(a, 10));
        assertThrows(BusinessException.class, () -> a.transferTo(null, 10));
    }

    @Test
    void balanceIsRoundedToTwoDecimals() {
        Wallet w = wallet(1, 0);
        w.deposit(0.1);
        w.deposit(0.2);
        assertEquals(0.3, w.getBalance(), 0.0);
    }

    @Test
    void validateChecksNameAndType() {
        assertTrue(wallet(1, 0).validate());
        assertThrows(BusinessException.class,
                () -> new Wallet(1, " ", WalletType.BANK, 0).validate());
        assertThrows(BusinessException.class,
                () -> new Wallet(1, "BCA", null, 0).validate());
    }

    @Test
    void walletHasNoBalanceSetter() {
        for (var m : Wallet.class.getMethods()) {
            assertFalse("setBalance".equals(m.getName()));
        }
    }

    @Test
    void jsonRequestAppliesInitialBalanceButIgnoresBalanceField() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Wallet w = mapper.readValue(
                "{\"name\":\"GoPay\",\"walletType\":\"E_WALLET\",\"initialBalance\":100000,\"balance\":999}",
                Wallet.class);
        assertEquals("GoPay", w.getName());
        assertEquals(WalletType.E_WALLET, w.getWalletType());
        assertEquals(100000, w.getBalance(), 0.001);
    }

    @Test
    void memberTotalBalanceSumsWallets() {
        Member m = new Member("budi", "x", "Budi", "b@mail.com");
        m.addWallet(wallet(1, 150000));
        m.addWallet(wallet(2, 990000));
        assertEquals(1140000, m.getTotalBalance(), 0.001);
        assertTrue(m.removeWallet(1));
        assertFalse(m.removeWallet(99));
        assertEquals(990000, m.getTotalBalance(), 0.001);
    }
}

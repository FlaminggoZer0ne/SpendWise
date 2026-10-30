package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.core.ForbiddenException;
import com.spendwise.core.PasswordHasher;
import com.spendwise.core.UnauthorizedException;
import com.spendwise.dao.UserDAO;
import com.spendwise.dao.WalletDAO;
import com.spendwise.model.Member;
import com.spendwise.model.User;
import com.spendwise.model.Wallet;
import com.spendwise.model.WalletType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO;
    private final WalletDAO walletDAO;

    public AuthService(UserDAO userDAO, WalletDAO walletDAO) {
        this.userDAO = userDAO;
        this.walletDAO = walletDAO;
    }

    /** Selalu membuat MEMBER beserta dompet "Tunai" bersaldo 0 (F01 AC6). */
    @Transactional
    public Member register(String username, String password, String fullName, String email) {
        if (userDAO.existsByUsername(username)) {
            throw usernameTaken();
        }
        Member member = new Member(username, hashPassword(password), fullName.trim(), email.trim());
        try {
            userDAO.save(member);
        } catch (DuplicateKeyException e) {
            throw usernameTaken();
        }
        Wallet cash = new Wallet(member.getUserId(), "Tunai", WalletType.CASH, 0);
        walletDAO.save(cash);
        member.addWallet(cash);
        log.info("Member baru terdaftar: {}", username);
        return member;
    }

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null || !user.checkPassword(password)) {
            throw new UnauthorizedException("Username atau password salah");
        }
        if (!user.isActive()) {
            throw new ForbiddenException("Akun Anda dinonaktifkan. Hubungi admin.");
        }
        return user;
    }

    public boolean changePassword(int userId, String oldPw, String newPw) {
        User user = getUser(userId);
        user.changePassword(oldPw, newPw);
        return userDAO.update(user);
    }

    public User updateProfile(int userId, String fullName, String email) {
        User user = getUser(userId);
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        userDAO.update(user);
        return user;
    }

    /** Memuat ulang pengguna dari database (dipakai /auth/me dan penyegaran session). */
    public User getUser(int userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new UnauthorizedException("Silakan login terlebih dahulu");
        }
        return user;
    }

    private String hashPassword(String raw) {
        return PasswordHasher.hash(raw);
    }

    private BusinessException usernameTaken() {
        return new BusinessException(HttpStatus.CONFLICT, "Username sudah dipakai");
    }
}

package bank.core.models;

import java.util.List;
import java.util.UUID;

public class User {

    private final UUID userId;
    private String userNickname;
    private final List<UUID> allowedAccountIdentifiers;

    public User(String nickname, List<UUID> accountIdentifiers) {
        this.userId = UUID.randomUUID();
        this.userNickname = nickname;
        this.allowedAccountIdentifiers = accountIdentifiers;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public List<UUID> getAllowedAccountIdentifiers() {
        return allowedAccountIdentifiers;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId.toString() +
                ", userNickname='" + userNickname + '\'' +
                ", accountCount=" + allowedAccountIdentifiers.size() +
                '}';
    }
}
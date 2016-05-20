package com.sdw.ivana.inertiavectorrace;

import java.io.Serializable;

/**
 * Created by aurel on 5/20/2016.
 */
public class UserV1 implements Serializable{
    private String email;
    private String nickName;
    private String socialAccount;
    private boolean firstTime;

    public UserV1() {
    }

    public UserV1(String email, String nickName, String socialAccount, boolean firstTime) {
        this.email = email;
        this.nickName = nickName;
        this.socialAccount = socialAccount;
        this.firstTime = firstTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSocialAccount() {
        return socialAccount;
    }

    public void setSocialAccount(String socialAccount) {
        this.socialAccount = socialAccount;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }
}

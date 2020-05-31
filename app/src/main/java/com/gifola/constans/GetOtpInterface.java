package com.gifola.constans;

public interface GetOtpInterface {
    void onOtpReceived(String otp);
    void onOtpTimeout();
}

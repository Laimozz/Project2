package com.example.Project2.service;

import com.example.Project2.config.VnPayConfig;
import com.example.Project2.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnPayService {

    @Autowired
    private VnPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay cho 1 đơn hàng
     *
     * @param order     đơn hàng cần thanh toán
     * @param ipAddress IP của khách hàng
     * @return URL redirect sang VNPay
     */
    public String createPaymentUrl(Order order, String ipAddress) {
        String vnpTxnRef = order.getVnpTxnRef();
        // vnp_Amount: VNPay yêu cầu nhân 100 (không có phần thập phân)
        long amount = order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version",    "2.1.0");
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    vnPayConfig.getTmnCode());
        params.put("vnp_Amount",     String.valueOf(amount));
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     vnpTxnRef);
        params.put("vnp_OrderInfo",  "Thanh toan don hang #" + order.getId());
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr",     ipAddress);

        // Thời gian tạo giao dịch
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now().format(formatter);
        params.put("vnp_CreateDate", createDate);

        // Thời gian hết hạn (15 phút)
        String expireDate = LocalDateTime.now().plusMinutes(15).format(formatter);
        params.put("vnp_ExpireDate", expireDate);

        // Tạo query string (đã sort) và ký HMAC-SHA512
        String queryString = VnPayConfig.buildQueryString(params);
        String secureHash = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), queryString);

        return vnPayConfig.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" +
                URLEncoder.encode(secureHash, StandardCharsets.UTF_8);
    }

    /**
     * Xác thực callback từ VNPay
     *
     * @param params tất cả query params từ VNPay
     * @return true nếu checksum hợp lệ VÀ giao dịch thành công (vnp_ResponseCode=00)
     */
    public boolean validateCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Loại bỏ vnp_SecureHash và vnp_SecureHashType khỏi params trước khi verify
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        String queryString = VnPayConfig.buildQueryString(verifyParams);
        String calculatedHash = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), queryString);

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * Kiểm tra mã phản hồi từ VNPay
     * @return true nếu giao dịch thành công (vnp_ResponseCode = "00")
     */
    public boolean isPaymentSuccess(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"));
    }
}

package com.codeWithProjects.ecom.services.customer.razorpay;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RazorpayServiceImpl implements RazorpayService{
    private final RazorpayClient razorpayClient;

    @Autowired
    public RazorpayServiceImpl(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    @Override
    @Cacheable
    public String createOrder(Long amount, String receipt) throws RazorpayException, JSONException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1); // Auto capture payment

        Order order = razorpayClient.orders.create(orderRequest);
        return order.get("id");
    }
}

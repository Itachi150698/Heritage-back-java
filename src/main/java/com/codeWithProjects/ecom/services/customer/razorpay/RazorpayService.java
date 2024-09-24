package com.codeWithProjects.ecom.services.customer.razorpay;

import com.razorpay.RazorpayException;
import org.json.JSONException;

public interface RazorpayService {
    String createOrder(Long amount, String receipt) throws RazorpayException, JSONException;
}

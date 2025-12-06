package com.example.projectfilm.ui.user.booking;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

public class MomoPaymentTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "MomoPayment";
    private Context context;
    private int userId;
    private String momoAmount;

    public MomoPaymentTask(Context context, int userId, String momoAmount) {
        this.context = context;
        this.userId = userId;
        this.momoAmount = momoAmount;
    }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            String accessKey = "F8BBA842ECF85";
            String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
            String orderInfo = "Thanh toán đơn hàng";
            String partnerCode = "MOMO";
            String redirectUrl = "projectfilm://thanhtoanthanhcong";
            String ipnUrl = "https://momo.vn";
            String requestType = "captureWallet";
            String orderId = partnerCode + System.currentTimeMillis();
            String requestId = orderId;
            String extraData = "";
            boolean autoCapture = true;
            String lang = "vi";

            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + this.momoAmount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType ;
//                    "&autoCapture=" + autoCapture;

            String signature = hmacSHA256(rawSignature, secretKey);

            String jsonBody = "{"
                    + "\"partnerCode\":\"" + partnerCode + "\","
                    + "\"partnerName\":\"MoMoTest\","
                    + "\"storeId\":\"TestStore\","
                    + "\"requestId\":\"" + requestId + "\","
                    + "\"amount\":\"" + this.momoAmount + "\","
                    + "\"orderId\":\"" + orderId + "\","
                    + "\"orderInfo\":\"" + orderInfo + "\","
                    + "\"redirectUrl\":\"" + redirectUrl + "\","
                    + "\"ipnUrl\":\"" + ipnUrl + "\","
                    + "\"lang\":\"" + lang + "\","
                    + "\"requestType\":\"" + requestType + "\","
                    + "\"autoCapture\":" + autoCapture + ","
                    + "\"extraData\":\"" + extraData + "\","
                    + "\"signature\":\"" + signature + "\""
                    + "}";

            Log.d(TAG, "Raw Signature: " + rawSignature);
            Log.d(TAG, "Signature: " + signature);
            Log.d(TAG, "JSON Body: " + jsonBody);

            URL url = new URL("https://test-payment.momo.vn/v2/gateway/api/create");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            InputStream is = (status < HttpURLConnection.HTTP_BAD_REQUEST) ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder response = new StringBuilder();
            int b;
            while ((b = is.read()) != -1) {
                response.append((char) b);
            }
            is.close();
            conn.disconnect();

            Log.d(TAG, "Server Response: " + response.toString());
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            return null;
        }
    }


    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secret_key);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) return;

        try {
            JSONObject json = new JSONObject(result);
            int resultCode = json.getInt("resultCode");

            if (resultCode == 0) {
                String payUrl = json.getString("payUrl");
                // 👉 Mở URL thanh toán (trình duyệt hoặc MoMo app)
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(payUrl));
                context.startActivity(intent);

            } else {
                Log.e(TAG, "Thanh toán thất bại: " + json.getString("message"));
                Toast.makeText(context, "Khởi tạo thanh toán thất bại!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
        }
    }
}

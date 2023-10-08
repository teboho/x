package me.teboho.tweetjava.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import me.teboho.tweetjava.BuildConfig;

public class Utility {
    public static String consumerKey = BuildConfig.consumerKey; // This is generated on the developer portal
    public static String consumerSecret = BuildConfig.consumerSecret; // From dev portal as well
    public static String tokenKey = ""; //BuildConfig.tokenKey;
    public static String tokenSecret = ""; // BuildConfig.tokenSecret;

    public static String prepareSignature(String signatureBase, String oAuthConsumerSecret, String oAuthTokenSecret) throws UnsupportedEncodingException {
        byte[] byteHMAC = null;
        try {
            Mac mac  = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec;
            if (oAuthTokenSecret == null) {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&';
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            } else {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&'; // encode consumer secret
                signingKey += URLEncoder.encode(oAuthTokenSecret, "UTF-8"); // encode token secret
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            }
            mac.init(spec);
            byteHMAC = mac.doFinal(signatureBase.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Encoding in UTF 8
        String signature = new String(Base64.getEncoder().encode(byteHMAC));
        signature = URLEncoder.encode(signature, "UTF-8");

        return signature;
    }

    /**
     * Unique **len** amount string
     * @param len amount of characters in the noce
     * @return nonce string
     */
    public static String genNonce(int len) {
        String letters = "abcdefghjkmnpqrstuvwxyz";
        letters += "ABCDEFGHJKMNPQRSTUVWXYZ";
        letters += "0123456789";
        String nonce = "";
        for (int i = 0; i < len; i++) {
            int index = (int) (new Random().nextDouble() * letters.length());
            nonce += letters.substring(index, index + 1);
        }
        return nonce;
    }

    public static String genParamaterString(String nonce, String timestamp, String consumerKey) throws UnsupportedEncodingException {
        return URLEncoder.encode("oauth_consumer_key", "UTF-8") + "="
                + URLEncoder.encode(consumerKey, "UTF-8") + "&" + URLEncoder.encode("oauth_nonce", "UTF-8") + "="
                + URLEncoder.encode(nonce, "UTF-8") + "&" + URLEncoder.encode("oauth_signature_method", "UTF-8")
                + "=" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "&"
                + URLEncoder.encode("oauth_timestamp", "UTF-8") + "=" + URLEncoder.encode(timestamp, "UTF-8") + "&"
                + URLEncoder.encode("oauth_token", "UTF-8") + "=" + URLEncoder.encode(tokenKey, "UTF-8") + "&"
                + URLEncoder.encode("oauth_version", "UTF-8") + "=" + URLEncoder.encode("1.0", "UTF-8");
    }

    public static String genOAuthHeader(String nonce, String timestamp, String signature, String consumerKey, String tokenKey) {
        return "OAuth " +
                "oauth_consumer_key=\""+consumerKey+"\"" + "," +
                "oauth_token=\""+tokenKey+"\"" + "," +
                "oauth_signature_method=\"HMAC-SHA1\"" + "," +
                "oauth_timestamp=\""+ timestamp + "\"" + "," +
                "oauth_nonce=\""+ nonce +"\"" + "," +
                "oauth_version=\"1.0\"" + "," +
                "oauth_signature=\""+signature+"\"";
    }
}

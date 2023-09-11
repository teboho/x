package me.teboho.tweetjava;

//import hmac.HmacSHA1;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OAuthSignature {
    /**
     *
     * @param method GET
     * @param url
     * @param consumerKey
     * @param consumerSecret
     * @param tokenKey
     * @param tokenSecret
     * @return
     */
    public static String generateSignature(String method, String url, String consumerKey, String consumerSecret, String tokenKey, String tokenSecret) {
        // Encode the URL parameters.
        String encodedParams = null;
        try {
            encodedParams = URLEncoder.encode(urlencode(url), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // Create the signature base string.
        String signatureBase = method + "&" + encodedParams + "&" + consumerSecret;

        // Create the signature key.
        String signatureKey = consumerKey + ":" + tokenKey;

        // Generate the signature.
        byte[] signatureBytes = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1,signatureKey.getBytes()).doFinal();
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        return signature;
    }

    private String computeSignature(String signatureBaseStr, String oAuthConsumerSecret, String oAuthTokenSecret) {

        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec;
            if (null == oAuthTokenSecret) {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&';
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            } else {
                String signingKey = URLEncoder.encode(oAuthConsumerSecret, "UTF-8") + '&'
                        + URLEncoder.encode(oAuthTokenSecret, "UTF-8");
                spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            }
            mac.init(spec);
            byteHMAC = mac.doFinal(signatureBaseStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(byteHMAC));
    }

    private static String urlencode(String str) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(str, "UTF-8");
    }
}

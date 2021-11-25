package com.app.workstamper.config;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AES256
{
    private static final String // WARNING! Change these in public release.
            SECRET_KEY = "NDd:&]SjdA<zaJh8PTfgprhm+<GsNk",
            SALT = "#Z<Y4sv`~!P&S9jYF=51"; // This should be generated with some logic instead of hard coding it.

    private static IvParameterSpec GetIvParameterSpec()
    {
        byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        return new IvParameterSpec(ivBytes);
    }

    private static SecretKeySpec GetProcessedKey()
    {
        try
        {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tempKey = keyFactory.generateSecret(keySpec);
            return new SecretKeySpec(tempKey.getEncoded(), "AES");
        }
        catch (Exception e)
        {
            Log.e("Encrypt","Error while processing key: " + e.toString());
            return null;
        }
    }

    public static String Encrypt(String string)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, GetProcessedKey(), GetIvParameterSpec());
            return Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e)
        {
            Log.e("Encrypt","Error while encrypting: " + e.toString());
            return "Error";
        }
    }


    public static String Decrypt(String string)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, GetProcessedKey(), GetIvParameterSpec());
            return new String(cipher.doFinal(Base64.getDecoder().decode(string)));
        }
        catch (Exception e)
        {
            Log.e("Decrypt","Error while decrypting: " + e.toString());
            return "Error";
        }
    }
}

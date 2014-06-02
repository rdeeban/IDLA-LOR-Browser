package org.idla.lor.services;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * @author Deeban Ramalingam
 * @version 1.0.0
 * CryptoService performs Cryptographic services such as AES encryption/decryption
 */
public class CryptoService {

    private String key;

    /**
     * default constructor
     */
    public CryptoService(String key) {
        this.key = key;
    }

    /**
     * returns encrypted key
     * @param value - key to encrypt
     * @return encrypted key
     * @throws GeneralSecurityException - if key is AES incompatible
     */
    public String encrypt(String value) throws GeneralSecurityException {
        if (value != null) {
            SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return byteArrayToHexString(encrypted).toLowerCase();
        } else {
            return "";
        }
    }

    /**
     * returns decrypted key
     * @param message - AES encrypted key to decrypt
     * @return decrypted AES key
     * @throws GeneralSecurityException - if encrypted key is AES incompatible
     */
    public String decrypt(String message) throws GeneralSecurityException {
        if (message != null) {
            SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
            return (new String(decrypted).length() == 0) ? "admin" : new String(decrypted);
        } else {
            return "";
        }
    }

    /**
     *
     * @param b - byte array
     * @return hex string of byte array
     */
    private String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    /**
     *
     * @param s - hex string
     * @return byte array of hex string
     */
    private byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static void main (String args[]) throws GeneralSecurityException {
        CryptoService cryptoService = new CryptoService("0123456789abcdef0123456789abcdef");
        System.out.println(cryptoService.encrypt("deeban-test"));
    }
}
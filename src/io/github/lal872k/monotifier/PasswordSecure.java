/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lal872k.monotifier;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author L. Arthur Lewis II
 */
public class PasswordSecure {
    
    // http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
    
    private static final SecretKey[] SECRETS = new SecretKey[Key.values().length];
    
    public enum Key {
        OLD_KEY(0), CURRENT_KEY(1), TEMP_KEY(2);
        
        private final int index;
        
        Key(int index){
            this.index = index;
        }
        
        public int getIndex(){
            return index;
        }
        
    }
    
    public static void initializeCipher(String password, byte[] salt, Key key) throws NoSuchAlgorithmException, InvalidKeySpecException{
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        setKey(key, new SecretKeySpec(tmp.getEncoded(), "AES"));
    }
    
    public static byte[] getRandomSalt(){
        SecureRandom rs = new SecureRandom();
        byte[] salt = new byte[8];
        rs.nextBytes(salt);
        return salt;
    }
    
    public static Password encryptText(String text, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(key));
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"));
            return new Password(iv, ciphertext);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (InvalidParameterSpecException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static String decryptText(Password text, Key key){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(key), new IvParameterSpec(text.getIV()));
            String plaintext = new String(cipher.doFinal(text.getCipherPassword()), "UTF-8");
            return plaintext;
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            // wrong pass
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            System.out.println("There seems to be a problem with your key. Try following the instructions to the following link: http://stackoverflow.com/a/6481658");
        } catch (InvalidAlgorithmParameterException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static SecretKey getKey(Key key){
        return SECRETS[key.getIndex()];
    }
    
    private static void setKey(Key key, SecretKey secretKey){
        SECRETS[key.getIndex()] = secretKey;
    }
    
    public static void moveKey(Key oldKey, Key newKey){
        SECRETS[newKey.getIndex()] = SECRETS[oldKey.getIndex()];
    }
    
    public static boolean keysEqual(Key key1, Key key2){
        if (key1 == null){
            throw new NullPointerException();
        }
        if (SECRETS[key1.getIndex()] == null){
            throw new NullPointerException();
        }
        return SECRETS[key1.getIndex()].equals(SECRETS[key2.getIndex()]);
    }
    
}

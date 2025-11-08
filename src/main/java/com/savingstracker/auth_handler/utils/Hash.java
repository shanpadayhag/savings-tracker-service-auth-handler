package com.savingstracker.auth_handler.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
  public static String hash(String token) {
    if (token == null) return null;

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

      return bytesToHex(encodedhash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Could not find SHA-256 algorithm", e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int index = 0; index < hash.length; index++) {
      String hex = Integer.toHexString(0xff & hash[index]);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}

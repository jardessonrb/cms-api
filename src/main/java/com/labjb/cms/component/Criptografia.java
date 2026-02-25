package com.labjb.cms.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Criptografia {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // recomendado para GCM
    private static final int TAG_LENGTH = 128; // em bits

    /**
     * Criptografa o conteúdo usando AES/GCM
     */
    public String encripta(String chave, String conteudo) {
        try {
            byte[] keyBytes = ajustarChave(chave);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            // Gera IV aleatório
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));

            // Junta IV + conteúdo criptografado
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            // Base64 URL-safe
            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    /**
     * Descriptografa o conteúdo usando AES/GCM
     */
    public String decripta(String chave, String conteudoCriptografado) {
        try {
            byte[] keyBytes = ajustarChave(chave);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            byte[] decoded = Base64.getUrlDecoder().decode(conteudoCriptografado);

            // Extrai IV
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // Extrai payload criptografado
            byte[] encrypted = new byte[decoded.length - IV_SIZE];
            System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }

    /**
     * Ajusta a chave para 16, 24 ou 32 bytes (AES-128/192/256)
     */
    private static byte[] ajustarChave(String chave) {
        byte[] key = chave.getBytes(StandardCharsets.UTF_8);

        // força 32 bytes (AES-256)
        byte[] keyBytes = new byte[32];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));

        return keyBytes;
    }

    public static void main(String[] args) throws JsonProcessingException {
        String chave = "giEANblv8peAPPH07TefR5uMSBtqZ1j7";
        Criptografia criptografia =  new Criptografia();

        Map<String, Object> conteudo = new LinkedHashMap<>();
        conteudo.put("campeonatoId","fb897ab6-90b7-4f56-a5f9-a59bd9577f75");
        conteudo.put("usuarioId","fulanodetal");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(conteudo);

//        String criptografado = criptografia.encripta(chave, json);
        String criptografado = "yjYMWnQupA6PFtdspzDRRg20VvOW1TsX5RltyRsZDzxKwfL_vi8wKBR_hJCzT0njLPF8rJ6Qb5hoD4PoxdchYOIjyMXT4AYX9JoKmdcdONnNc4Tich1RNPtDmZQvjYOmdiY9j7yGsmJAO_8q7g";
        System.out.println("Criptografado: " + criptografado);

        String descriptografado = criptografia.decripta(chave, criptografado);
        System.out.println("Descriptografado: " + descriptografado);
    }
}
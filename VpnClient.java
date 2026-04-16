import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * VPN Client - Criptografa o tráfego e envia ao servidor VPN.
 */
public class VpnClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;
    // Mesma chave do servidor (em produção, use Diffie-Hellman para troca segura)
    private static final byte[] SECRET_KEY = "MinhaChaveSecreta12345678901234".getBytes(); // 32 bytes

    public static void main(String[] args) throws Exception {
        System.out.println("[CLIENT] Conectando ao servidor VPN...");

        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("[CLIENT] Conectado! Túnel VPN estabelecido.");
            System.out.println("[CLIENT] Digite mensagens para enviar pelo túnel (ou 'sair' para fechar):\n");

            // Simula pacotes de rede sendo enviados pelo túnel
            String[] simulatedPackets = {
                "GET /index.html HTTP/1.1 Host: exemplo.com",
                "POST /api/login {user: 'admin', pass: 'secret'}",
                "DNS QUERY: www.google.com",
                "PING 8.8.8.8 TTL=64"
            };

            for (String packet : simulatedPackets) {
                System.out.println("\n[CLIENT] Enviando pacote: " + packet);

                // Criptografa antes de enviar pelo túnel
                String encrypted = encrypt(packet);
                System.out.println("[CLIENT] Criptografado (Base64): " + encrypted.substring(0, 40) + "...");

                out.println(encrypted);

                // Aguarda resposta do servidor
                String encryptedResponse = in.readLine();
                if (encryptedResponse != null) {
                    String decryptedResponse = decrypt(encryptedResponse);
                    System.out.println("[CLIENT] Resposta recebida: " + decryptedResponse);
                }

                Thread.sleep(500); // Pequena pausa entre pacotes
            }

            System.out.println("\n[CLIENT] Todos os pacotes enviados. Fechando túnel VPN.");
        }
    }

    // ─── Criptografia AES-256-CBC ──────────────────────────────────────────────

    public static String encrypt(String plainText) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
}

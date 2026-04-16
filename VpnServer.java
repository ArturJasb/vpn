import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * VPN Server - Recebe conexões de clientes, descriptografa e encaminha o tráfego.
 */
public class VpnServer {

    private static final int PORT = 9999;
    // Chave AES de 256 bits (32 bytes) - em produção, use troca de chaves (Diffie-Hellman)
    private static final byte[] SECRET_KEY = "MinhaChaveSecreta12345678901234".getBytes(); // 32 bytes

    public static void main(String[] args) throws Exception {
        System.out.println("[SERVER] VPN Server iniciando na porta " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Cliente conectado: " + clientSocket.getInetAddress());

                // Cada cliente é tratado em uma thread separada
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String encryptedMessage;
            while ((encryptedMessage = in.readLine()) != null) {
                // Descriptografa a mensagem recebida
                String decrypted = decrypt(encryptedMessage);
                System.out.println("[SERVER] Pacote recebido (descriptografado): " + decrypted);

                // Simula o encaminhamento do pacote para o destino
                String response = processPacket(decrypted);

                // Criptografa a resposta e envia de volta
                String encryptedResponse = encrypt(response);
                out.println(encryptedResponse);
                System.out.println("[SERVER] Resposta enviada (criptografada).");
            }
        } catch (Exception e) {
            System.out.println("[SERVER] Conexão encerrada: " + e.getMessage());
        }
    }

    /**
     * Simula o processamento/encaminhamento de um pacote de rede.
     * Em uma VPN real, aqui você encaminharia para o destino real via TUN/TAP.
     */
    private static String processPacket(String packet) {
        return "RESPOSTA_DO_SERVIDOR: [" + packet + "] processado com sucesso";
    }

    // ─── Criptografia AES-256-CBC ──────────────────────────────────────────────

    public static String encrypt(String plainText) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16]; // IV de 128 bits
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Concatena IV + dados criptografados e codifica em Base64
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        // Extrai IV (primeiros 16 bytes) e dados criptografados
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

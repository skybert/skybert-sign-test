package net.skybert.sign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * SignApp. Reads a public and private key from file and uses these to sign data.
 *
 * <p>The private and public key must be created with:
 *
 * <pre>
 * $ openssl genpkey -algorithm RSA -out private-key.pem
 * $ openssl rsa -pubout -in private-key.pem -out public-key.pem
 * </pre>
 *
 * @author <a href="mailto:tkj@stibodx.com">Torstein Krause Johansen</a>
 */
public class SignApp {

  private PublicKey publicKey;
  private PrivateKey privateKey;

  public SignApp(final Path pPrivateKey, final Path pPublicKey)
      throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {

    // Read private key from file
    String privateKeyContent =
        new String(Files.readAllBytes(pPrivateKey))
            .replaceAll("\\n", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");
    System.out.println("privateKeyContent=[" + privateKeyContent + "]");
    byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    privateKey = keyFactory.generatePrivate(privateKeySpec);

    // Read public key from file
    String publicKeyContent =
        new String(Files.readAllBytes(pPublicKey))
            .replaceAll("\\n", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "");

    System.out.println("publicKeyContent=[" + publicKeyContent + "]");
    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    publicKey = keyFactory.generatePublic(publicKeySpec);
  }

  public static void main(String[] args)
      throws IOException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          InvalidKeyException,
          SignatureException {

    SignApp app = new SignApp(Paths.get("etc/private-key.pem"), Paths.get("etc/public-key.pem"));

    // Sign a string using the private key
    String data = "This is a string to be signed";
    String signedDataBase64 = app.sign(data);
    System.out.println("Signed data: " + signedDataBase64);

    boolean isVerified = app.verify(data, signedDataBase64);
    System.out.println("Signature verified: " + isVerified);
  }

  private boolean verify(String pUnsignedData, String pSignedDataBase64)
      throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
    byte[] signedData = Base64.getDecoder().decode(pSignedDataBase64);
    // Verify the signature using the public key
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(publicKey);
    signature.update(pUnsignedData.getBytes());
    return signature.verify(signedData);
  }

  private String sign(String pData)
      throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(pData.getBytes());
    byte[] signedData = signature.sign();
    return Base64.getEncoder().encodeToString(signedData);
  }
}

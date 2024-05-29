import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws Exception {
        // Register the BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());

        createKeystore("keystore.p12", "password", "alias");

        // Paths for the input PDF, signed PDF, and password-protected PDF
        String inputPdf = "input.pdf";
        String signedPdf = "signed_input.pdf";
        String passwordProtectedPdf = "protected_input.pdf";

        // Sign the PDF using the keystore
        signPdf(inputPdf, signedPdf, "keystore.p12", "password", "alias");

        // Password protect the signed PDF
        passwordProtectPdf(signedPdf, passwordProtectedPdf, "ownerPassword", "userPassword");
    }

    /**
     * Signs a PDF document using a keystore.
     * @param src The source PDF file path.
     * @param dest The destination signed PDF file path.
     * @param keystorePath The keystore file path.
     * @param keystorePassword The keystore password.
     * @param alias The alias of the key in the keystore.
     * @throws Exception If an error occurs during signing.
     */
    private static void signPdf(String src, String dest, String keystorePath, String keystorePassword, String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        PrivateKey pk = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties().useAppendMode());

        signer.setFieldName("Signature");

        PrivateKeySignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
        BouncyCastleDigest digest = new BouncyCastleDigest();
        signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
    }

    /**
     * Password protects a PDF document.
     * @param src The source PDF file path.
     * @param dest The destination password-protected PDF file path.
     * @param ownerPassword The owner password.
     * @param userPassword The user password.
     * @throws Exception If an error occurs during password protection.
     */
    private static void passwordProtectPdf(String src, String dest, String ownerPassword, String userPassword) throws Exception {
        PdfReader reader = new PdfReader(src);
        PdfWriter writer = new PdfWriter(dest, new WriterProperties().setStandardEncryption(
                userPassword.getBytes(), ownerPassword.getBytes(),
                EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_128));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        pdfDoc.close();
    }

    /**
     * Creates a keystore with a self-signed certificate.
     * @param keystorePath The keystore file path.
     * @param keystorePassword The keystore password.
     * @param alias The alias of the key in the keystore.
     * @throws Exception If an error occurs during keystore creation.
     */
    public static void createKeystore(String keystorePath, String keystorePassword, String alias) throws Exception {
        // Generate a key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        // Generate a self-signed certificate
        X500Name issuer = new X500Name("CN=Test, L=London, C=GB");
        BigInteger serialNumber = BigInteger.valueOf(new SecureRandom().nextInt() & 0x7fffffff);
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + (365 * 24 * 60 * 60 * 1000L));
        X500Name subject = issuer; // Self-signed, so issuer is the same as subject

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, notBefore, notAfter, subject, keyPair.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC").build(privateKey);
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);

        // Create a keystore and set the entry
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, privateKey, keystorePassword.toCharArray(), new Certificate[]{cert});

        // Save the keystore to a file
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }
}

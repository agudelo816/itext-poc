import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        // Register the BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());

        createKeystore("keystore.p12", "password", "alias");

        String dest = "table_example.pdf";
        String signedDest = "signed_table_example.pdf";
        String passwordProtectedDest = "protected_table_example.pdf";
        List<String> columnNames = Arrays.asList("Column 1", "Column 2", "Column 3");
        float[] columnWidths = new float[columnNames.size()];
        Arrays.fill(columnWidths, 25);

        createPdf(dest, columnNames, columnWidths);
        signPdf(dest, signedDest, "keystore.p12", "password", "alias", "Test Signer", "Test Reason", "Test Location");
        passwordProtectPdf(signedDest, passwordProtectedDest, "ownerPassword", "userPassword");
    }

    private static void createPdf(String dest, List<String> columnNames, float[] columnWidths) throws Exception {
        PdfWriter writer = createPdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        Table table = createTable(columnWidths);
        addHeaderCells(table, columnNames, font);

        document.add(table);
        document.close();
    }

    private static PdfWriter createPdfWriter(String dest) throws Exception {
        return new PdfWriter(dest);
    }

    private static Table createTable(float[] columnWidths) {
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private static void addHeaderCells(Table table, List<String> columnNames, PdfFont font) {
        for (String columnName : columnNames) {
            Paragraph paragraphCell = new Paragraph(columnName);
            paragraphCell.setFont(font);
            paragraphCell.setFontSize(10);
            paragraphCell.setTextAlignment(TextAlignment.CENTER);
            Cell cell = new Cell();
            cell.add(paragraphCell);
            table.addHeaderCell(cell);
        }
    }

    private static void signPdf(String src, String dest, String keystorePath, String keystorePassword, String alias,
                                String signerName, String reason, String location) throws Exception {
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

    private static void passwordProtectPdf(String src, String dest, String ownerPassword, String userPassword) throws Exception {
        PdfReader reader = new PdfReader(src);
        PdfWriter writer = new PdfWriter(dest, new WriterProperties().setStandardEncryption(
                userPassword.getBytes(), ownerPassword.getBytes(),
                EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_128));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        pdfDoc.close();
    }

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

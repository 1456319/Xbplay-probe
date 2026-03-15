package crypto;

import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.spongycastle.asn1.DERBitString;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.ECPointUtil;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECNamedCurveSpec;
import util.Util;
/* loaded from: /app/base.apk/classes4.dex */
public class SgCrypto {
    public byte[] aes_iv;
    public byte[] aes_key;
    public byte[] certificate;
    public byte[] hmac_key;
    public String issuer;
    public byte[] ourGeneratedPublicKey;
    public byte[] sharedSecretHashed;
    public byte[] tmpIv;
    public String xboxLiveId;

    public void SgCrypto() {
        this.aes_key = Util.createNullByteArray(16);
        this.aes_iv = Util.createNullByteArray(16);
        this.hmac_key = Util.createNullByteArray(32);
    }

    public void loadSgData(byte[] bArr) {
        getIssuerInfoFromCert(bArr);
        getKeysFromCert(bArr);
    }

    public void printKeyData(String str) {
        Log.e("SG Key Data", str);
        Log.e("SG Key Data", "aes_key: " + Util.byteArrayToHexString(this.aes_key, true));
        Log.e("SG Key Data", "aes_iv: " + Util.byteArrayToHexString(this.aes_iv, true));
        Log.e("SG Key Data", "hmac_key: " + Util.byteArrayToHexString(this.hmac_key, true));
    }

    private void getKeysFromCert(byte[] bArr) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();
            byte[] ecKeyBytesFromDERKey = ecKeyBytesFromDERKey(generateKeyPair.getPublic().getEncoded());
            byte[] pubFromEC = getPubFromEC(bArr);
            PublicKey publicKeyFromEC = publicKeyFromEC(pubFromEC);
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(generateKeyPair.getPrivate());
            keyAgreement.doPhase(publicKeyFromEC, true);
            byte[] generateSecret = keyAgreement.generateSecret();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(Util.PRE_SALT);
            messageDigest.update(generateSecret);
            messageDigest.update(Util.POST_SALT);
            byte[] digest = messageDigest.digest();
            this.sharedSecretHashed = digest;
            this.aes_key = Arrays.copyOfRange(digest, 0, 16);
            this.aes_iv = Arrays.copyOfRange(digest, 16, 32);
            this.hmac_key = Arrays.copyOfRange(digest, 32, 64);
            this.ourGeneratedPublicKey = Arrays.copyOfRange(ecKeyBytesFromDERKey, 1, 65);
            this.certificate = Arrays.copyOfRange(pubFromEC, 1, 65);
        } catch (Exception e) {
            Log.e("Test", e.getMessage());
        }
    }

    private void getIssuerInfoFromCert(byte[] bArr) {
        try {
            X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bArr));
            this.issuer = x509Certificate.getIssuerX500Principal().toString();
            this.xboxLiveId = x509Certificate.getSubjectX500Principal().toString();
        } catch (Exception unused) {
        }
    }

    public ByteArrayOutputStream encryptV2(ByteArrayOutputStream byteArrayOutputStream, byte[] bArr, byte[] bArr2) {
        byte[] doFinal;
        ByteArrayOutputStream byteArrayOutputStream2;
        ByteArrayOutputStream byteArrayOutputStream3 = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(1, new SecretKeySpec(bArr, "AES/CBC/NoPadding"), new IvParameterSpec(bArr2));
            doFinal = cipher.doFinal(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream2 = new ByteArrayOutputStream(doFinal.length);
        } catch (Exception e) {
            e = e;
        }
        try {
            byteArrayOutputStream2.write(doFinal, 0, doFinal.length);
            return byteArrayOutputStream2;
        } catch (Exception e2) {
            e = e2;
            byteArrayOutputStream3 = byteArrayOutputStream2;
            Log.e("ERR", "error encrypting payload" + e.getMessage());
            return byteArrayOutputStream3;
        }
    }

    public byte[] decryptMessageResponse(byte[] bArr, byte[] bArr2) {
        Log.i("aes_iv: ", Util.byteArrayToHexString(this.aes_iv, true));
        Log.i("aes_key: ", Util.byteArrayToHexString(this.aes_key, true));
        Log.i("encryptedData: ", Util.byteArrayToHexString(bArr, true));
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptV2(Util.byteArrayToOutputStream(bArr2), this.aes_iv, Util.createNullByteArray(16)).toByteArray());
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.aes_key, "AES/CBC/NoPadding");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKeySpec, ivParameterSpec);
            byte[] doFinal = cipher.doFinal(bArr);
            Log.i("decryptedData: ", Util.byteArrayToHexString(doFinal, true));
            return doFinal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    public byte[] decrypt(byte[] bArr) {
        Log.i("aes_iv: ", Util.byteArrayToHexString(this.aes_iv, true));
        Log.i("aes_key: ", Util.byteArrayToHexString(this.aes_key, true));
        Log.i("encryptedData: ", Util.byteArrayToHexString(bArr, true));
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(this.aes_iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.aes_key, "AES/CBC/NoPadding");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKeySpec, ivParameterSpec);
            byte[] doFinal = cipher.doFinal(bArr);
            Log.i("decryptedData: ", Util.byteArrayToHexString(doFinal, true));
            return doFinal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getPubFromEC(byte[] bArr) {
        try {
            return ecKeyBytesFromDERKey(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bArr))).getPublicKey().getEncoded());
        } catch (Exception unused) {
            return null;
        }
    }

    public static byte[] ecKeyBytesFromDERKey(byte[] bArr) {
        return ((DERBitString) DERSequence.getInstance(bArr).getObjectAt(1)).getBytes();
    }

    private static PublicKey publicKeyFromEC(byte[] bArr) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec("secp256r1", parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN());
        return keyFactory.generatePublic(new ECPublicKeySpec(ECPointUtil.decodePoint(eCNamedCurveSpec.getCurve(), bArr), eCNamedCurveSpec));
    }

    public String convertBytesToPemKey(byte[] bArr) {
        String[] splitStringEvery = Util.splitStringEvery(Base64.encodeToString(bArr, 2), 64);
        StringBuilder sb = new StringBuilder();
        for (String str : splitStringEvery) {
            sb.append(str);
            sb.append("\n");
        }
        return ("-----BEGIN CERTIFICATE-----\n" + sb.toString()) + "-----END CERTIFICATE-----";
    }
}

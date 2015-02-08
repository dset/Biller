package se.dset.android.wallenberg.security;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class BillingSignatureVerifier implements SignatureVerifier {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    private String base64PublicKey;

    public BillingSignatureVerifier(String base64PublicKey) {
        this.base64PublicKey = base64PublicKey;
    }

    @Override
    public boolean verify(String data, String signature) {
        try {
            return verifyInternal(data, signature);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyInternal(String data, String signature) throws NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException {
        if(Strings.isNullOrEmpty(base64PublicKey)
                || Strings.isNullOrEmpty(data)
                || Strings.isNullOrEmpty(signature)) {
            return false;
        }

        byte[] decodedKey = BaseEncoding.base64().decode(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));

        byte[] decodedSignature = BaseEncoding.base64().decode(signature);

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(key);
        sig.update(data.getBytes());
        return sig.verify(decodedSignature);
    }
}

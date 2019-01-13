package se.dset.android.biller.security;

public interface SignatureVerifier {
    public boolean verify(String data, String signature);
}

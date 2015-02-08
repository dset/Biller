package se.dset.android.wallenberg.security;

public interface SignatureVerifier {
    public boolean verify(String data, String signature);
}

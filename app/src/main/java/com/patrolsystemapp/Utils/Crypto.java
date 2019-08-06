package com.patrolsystemapp.Utils;

import android.util.Base64;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import java.util.Arrays;

public class Crypto {
    public String pbkdf2(String secret, String salt, int iterations, int keyLength) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        byte[] secretData = secret.getBytes();
        byte[] saltData = salt.getBytes();
        gen.init(secretData, saltData, iterations);
        byte[] derivedKey = ((KeyParameter)gen.generateDerivedParameters(keyLength * 8)).getKey();
        return encode(derivedKey);
    }

    private String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}

package org.bouncycastle.pqc.crypto.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import junit.framework.TestCase;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.pqc.crypto.cmce.CMCEKEMExtractor;
import org.bouncycastle.pqc.crypto.cmce.CMCEKEMGenerator;
import org.bouncycastle.pqc.crypto.cmce.CMCEKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEKeyPairGenerator;
import org.bouncycastle.pqc.crypto.cmce.CMCEParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEPublicKeyParameters;
import org.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import org.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import org.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class CMCEVectorTest
    extends TestCase
{
    public void testParameters()
        throws Exception
    {
        CMCEParameters[] params = new CMCEParameters[] {
            CMCEParameters.mceliece348864r3,
            CMCEParameters.mceliece348864fr3,
            CMCEParameters.mceliece460896r3,
            CMCEParameters.mceliece460896fr3,
            CMCEParameters.mceliece6688128r3,
            CMCEParameters.mceliece6688128fr3,
            CMCEParameters.mceliece6960119r3,
            CMCEParameters.mceliece6960119fr3,
            CMCEParameters.mceliece8192128r3,
            CMCEParameters.mceliece8192128fr3
        };

        assertEquals(128, CMCEParameters.mceliece348864r3.getDefaultKeySize());
        assertEquals(128, CMCEParameters.mceliece348864fr3.getDefaultKeySize());
        assertEquals(192, CMCEParameters.mceliece460896r3.getDefaultKeySize());
        assertEquals(192, CMCEParameters.mceliece460896fr3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece6688128r3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece6688128fr3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece6960119r3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece6960119fr3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece8192128r3.getDefaultKeySize());
        assertEquals(256, CMCEParameters.mceliece8192128fr3.getDefaultKeySize());
    }
    
    public void testVectors()
        throws Exception
    {
        String[] files = new String[] {
            "3488-64-cmce.rsp",
            "3488-64-f-cmce.rsp",
            "4608-96-cmce.rsp",
            "4608-96-f-cmce.rsp",
            "6688-128-cmce.rsp",
            "6688-128-f-cmce.rsp",
            "6960-119-cmce.rsp",
            "6960-119-f-cmce.rsp",
            "8192-128-cmce.rsp",
            "8192-128-f-cmce.rsp"
        };

        CMCEParameters[] params = new CMCEParameters[] {
            CMCEParameters.mceliece348864r3,
            CMCEParameters.mceliece348864fr3,
            CMCEParameters.mceliece460896r3,
            CMCEParameters.mceliece460896fr3,
            CMCEParameters.mceliece6688128r3,
            CMCEParameters.mceliece6688128fr3,
            CMCEParameters.mceliece6960119r3,
            CMCEParameters.mceliece6960119fr3,
            CMCEParameters.mceliece8192128r3,
            CMCEParameters.mceliece8192128fr3
        };

//        files = "6960-119-cmce.rsp";// 8192-128-cmce.rsp";
//        files = "8192-128-cmce.rsp";
//        String files = "4608-96-cmce.rsp";// 6688-128-cmce.rsp 6960-119-cmce.rsp 8192-128-cmce.rsp";
        for (int fileIndex = 0; fileIndex != files.length; fileIndex++)
        {
            String name = files[fileIndex];
            System.out.println("testing: " + name);
            InputStream src = CMCEVectorTest.class.getResourceAsStream("/org/bouncycastle/pqc/crypto/test/cmce/" + name);
            BufferedReader bin = new BufferedReader(new InputStreamReader(src));

            String line = null;
            HashMap<String, String> buf = new HashMap<String, String>();
            while ((line = bin.readLine()) != null)
            {
                line = line.trim();

                if (line.startsWith("#"))
                {
                    continue;
                }
                if (line.length() == 0)
                {
                    if (buf.size() > 0)
                    {
                        String count = buf.get("count");
                        System.out.println("test case: " + count);

                        byte[] seed = Hex.decode(buf.get("seed")); // seed for cmce secure random
                        byte[] pk = Hex.decode(buf.get("pk"));     // public key
                        byte[] sk = Hex.decode(buf.get("sk"));     // private key
                        byte[] ct = Hex.decode(buf.get("ct"));     // ciphertext
                        byte[] ss = Hex.decode(buf.get("ss"));     // session key

                        NISTSecureRandom random = new NISTSecureRandom(seed, null);
                        CMCEParameters parameters = params[fileIndex];

                        CMCEKeyPairGenerator kpGen = new CMCEKeyPairGenerator();
                        CMCEKeyGenerationParameters genParam = new CMCEKeyGenerationParameters(random, parameters);
                        //
                        // Generate keys and test.
                        //
                        kpGen.init(genParam);
                        AsymmetricCipherKeyPair kp = kpGen.generateKeyPair();

                        CMCEPublicKeyParameters pubParams = (CMCEPublicKeyParameters)PublicKeyFactory.createKey(SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo((CMCEPublicKeyParameters)kp.getPublic()));
                        CMCEPrivateKeyParameters privParams = (CMCEPrivateKeyParameters)PrivateKeyFactory.createKey(PrivateKeyInfoFactory.createPrivateKeyInfo((CMCEPrivateKeyParameters)kp.getPrivate()));

                        assertTrue(name + " " + count + ": public key", Arrays.areEqual(pk, pubParams.getPublicKey()));
                        assertTrue(name + " " + count + ": secret key", Arrays.areEqual(sk, privParams.getPrivateKey()));
                        
                        // KEM Enc
                        CMCEKEMGenerator cmceEncCipher = new CMCEKEMGenerator(random);
                        SecretWithEncapsulation secWenc = cmceEncCipher.generateEncapsulated(pubParams, 256);
                        byte[] generated_cipher_text = secWenc.getEncapsulation();
                        assertTrue(name + " " + count + ": kem_enc cipher text", Arrays.areEqual(ct, generated_cipher_text));
                        byte[] secret = secWenc.getSecret();
                        assertTrue(name + " " + count + ": kem_enc key", Arrays.areEqual(ss, secret));

                        // KEM Dec
                        CMCEKEMExtractor cmceDecCipher = new CMCEKEMExtractor(privParams);

                        byte[] dec_key = cmceDecCipher.extractSecret(generated_cipher_text, 256);

                        assertTrue(name + " " + count + ": kem_dec ss", Arrays.areEqual(dec_key, ss));
                        assertTrue(name + " " + count + ": kem_dec key", Arrays.areEqual(dec_key, secret));
                    }
                    buf.clear();

                    continue;
                }

                int a = line.indexOf("=");
                if (a > -1)
                {
                    buf.put(line.substring(0, a).trim(), line.substring(a + 1).trim());
                }


            }
            System.out.println("testing successful!");
        }

    }
}

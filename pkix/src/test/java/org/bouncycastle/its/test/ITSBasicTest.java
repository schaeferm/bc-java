package org.bouncycastle.its.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import junit.framework.TestCase;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.its.ITSCertificate;
import org.bouncycastle.its.ITSValidityPeriod;
import org.bouncycastle.its.bc.BcITSContentSigner;
import org.bouncycastle.its.bc.BcITSContentVerifierProvider;
import org.bouncycastle.its.bc.BcITSExplicitCertificateBuilder;
import org.bouncycastle.its.bc.BcITSImplicitCertificateBuilder;
import org.bouncycastle.its.operator.ITSContentSigner;
import org.bouncycastle.oer.OERInputStream;
import org.bouncycastle.oer.OEROutputStream;
import org.bouncycastle.oer.its.AdditionalParams;
import org.bouncycastle.oer.its.ButterflyExpansion;
import org.bouncycastle.oer.its.Certificate;
import org.bouncycastle.oer.its.CertificateId;
import org.bouncycastle.oer.its.CertificateType;
import org.bouncycastle.oer.its.CrlSeries;
import org.bouncycastle.oer.its.Duration;
import org.bouncycastle.oer.its.EccP256CurvePoint;
import org.bouncycastle.oer.its.EeEcaCertRequest;
import org.bouncycastle.oer.its.EeRaCertRequest;
import org.bouncycastle.oer.its.EndEntityType;
import org.bouncycastle.oer.its.HashedId3;
import org.bouncycastle.oer.its.HashedId8;
import org.bouncycastle.oer.its.Hostname;
import org.bouncycastle.oer.its.IssuerIdentifier;
import org.bouncycastle.oer.its.Psid;
import org.bouncycastle.oer.its.PsidGroupPermissions;
import org.bouncycastle.oer.its.PsidSsp;
import org.bouncycastle.oer.its.PsidSspRange;
import org.bouncycastle.oer.its.PublicEncryptionKey;
import org.bouncycastle.oer.its.PublicVerificationKey;
import org.bouncycastle.oer.its.SequenceOfPsidGroupPermissions;
import org.bouncycastle.oer.its.SequenceOfPsidSsp;
import org.bouncycastle.oer.its.SequenceOfPsidSspRange;
import org.bouncycastle.oer.its.ServiceSpecificPermissions;
import org.bouncycastle.oer.its.SspRange;
import org.bouncycastle.oer.its.SubjectAssurance;
import org.bouncycastle.oer.its.SubjectPermissions;
import org.bouncycastle.oer.its.SymmAlgorithm;
import org.bouncycastle.oer.its.Time32;
import org.bouncycastle.oer.its.ToBeSignedCertificate;
import org.bouncycastle.oer.its.Uint8;
import org.bouncycastle.oer.its.ValidityPeriod;
import org.bouncycastle.oer.its.VerificationKeyIndicator;
import org.bouncycastle.oer.its.template.IEEE1609dot2;
import org.bouncycastle.oer.its.template.Ieee1609Dot2Dot1EcaEeInterface;
import org.bouncycastle.oer.its.template.Ieee1609Dot2Dot1EeRaInterface;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;

public class ITSBasicTest
    extends TestCase
{


    private static ITSCertificate loadCertificate(byte[] data)
        throws Exception
    {
        ByteArrayInputStream fin = new ByteArrayInputStream(data);

        OERInputStream oi = new OERInputStream(fin);

//        OERInputStream oi = new OERInputStream(new HexIn(fin))
//        {
//            {
//                debugOutput = new PrintWriter(System.out);
//            }
//        };
        ASN1Object obj = oi.parse(IEEE1609dot2.Certificate.build());
        ITSCertificate certificate = new ITSCertificate(Certificate.getInstance(obj));
        fin.close();
        return certificate;
    }

    public void testWithEncryptionKey()
        throws Exception
    {
        byte[] certRaw = Hex.decode("80030080e5f6db2f26d073ae798300000000001a5617008466a88101011a0d203004268ab91a0645a1042d65488001018002026f810302013201012080010780012482080301fffc03ff0003800125820a0401ffffff04ff00000080018982060201e002ff1f80018a82060201c002ff3f80018b820e0601000000fff806ff000000000780018c820a0401ffffe004ff00001f00018dc0008082e35a13ea972ef64806c7bff581f4effd2b7118bc3e096e194b47d859e1334b93808082a2f65bbb216fdee3c18c9fd5b4197e884b938de55feed7ed96f11713215e6258808019b31711707d09aa00b79973190bbd84eefd1255ad3b4f1a4d224b4172dfadddce409989d93178a3ec054b814bce4bbb99a979e4624c9972b8b0c862ff9ae70b");
        ITSCertificate cert = loadCertificate(certRaw);

        PublicEncryptionKey pec = cert.toASN1Structure().getCertificateBase().getToBeSignedCertificate().getEncryptionKey();
        assertSame(pec.getSupportedSymmAlg(), SymmAlgorithm.aes128Ccm);
    }

    public void testImplicitBuilder()
        throws Exception
    {

        byte[] ca = Hex.decode("800300810038811B45545349205465737420524341204320636572746966696361746500000000001A5617008466A8C001028002026E810201018002027081030201380102A080010E80012482080301FFFC03FF0003800125820A0401FFFFFF04FF00000080018982060201E002FF1F80018A82060201C002FF3F80018B820E0601000000FFF806FF000000000780018C820A0401FFFFE004FF00001F00018D0001600001610001620001630001640001650001660102C0208001018002026F82060201FE02FF01C0808082A4C29A1DDE0E1AEA8D36858B59016A45DB4A4968A2D5A1073B8EABC842C1D5948080B58B1A7CE9848D3EC315C70183D08E6E8B21C0FDA15A7839445AEEA636C794BA4ED59903EADC60372A542D21D77BFFB3E65B5B8BA3FB14BCE7CDA91268B177BC");
        ITSCertificate caCert = loadCertificate(ca);


        byte[] parentData = caCert.getEncoded();
        Digest digest = new SHA256Digest();
        byte[] parentDigest = new byte[digest.getDigestSize()];

        digest.update(parentData, 0, parentData.length);

        digest.doFinal(parentDigest, 0);


        ToBeSignedCertificate.Builder tbsBuilder = new ToBeSignedCertificate.Builder();

        tbsBuilder.setAssuranceLevel(new SubjectAssurance(new byte[]{(byte)0xC0}));
        // builder.setCanRequestRollover(OEROptional.ABSENT);

        BcITSImplicitCertificateBuilder certificateBuilder = new BcITSImplicitCertificateBuilder(caCert, tbsBuilder);

        certificateBuilder.setValidityPeriod(ITSValidityPeriod.from(new Date()).plusYears(1));

        certificateBuilder.setAppPermissions(
            PsidSsp.builder()
                .setPsid(new Psid(622))
                .setSsp(ServiceSpecificPermissions.builder()
                    .bitmapSsp(new DEROctetString(Hex.decode("0101")))
                    .createServiceSpecificPermissions())
                .createPsidSsp(),
            PsidSsp.builder()
                .setPsid(new Psid(624))
                .setSsp(ServiceSpecificPermissions.builder()
                    .bitmapSsp(new DEROctetString(Hex.decode("020138")))
                    .createServiceSpecificPermissions())
                .createPsidSsp()); // App Permissions

        certificateBuilder.setCertIssuePermissions(
            PsidGroupPermissions.builder()
                .setSubjectPermissions(
                    SubjectPermissions.builder().explicit(
                        SequenceOfPsidSspRange.builder()
                            .add(PsidSspRange.builder()
                                .setPsid(36).setSspRange(SspRange.builder().extension(Hex.decode("0301fffc03ff0003")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder()
                                .setPsid(37).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFFF04FF000000")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder()
                                .setPsid(137).setSspRange(SspRange.builder().extension(Hex.decode("0201E002FF1F")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder()
                                .setPsid(138).setSspRange(SspRange.builder().extension(Hex.decode("0201C002FF3F")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder()
                                .setPsid(139).setSspRange(SspRange.builder().extension(Hex.decode("0601000000FFF806FF0000000007")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder()
                                .setPsid(140).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFE004FF00001F")).createSspRange()).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(141).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(96).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(97).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(98).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(99).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(100).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(101).createPsidSspRange())
                            .add(PsidSspRange.builder().setPsid(102).createPsidSspRange())
                            .build()
                    ).createSubjectPermissions())
                .setMinChainLength(2)
                .setChainLengthRange(0)
                .setEeType(new EndEntityType(0xC0))
                .createPsidGroupPermissions(),
            PsidGroupPermissions.builder()
                .setSubjectPermissions(SubjectPermissions.builder()
                    .explicit(SequenceOfPsidSspRange.builder()
                        .add(PsidSspRange.builder()
                            .setPsid(623)
                            .setSspRange(
                                SspRange.builder()
                                    .extension(Hex.decode("0201FE02FF01"))
                                    .createSspRange()).createPsidSspRange())
                        .build())
                    .createSubjectPermissions())
                .setMinChainLength(1)
                .setChainLengthRange(0)
                .setEeType(new EndEntityType(0xC0)).createPsidGroupPermissions());

        ITSCertificate cert = certificateBuilder.build(
            CertificateId.builder()
                .name(new Hostname("Legion of the BouncyCastle CA"))
                .createCertificateId(), BigInteger.ONE, BigIntegers.TWO, null);

        IssuerIdentifier caIssuerIdentifier = IssuerIdentifier
            .builder()
            .sha256AndDigest(new HashedId8(Arrays.copyOfRange(parentDigest, parentDigest.length - 8, parentDigest.length)))
            .createIssuerIdentifier();

        assertTrue(cert.getIssuer().equals(caIssuerIdentifier));

        VerificationKeyIndicator vki = cert.toASN1Structure().getCertificateBase().getToBeSignedCertificate().getVerificationKeyIndicator();
        assertEquals(vki.getChoice(), VerificationKeyIndicator.reconstructionValue);
        assertEquals(vki.getObject(), EccP256CurvePoint.builder().createUncompressedP256(BigInteger.ONE, BigIntegers.TWO));
    }

    public void testBuildSelfSigned()
        throws Exception
    {
        SecureRandom rand = new SecureRandom();

        byte[] ca = Hex.decode("800300810038811B45545349205465737420524341204320636572746966696361746500000000001A5617008466A8C001028002026E810201018002027081030201380102A080010E80012482080301FFFC03FF0003800125820A0401FFFFFF04FF00000080018982060201E002FF1F80018A82060201C002FF3F80018B820E0601000000FFF806FF000000000780018C820A0401FFFFE004FF00001F00018D0001600001610001620001630001640001650001660102C0208001018002026F82060201FE02FF01C0808082A4C29A1DDE0E1AEA8D36858B59016A45DB4A4968A2D5A1073B8EABC842C1D5948080B58B1A7CE9848D3EC315C70183D08E6E8B21C0FDA15A7839445AEEA636C794BA4ED59903EADC60372A542D21D77BFFB3E65B5B8BA3FB14BCE7CDA91268B177BC");
        ITSCertificate caCert = loadCertificate(ca);

        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        X9ECParameters parameters = NISTNamedCurves.getByOID(SECObjectIdentifiers.secp256r1);
        generator.init(new ECKeyGenerationParameters(new ECNamedDomainParameters(SECObjectIdentifiers.secp256r1, parameters), rand));
        AsymmetricCipherKeyPair kp = generator.generateKeyPair();

        ECPublicKeyParameters publicVerificationKey = (ECPublicKeyParameters)kp.getPublic();
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters)kp.getPrivate();


        ToBeSignedCertificate.Builder tbsBuilder = new ToBeSignedCertificate.Builder();
        tbsBuilder.setAppPermissions(
            SequenceOfPsidSsp.builder()
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(622))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("0101")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(624))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("020138")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .createSequenceOfPsidSsp()); // App Permissions
        tbsBuilder.setAssuranceLevel(new SubjectAssurance(new byte[]{(byte)0xC0}));
        // builder.setCanRequestRollover(OEROptional.ABSENT);
        tbsBuilder.setCertIssuePermissions(
            SequenceOfPsidGroupPermissions.builder()
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(
                        SubjectPermissions.builder().explicit(
                            SequenceOfPsidSspRange.builder()
                                .add(PsidSspRange.builder()
                                    .setPsid(36).setSspRange(SspRange.builder().extension(Hex.decode("0301fffc03ff0003")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(37).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFFF04FF000000")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(137).setSspRange(SspRange.builder().extension(Hex.decode("0201E002FF1F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(138).setSspRange(SspRange.builder().extension(Hex.decode("0201C002FF3F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(139).setSspRange(SspRange.builder().extension(Hex.decode("0601000000FFF806FF0000000007")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(140).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFE004FF00001F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(141).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(96).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(97).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(98).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(99).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(100).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(101).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(102).createPsidSspRange())
                                .build()
                        ).createSubjectPermissions())
                    .setMinChainLength(2)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))

                    .createPsidGroupPermissions())
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(SubjectPermissions.builder()
                        .explicit(SequenceOfPsidSspRange.builder()
                            .add(PsidSspRange.builder()
                                .setPsid(623)
                                .setSspRange(
                                    SspRange.builder()
                                        .extension(Hex.decode("0201FE02FF01"))
                                        .createSspRange()).createPsidSspRange())
                            .build())
                        .createSubjectPermissions())
                    .setMinChainLength(1)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))
                    .createPsidGroupPermissions())
                .createSequenceOfPsidGroupPermissions());

        tbsBuilder.setCrlSeries(new CrlSeries(1));

        ITSContentSigner itsContentSigner = new BcITSContentSigner(new ECPrivateKeyParameters(privateKeyParameters.getD(), new ECNamedDomainParameters(SECObjectIdentifiers.secp256r1, privateKeyParameters.getParameters())));
        BcITSExplicitCertificateBuilder itsCertificateBuilder = new BcITSExplicitCertificateBuilder(itsContentSigner, tbsBuilder);

        itsCertificateBuilder.setValidityPeriod(ITSValidityPeriod.from(new Date()).plusYears(1));

        ITSCertificate newCert = itsCertificateBuilder.build(
            CertificateId.builder().name(new Hostname("Legion of the BouncyCastle CA")).createCertificateId(),
            publicVerificationKey);

        BcITSContentVerifierProvider provider = new BcITSContentVerifierProvider(newCert);
        boolean valid = newCert.isSignatureValid(provider);

        TestCase.assertTrue(valid);
    }

    public void testSelfSignedCA()
        throws Exception
    {
        byte[] ca = Hex.decode("800300810038811B45545349205465737420524341204320636572746966696361746500000000001A5617008466A8C001028002026E810201018002027081030201380102A080010E80012482080301FFFC03FF0003800125820A0401FFFFFF04FF00000080018982060201E002FF1F80018A82060201C002FF3F80018B820E0601000000FFF806FF000000000780018C820A0401FFFFE004FF00001F00018D0001600001610001620001630001640001650001660102C0208001018002026F82060201FE02FF01C0808082A4C29A1DDE0E1AEA8D36858B59016A45DB4A4968A2D5A1073B8EABC842C1D5948080B58B1A7CE9848D3EC315C70183D08E6E8B21C0FDA15A7839445AEEA636C794BA4ED59903EADC60372A542D21D77BFFB3E65B5B8BA3FB14BCE7CDA91268B177BC");
        ITSCertificate caCert = loadCertificate(ca);
        BcITSContentVerifierProvider provider = new BcITSContentVerifierProvider(caCert);
        boolean valid = caCert.isSignatureValid(provider);
        TestCase.assertTrue(valid);
    }

//    public void testBasicGeneration()
//        throws Exception
//    {
//
//        byte[] issuerRaw = Hex.decode("80030080c6f1125e19b175ee398300000000001a5617008466a88001018002026f810302013201012080010780012482080301fffc03ff0003800125820a0401ffffff04ff00000080018982060201e002ff1f80018a82060201c002ff3f80018b820e0601000000fff806ff000000000780018c820a0401ffffe004ff00001f00018dc0008082ce2a2219c94c6644d9f056ecc91ba39dfac64c5dd62f26b49e5fa51a28dd880e80808253b8a1bdc3e281fc950d4620e2bae0289df4c3cd34c9e716dc2f0ce022ff223a808072c44e228a92e9c8b7d74686ebb1481eac212d788c8f0a4d67b305210d95d6d22a518deab39110189e0463a677ce47328fdb902ae124bdd85ceaecccce148cac");
//        byte[] subjectRaw = Hex.decode("8003008034556d34931e5e5c318300000000001a56170084223860010380012481040301fffc80012581050401ffffff80018d810201000080826804fcef8c89168b7e4ffc0615ef7d64a02cb92456cb6d00baabb71d0adcc690808082b3d838f7d8851b97177a2d314ee6f1c7aadd7273619a4868ad8b0e34443a0e4180806c8e98b60ee6ccbc1d69e0e910c9230cdbbaf013061ca9bf97844bceda4dd0357900bddda50788111db4e833e8850924a2150a5afb5186c1e78908ee5c30af6d");
//
//        ITSCertificate issuer = loadCertificate(issuerRaw);
//        ITSCertificate subject = loadCertificate(subjectRaw);
//
//        ECKeyPairGenerator kpGen = new ECKeyPairGenerator();
//
//        kpGen.init(new ECKeyGenerationParameters(
//            new ECNamedDomainParameters(SECObjectIdentifiers.secp256r1, ECNamedCurveTable.getByName("P-256")), new SecureRandom()));
//
//        AsymmetricCipherKeyPair kp = kpGen.generateKeyPair();
//
//        ITSExplicitCertificateBuilder bldr = new ITSExplicitCertificateBuilder(subject.toASN1Structure().getCertificateBase().getToBeSignedCertificate());
//        BcITSContentSigner signer = new BcITSContentSigner((ECPrivateKeyParameters)kp.getPrivate(), issuer);
//
//        ITSCertificate genCert = bldr.buildExplicit(signer);
//
//        assertEquals(subject.getIssuer(), genCert.getIssuer());
////   TODO: signature creation is confirmed, but need a way of generating root certificate
////        BcITSContentVerifierProvider provider = new BcITSContentVerifierProvider(issuer);
////        boolean valid = genCert.isSignatureValid(provider);
////        TestCase.assertTrue(valid);
//    }

    public void testBasicVerification()
        throws Exception
    {

        byte[] rootCertRaw = Hex.decode("800300810038811B45545349205465737420524341204320636572746966696361746500000000001A5617008466A8C001028002026E810201018002027081030201380102A080010E80012482080301FFFC03FF0003800125820A0401FFFFFF04FF00000080018982060201E002FF1F80018A82060201C002FF3F80018B820E0601000000FFF806FF000000000780018C820A0401FFFFE004FF00001F00018D0001600001610001620001630001640001650001660102C0208001018002026F82060201FE02FF01C0808082A4C29A1DDE0E1AEA8D36858B59016A45DB4A4968A2D5A1073B8EABC842C1D5948080B58B1A7CE9848D3EC315C70183D08E6E8B21C0FDA15A7839445AEEA636C794BA4ED59903EADC60372A542D21D77BFFB3E65B5B8BA3FB14BCE7CDA91268B177BC");
        byte[] issuerRaw = Hex.decode("80030080c6f1125e19b175ee398300000000001a5617008466a88001018002026f810302013201012080010780012482080301fffc03ff0003800125820a0401ffffff04ff00000080018982060201e002ff1f80018a82060201c002ff3f80018b820e0601000000fff806ff000000000780018c820a0401ffffe004ff00001f00018dc0008082ce2a2219c94c6644d9f056ecc91ba39dfac64c5dd62f26b49e5fa51a28dd880e80808253b8a1bdc3e281fc950d4620e2bae0289df4c3cd34c9e716dc2f0ce022ff223a808072c44e228a92e9c8b7d74686ebb1481eac212d788c8f0a4d67b305210d95d6d22a518deab39110189e0463a677ce47328fdb902ae124bdd85ceaecccce148cac");
        byte[] subjectRaw = Hex.decode("8003008034556d34931e5e5c318300000000001a56170084223860010380012481040301fffc80012581050401ffffff80018d810201000080826804fcef8c89168b7e4ffc0615ef7d64a02cb92456cb6d00baabb71d0adcc690808082b3d838f7d8851b97177a2d314ee6f1c7aadd7273619a4868ad8b0e34443a0e4180806c8e98b60ee6ccbc1d69e0e910c9230cdbbaf013061ca9bf97844bceda4dd0357900bddda50788111db4e833e8850924a2150a5afb5186c1e78908ee5c30af6d");


        ITSCertificate rootCert = loadCertificate(rootCertRaw);
        ITSCertificate issuer = loadCertificate(issuerRaw);

        //
        // Verify issuer against root
        //
        BcITSContentVerifierProvider provider = new BcITSContentVerifierProvider(rootCert);
        boolean issuerValidAgainstRoot = issuer.isSignatureValid(provider);
        TestCase.assertTrue(issuerValidAgainstRoot);

        ITSCertificate subject = loadCertificate(subjectRaw);

        //
        // Verify subject against issuer
        //
        provider = new BcITSContentVerifierProvider(issuer);
        boolean valid = subject.isSignatureValid(provider);
        TestCase.assertTrue(valid);
    }


    public void testRoundTripEeEcaCertRequest()
        throws Exception
    {
        //
        // The issue is that as of 7-jan-2022 we have not been able to find
        // and real life examples of the version 2 requests so this is a basic
        // sanity test.
        //

        // TBS component
        ToBeSignedCertificate.Builder tbsBuilder = new ToBeSignedCertificate.Builder();
        tbsBuilder.setAppPermissions(
            SequenceOfPsidSsp.builder()
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(622))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("0101")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(624))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("020138")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .createSequenceOfPsidSsp()); // App Permissions
        tbsBuilder.setAssuranceLevel(new SubjectAssurance(new byte[]{(byte)0xC0}));
        // builder.setCanRequestRollover(OEROptional.ABSENT);
        tbsBuilder.setCertIssuePermissions(
            SequenceOfPsidGroupPermissions.builder()
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(
                        SubjectPermissions.builder().explicit(
                            SequenceOfPsidSspRange.builder()
                                .add(PsidSspRange.builder()
                                    .setPsid(36).setSspRange(SspRange.builder().extension(Hex.decode("0301fffc03ff0003")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(37).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFFF04FF000000")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(137).setSspRange(SspRange.builder().extension(Hex.decode("0201E002FF1F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(138).setSspRange(SspRange.builder().extension(Hex.decode("0201C002FF3F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(139).setSspRange(SspRange.builder().extension(Hex.decode("0601000000FFF806FF0000000007")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(140).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFE004FF00001F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(141).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(96).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(97).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(98).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(99).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(100).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(101).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(102).createPsidSspRange())
                                .build()
                        ).createSubjectPermissions())
                    .setMinChainLength(2)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))

                    .createPsidGroupPermissions())
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(SubjectPermissions.builder()
                        .explicit(SequenceOfPsidSspRange.builder()
                            .add(PsidSspRange.builder()
                                .setPsid(623)
                                .setSspRange(
                                    SspRange.builder()
                                        .extension(Hex.decode("0201FE02FF01"))
                                        .createSspRange()).createPsidSspRange())
                            .build())
                        .createSubjectPermissions())
                    .setMinChainLength(1)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))
                    .createPsidGroupPermissions())
                .createSequenceOfPsidGroupPermissions());

        tbsBuilder.setCrlSeries(new CrlSeries(1));
        tbsBuilder.setValidityPeriod(
            ValidityPeriod.builder()
                .setTime32(Time32.now())
                .setDuration(new Duration(Duration.years, 1))
                .createValidityPeriod());
        tbsBuilder.setCracaId(new HashedId3(Hex.decode("010203")));
        tbsBuilder.setVerificationKeyIndicator(VerificationKeyIndicator.builder()
            .publicVerificationKey(
                PublicVerificationKey.builder().ecdsaBrainpoolP256r1(EccP256CurvePoint.builder().createUncompressedP256(BigInteger.TEN, BigInteger.TEN)).createPublicVerificationKey())
            .createVerificationKeyIndicator());
        tbsBuilder.setCertificateId(CertificateId.builder().name(new Hostname("hostname")).createCertificateId());
        ToBeSignedCertificate tbs = tbsBuilder.createToBeSignedCertificate();


        EeEcaCertRequest cr = EeEcaCertRequest.builder()
            .setVersion(new Uint8(2))
            .setTbsCert(tbs)
            .setGenerationTime(Time32.now())
            .setType(CertificateType.Explicit)
            .setCanonicalId(new DERIA5String("test"))
            .createEeEcaCertRequest();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OEROutputStream oos = new OEROutputStream(bos);
        oos.write(cr, Ieee1609Dot2Dot1EcaEeInterface.EeEcaCertRequest.build());
        oos.flush();

        byte[] firstEncoding = bos.toByteArray();


        // Decode
        OERInputStream inputStream = new OERInputStream(new ByteArrayInputStream(firstEncoding));
        ASN1Object firstDecoding = inputStream.parse(Ieee1609Dot2Dot1EcaEeInterface.EeEcaCertRequest.build());


        // Encode again.
        bos = new ByteArrayOutputStream();
        oos = new OEROutputStream(bos);
        oos.write(firstDecoding, Ieee1609Dot2Dot1EcaEeInterface.EeEcaCertRequest.build());
        oos.flush();

        byte[] secondEncoding = bos.toByteArray();

        // First encoding and second encoding should be the same.
        assertTrue(Arrays.areEqual(firstEncoding, secondEncoding));

    }


    public void testRoundTripEeRaCertRequest()
        throws Exception
    {
        //
        // The issue is that as of 7-jan-2022 we have not been able to find
        // and real life examples of the version 2 requests so this is a basic
        // sanity test.
        //

        // TBS component
        ToBeSignedCertificate.Builder tbsBuilder = new ToBeSignedCertificate.Builder();
        tbsBuilder.setAppPermissions(
            SequenceOfPsidSsp.builder()
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(622))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("0101")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .setItem(PsidSsp.builder()
                    .setPsid(new Psid(624))
                    .setSsp(ServiceSpecificPermissions.builder()
                        .bitmapSsp(new DEROctetString(Hex.decode("020138")))
                        .createServiceSpecificPermissions())
                    .createPsidSsp())
                .createSequenceOfPsidSsp()); // App Permissions
        tbsBuilder.setAssuranceLevel(new SubjectAssurance(new byte[]{(byte)0xC0}));
        // builder.setCanRequestRollover(OEROptional.ABSENT);
        tbsBuilder.setCertIssuePermissions(
            SequenceOfPsidGroupPermissions.builder()
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(
                        SubjectPermissions.builder().explicit(
                            SequenceOfPsidSspRange.builder()
                                .add(PsidSspRange.builder()
                                    .setPsid(36).setSspRange(SspRange.builder().extension(Hex.decode("0301fffc03ff0003")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(37).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFFF04FF000000")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(137).setSspRange(SspRange.builder().extension(Hex.decode("0201E002FF1F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(138).setSspRange(SspRange.builder().extension(Hex.decode("0201C002FF3F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(139).setSspRange(SspRange.builder().extension(Hex.decode("0601000000FFF806FF0000000007")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder()
                                    .setPsid(140).setSspRange(SspRange.builder().extension(Hex.decode("0401FFFFE004FF00001F")).createSspRange()).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(141).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(96).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(97).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(98).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(99).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(100).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(101).createPsidSspRange())
                                .add(PsidSspRange.builder().setPsid(102).createPsidSspRange())
                                .build()
                        ).createSubjectPermissions())
                    .setMinChainLength(2)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))

                    .createPsidGroupPermissions())
                .addGroupPermission(PsidGroupPermissions.builder()
                    .setSubjectPermissions(SubjectPermissions.builder()
                        .explicit(SequenceOfPsidSspRange.builder()
                            .add(PsidSspRange.builder()
                                .setPsid(623)
                                .setSspRange(
                                    SspRange.builder()
                                        .extension(Hex.decode("0201FE02FF01"))
                                        .createSspRange()).createPsidSspRange())
                            .build())
                        .createSubjectPermissions())
                    .setMinChainLength(1)
                    .setChainLengthRange(0)
                    .setEeType(new EndEntityType(0xC0))
                    .createPsidGroupPermissions())
                .createSequenceOfPsidGroupPermissions());

        tbsBuilder.setCrlSeries(new CrlSeries(1));
        tbsBuilder.setValidityPeriod(
            ValidityPeriod.builder()
                .setTime32(Time32.now())
                .setDuration(new Duration(Duration.years, 1))
                .createValidityPeriod());
        tbsBuilder.setCracaId(new HashedId3(Hex.decode("010203")));
        tbsBuilder.setVerificationKeyIndicator(VerificationKeyIndicator.builder()
            .publicVerificationKey(
                PublicVerificationKey.builder().ecdsaBrainpoolP256r1(EccP256CurvePoint.builder().createUncompressedP256(BigInteger.TEN, BigInteger.TEN)).createPublicVerificationKey())
            .createVerificationKeyIndicator());
        tbsBuilder.setCertificateId(CertificateId.builder().name(new Hostname("hostname")).createCertificateId());
        ToBeSignedCertificate tbs = tbsBuilder.createToBeSignedCertificate();


        EeRaCertRequest cr = EeRaCertRequest.builder()
            .setVersion(new Uint8(2))
            .setTbsCert(tbs)
            .setGenerationTime(Time32.now())
            .setType(CertificateType.Explicit)
            .setAdditionalParams(AdditionalParams.builder().compactUnified(
                ButterflyExpansion.builder().aes128(new DEROctetString(new byte[16])).build()
            ).build())
            .createEeRaCertRequest();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OEROutputStream oos = new OEROutputStream(bos);
        oos.write(cr, Ieee1609Dot2Dot1EeRaInterface.EeRaCertRequest.build());
        oos.flush();

        byte[] firstEncoding = bos.toByteArray();


        // Decode
        OERInputStream inputStream = new OERInputStream(new ByteArrayInputStream(firstEncoding));
        ASN1Object firstDecoding = inputStream.parse(Ieee1609Dot2Dot1EeRaInterface.EeRaCertRequest.build());


        // Encode again.
        bos = new ByteArrayOutputStream();
        oos = new OEROutputStream(bos);
        oos.write(firstDecoding, Ieee1609Dot2Dot1EeRaInterface.EeRaCertRequest.build());
        oos.flush();

        byte[] secondEncoding = bos.toByteArray();

        // First encoding and second encoding should be the same.
        assertTrue(Arrays.areEqual(firstEncoding, secondEncoding));

    }


//
//    static class PrintingInputStream
//        extends FilterInputStream
//    {
//
//        /**
//         * Creates a <code>FilterInputStream</code>
//         * by assigning the  argument <code>in</code>
//         * to the field <code>this.in</code> so as
//         * to remember it for later use.
//         *
//         * @param in the underlying input stream, or <code>null</code> if
//         *           this instance is to be created without an underlying stream.
//         */
//        protected PrintingInputStream(InputStream in)
//        {
//            super(in);
//        }
//
//        public int read()
//            throws IOException
//        {
//            int i = in.read();
//            byte[] b = new byte[]{(byte)i};
//            System.out.print(Hex.toHexString(b));
//            return i;
//        }
//
//        @Override
//        public int read(byte[] b)
//            throws IOException
//        {
//            int i = super.read(b);
//            System.out.print(Hex.toHexString(b));
//            return i;
//        }
//
//        @Override
//        public int read(byte[] b, int off, int len)
//            throws IOException
//        {
//            int i = super.read(b, off, len);
//            System.out.print(Hex.toHexString(b, off, len));
//            return i;
//        }
//    }

}



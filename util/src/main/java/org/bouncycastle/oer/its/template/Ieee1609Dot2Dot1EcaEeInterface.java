package org.bouncycastle.oer.its.template;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.oer.OERDefinition;


public class Ieee1609Dot2Dot1EcaEeInterface
{
    /**
     * EeEcaCertRequest ::= SEQUENCE {
     * version         Uint8 (2),
     * generationTime  Time32,
     * type            CertificateType,
     * tbsCert         ToBeSignedCertificate (WITH COMPONENTS {
     * ...,
     * id (WITH COMPONENTS {
     * ...,
     * linkageData ABSENT
     * }),
     * cracaId ('000000'H),
     * crlSeries (0),
     * appPermissions ABSENT,
     * certIssuePermissions ABSENT,
     * certRequestPermissions PRESENT,
     * verifyKeyIndicator (WITH COMPONENTS {
     * verificationKey
     * })
     * }),
     * canonicalId     IA5String OPTIONAL,
     * ...
     * }
     */
    public static OERDefinition.Builder EeEcaCertRequest = OERDefinition.seq(
        Ieee1609Dot2BaseTypes.UINT8.label("version").defaultValue(new ASN1Integer(2)),
        Ieee1609Dot2BaseTypes.Time32.label("generationTime"),
        IEEE1609dot2.CertificateType.label("type"),
        IEEE1609dot2.ToBeSignedCertificate.label("tbsCert"),
        OERDefinition.optional(OERDefinition.ia5String().label("canonicalId")),
        OERDefinition.extension()
    ).label("EeEcaCertRequest");
}

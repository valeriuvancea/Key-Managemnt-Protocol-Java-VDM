# Messages
## BroadcastToSearchKeyVault
* type
* controller_id

## KeyVaultSearchBroadcastAcknowledgement
* type
* controller_id

## JoinRequest
* type
* controller_id
* cert_ct

## SendChallenge
* type
* controller_id
* encrypted_challenge

## SendChallengeAnswer
* type
* controller_id
* decrypted_challenge

## SendKeyVaultCertificate
* type
* controller_id
* cert_kv --- check it is the same for every controller

## SendNewEffectivePublicKey
* type
* controller_id
* pk_eff --- check it different between re-keying and it should be unique among all controllers
* hash --- it should be unique among all controllers, should have some length

## SendNewEffectiveCertificate
* type
* controller_id
* cert_eff --- check it different between re-keying and it should be unique among all controllers
* cert_ca --- check it is the same for every controller

## SendNewEffectiveCertificateAcknoledgement
* type
* controller_id

## SendReKeyRequest
* type
* controller_id

## BroadcastToSearchControllers
* type
* controller_id

## ControllerSearchBroadcastAcknowledgement
* type
* controller_id, --- should be diff then sender
* sender_id,
* cert_eff --- should bw the same associated with the sender id

## DummyMessage
* type
* controller_id --- should be diff then sender
* sender_id,
* encrypted_data

# TPM usage
## Generate random numbers

    openssl rand -engine tpm2tss -hex 10 

## Keys/Encryption/Decryption/Signing/Verification
Examples can be found: https://github.com/tpm2-software/tpm2-tss-engine

# TPMs used in:
* GenerateChallenge
* DecryptChallenge
* GetSigningRequestSignature
* GenerateEffectivePendingKeys
* GenerateKeyPairTPM
* DecryptMessageFromOtherController

# VDMAnnotations

## Controller

1. Verify generated challenge length.
2. Verify that the encrypted challenge is different from the original one. 
3. Verify that the challenge being encrypted is the same as the generated challenge.
4. Verify that the checked challenge matches the generated challenge.
    GENERATED_CheckChallengeAnswer. Stash the challenge when it is being generated. Verify that the challenge checked (as received from the reply) matches the on that was generated.
5. Verify that the check of the challenge yields true.
    GENERATED_CheckChallengeAnswer
6. Verify that the key vault certificate verification yields true.
    GENERATED_CheckKeyVaultCertificate
7. Verify that the function generating the signature for signing request is always presented with the pending effective public key value.
    Stash the pending key when it is retrieved. Check that the stashed value matches when the signature is being generated. 
8. Verify that the function generating signing request always returns an unique value.
    Stash all values generated. Have an invariant for no duplicates.
9. Make sure when new effective keys are saved, they match the previously generated pending effective keys.
10. Make sure that saved effective certificate and public and private key values never repeat.

## Key vault
1. Verify generated challenge length.
2. Verify that the encrypted challenge is different from the original one. 
3. Verify that the challenge being encrypted is the same as the generated challenge.
4. Verify that the checked challenge matches the generated challenge.
5. Verify that the check of the challenge yields true.
6. Check that controller certificate check yields true.
    GENERATED_CheckControllerCertificate
7. Check that no duplicate controller certificate is checked - tested.
    GENERATED_CheckControllerCertificate stashes the certificate. Invariant makes sure there's no duplicates.
8. Check no duplicate effective key is received.
9. Check that effective certificates are only generated for a key that has been received.
    GENERATED_GenerateEffectiveCertificate. Stash all received effective keys. Make sure the generate effective certificate operation is only on those keys.
10. Check that no duplicate effective certificate is generated.
11. Check that signatures are only generated for signed certificates.
12. Check that only one value of a certificate authority's certificate is used for generating effective certificates.
    GENERATED_GetEffectiveCertificateSignature generates signature for the message delivering cert_ca. Make sure the value of the cert_ca is always the same.
13. Check that all generated signatures are unique.

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

# Known issues
* Controllers do not verify the key vault id to be the same as the one in the certificate presented by the key vault
* Controllers do not verify the received cert_eff from the key vault

# Temp
http://:8080/message?contents={"encrypted_data": "hello world", "controller_id": "controller_1", "type": "DummyMessage"}
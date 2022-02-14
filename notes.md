# NOTES ON PIPELIN

## Signing

<https://developer.android.com/studio/publish/app-signing>

Generate a key with a password that needs to be loaded into the pipeline on Azure's side.

```bash
keytool -genkey -v -keystore akros.online.keystore -alias akros.online -keyalg RSA -sigalg SHA1withRSA -keysize 2048 -validity 10000
```

// Signing
jarsigner -keystore /opt/keystore.jks -storepass {{PASSWORD}} reveal-client/build/outputs/apk/release/reveal-client-release-unsigned.apk akros.online && /home/fanie/Android/Sdk/build-tools/29.0.3/zipalign 4 reveal-client/build/outputs/apk/release/reveal-client-release-unsigned.apk reveal-client/build/outputs/apk/release/reveal-client-release.apk

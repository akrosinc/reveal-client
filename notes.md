# NOTES ON PIPELIN

## Signing

<https://developer.android.com/studio/publish/app-signing>

Generate a key with a password that needs to be loaded into the pipeline on Azure's side.

```bash
keytool -genkey -v -keystore akros.online.keystore -alias akros.online -keyalg RSA -sigalg SHA1withRSA -keysize 2048 -validity 10000
```

// Signing
jarsigner -keystore keystore.jks -storepass {{PASSWORD}} reveal-client/build/outputs/apk/release/reveal-client-release-unsigned.apk upload && zipalign 4 reveal-client/build/outputs/apk/release/reveal-client-release-unsigned.apk reveal-client/build/outputs/apk/release/reveal-client-release.apk"

curl -v -u user:password --upload-file ./reveal-client/build/outputs/apk/release/app-release.apk https://OUR-ARTIFACTS-REPO/repository/reveal-client/release/reveal-client-release-${env.BUILD_NUMBER}.apk
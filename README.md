# MagicalXmppSDK
This repo is a magical SDK that can connect to XMPP server with smack client with send &amp; receive functionality ;)

# Usage
You need to just initialize the builder of the SDK like this example

```JAVA
magicalXmppSDKInstance = MagicalXmppSDK.Builder(this@MainActivity)
            .setUsername(PublicValue.TEST_USERNAME)
            .setPassword(PublicValue.TEST_PASSWORD)
            .setDomain(PublicValue.TEST_DOMAIN)
            .setHost(PublicValue.TEST_HOST)
            .setPort(PublicValue.TEST_PORT)
            .setCallback(this)
            .build()
```

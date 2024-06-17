# PGP-GUI

GUI wrapping the PGP function provided by Legion of the Bouncy Castle

The program allows you to encrypt/sign/decrypt text messages and files using asymmetric PGP keys.

The program can read and manage GnuPG keys if GnuPG is installed.
But the program also maintains its own secret and public key files and having GnuPG installed is optional.

## Features

* Encrypt
* Decrypt
* PGP signing
* Key management

See [About Page](./src/main/resources/help.html) for details.

## Build

Execute `mvn package` and the resulted app jar will be built in the `/target/` folder.
You can start it with:

    java -jar target/pgpgui-1.1-SNAPSHOT-jar-with-dependencies.jar

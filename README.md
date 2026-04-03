# PGP-GUI

GUI wrapping the PGP function provided by Legion of the Bouncy Castle

The program allows you to encrypt/sign/decrypt text messages and files using asymmetric PGP keys.

The program can read and manage GnuPG keys if GnuPG is installed.
But the program also maintains its own secret and public key files and having GnuPG installed is optional.

PGP key handling can be a bit cumbersome, therefore static password encrypt/decrypt has been added.

## Features

* Encrypt
* Decrypt
* PGP signing
* Key management
* In addition there is also static password encryption/decryption fo the ones that does not have the patiance 
  to deal with PGP keys

See [About Page](./src/main/resources/help.html) for details.

## Build
Execute `gradle build` and the resulted app jar will be built in the `/build/libs/` folder.
You can start the app with the bash script run`run-pgpui.sh`

Execute `mvn package` and the resulted app jar will be built in the `/target/` folder.
You can start it with:

    java -jar target/pgpgui-1.5-SNAPSHOT-jar-with-dependencies.jar


## Distribution
The build script will compile a zip and tar file containing the required files for running the app.
When unpacking the zip/tar a directory `./pgp-ui/` will be created. In the directory the run file 
pgp-ui.sh will start the app.

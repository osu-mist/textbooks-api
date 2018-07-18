# Textbooks API

Allows users to retrieve information on textbooks for different classes.

## Endpoints

### GET

#### /textbooks

Retrieve textbooks based on parameters.\

Example request:
```
GET /textbooks
    ?academicYear=2018
    &term=Fall,
    &subject=HST,
    &courseNumber=101,
    &section=001
```

Example response:
```json
{
    "links": null,
    "data": [
        {
            "id": "9780393265392",
            "type": "textbook",
            "attributes": {
                "coverImageUrl": "//coverimages.verbacompete.com/425589bb-0f05-52c4-b6e9-964d849b9998.jpg",
                "title": "Perspectives From The Past Vol 1",
                "author": "Brophy",
                "edition": 6,
                "copyright_year": 2016,
                "priceNewUSD": 40.5,
                "priceUsedUSD": 30.5
            },
            "links": null
        },
        {
            "id": "9780393650402",
            "type": "textbook",
            "attributes": {
                "coverImageUrl": "//coverimages.verbacompete.com/no_image.jpg",
                "title": "Western Civilization 19th Ed Vol 1 W/Perspectives From The Past 6th Ed Pkg",
                "author": "Cole (2)",
                "edition": null,
                "copyright_year": null,
                "priceNewUSD": 120.25,
                "priceUsedUSD": null
            },
            "links": null
        }
    ]
}
```

# Skeleton

This project is based on the [Web API Skeleton](https://github.com/osu-mist/web-api-skeleton). This section will cover information related to the skeleton required to run the API.

## Generate Keys

HTTPS is required for Web APIs in development and production. Use keytool(1) to generate public and private keys.

Generate key pair and keystore:

```
$ keytool \
  -genkeypair \
  -dname "CN=Jane Doe, OU=Enterprise Computing Services, O=Oregon State University, L=Corvallis, S=Oregon, C=US" \
  -ext "san=dns:localhost,ip:127.0.0.1" \
  -alias doej \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -validity 365 \
  -keystore doej.keystore
```

Export certificate to file:

```
$ keytool \
  -exportcert \
  -rfc \
  -alias "doej" \
  -keystore doej.keystore \
  -file doej.pem
```

Import certificate into truststore:

```
$ keytool \
  -importcert \
  -alias "doej" \
  -file doej.pem \
  -keystore doej.truststore
```

## Gradle

This project uses the build automation tool Gradle. Use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to download and install it automatically:

```
$ ./gradlew
```

The Gradle wrapper installs Gradle in the directory `~/.gradle`. To add it to your `$PATH`, add the following line to `~/.bashrc`:

```
$ export PATH=$PATH:/home/user/.gradle/wrapper/dists/gradle-2.4-all/WRAPPER_GENERATED_HASH/gradle-2.4/bin
```

The changes will take effect once you restart the terminal or `source ~/.bashrc`.

## Tasks

List all tasks runnable from root project:

```
$ gradle tasks
```

## IntelliJ IDEA

Generate IntelliJ IDEA project:

```
$ gradle idea
```

Open with `File` -> `Open Project`.

## Configure

Copy [configuration-example.yaml](configuration-example.yaml) to `configuration.yaml`. Modify as necessary, being careful to avoid committing sensitive data.

### Build

Build the project:

```
$ gradle build
```

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

## Run

Run the project:

```
$ gradle run
```

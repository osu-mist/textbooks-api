# Textbooks API Integration Tests

## Usage

1. Install dependencies:
```bash
$ pip3 install -r requirements.txt
```

2. Copy configuration_example.json to configuration.json and modify as necessary.

3. Run the integration tests:
```bash
$ python3 integration_tests.py -i configuration.json [-v]
```

## Docker
Build and run the Docker image:
```bash
$ docker build --rm -t textbooks-api-integration-tests .
$ docker run \
      -v "$PWD"/configuration.json:/usr/src/app/configuration.json:ro \
      textbooks-api-integration-tests
```

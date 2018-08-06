FROM python:3

WORKDIR /usr/src/app

COPY . .

RUN pip3 install -r requirements.txt

ENTRYPOINT [ "python3" ]
CMD [ "integration_tests.py", "-i", "configuration.json", "-v" ]

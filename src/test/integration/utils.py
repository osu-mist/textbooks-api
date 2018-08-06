import argparse
import sys
import requests


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("-i", help="path to input file", dest="input_file")
    namespace, args = parser.parse_known_args()
    return namespace, sys.argv[:1] + args


def set_local_vars(config):
    global url
    url_pieces = (config["hostname"], config["version"], config["api"])
    url = url_joiner(url_pieces)

    global session
    session = requests.Session()
    if config["use_basic_auth"]:
        basic_auth = config["basic_auth"]
        session.auth = (basic_auth["username"], basic_auth["password"])
        session.verify = False
    else:
        session.headers = get_oauth2_headers(config)


def get_oauth2_headers(config):
    token = "access_token"
    oauth2 = config["oauth2"]
    data = {
        "client_id": oauth2["client_id"],
        "client_secret": oauth2["client_secret"],
        "grant_type": "client_credentials"
    }
    res = requests.post(url=oauth2["token_api_url"], data=data)
    if token not in res.json():
        sys.exit("Error: invalid OAUTH2 credentials")
    return {"Authorization": "Bearer {}".format(res.json()[token])}


def get_textbooks(params=None):
    return session.get(url=url, params=params)


def url_joiner(url_pieces):
    return "/".join(str(piece).strip("/") for piece in url_pieces)

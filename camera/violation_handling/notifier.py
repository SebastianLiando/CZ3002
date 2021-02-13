import json
import os
import requests


class Notifier:
    def __init__(self):
        dir_path = os.path.dirname(__file__)
        config_file_path = os.path.join(dir_path, 'config.json')

        with open(config_file_path) as config_file:
            config = json.load(config_file)

        project_id = config['project_id']
        self.url = f'https://{project_id}.firebaseio.com/violations.json'

    def notify(self, data: dict):
        response = requests.post(self.url, json=data)

        print(f'status code: {response.status_code}')
        print(f'response: {response.json()}')

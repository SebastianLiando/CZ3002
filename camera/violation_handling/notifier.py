import json
import os
import threading

from google.auth.transport.requests import AuthorizedSession
from google.oauth2 import service_account


def notify(
    authorized_session: AuthorizedSession,
    url: str,
    violation_info: dict
):
    response = authorized_session.post(url, json=violation_info)

    print(f'status code: {response.status_code}')
    print(f'response: {response.json()}')


class Notifier:
    def __init__(self):
        dir_path = os.path.dirname(__file__)

        config_file_path = os.path.join(dir_path, 'config.json')
        with open(config_file_path) as config_file:
            config = json.load(config_file)

        project_id = config['project_id']
        self.url = f'https://{project_id}.firebaseio.com/violations.json'

        key_file_path = os.path.join(dir_path, 'key.json')
        scopes = [
            'https://www.googleapis.com/auth/userinfo.email',
            'https://www.googleapis.com/auth/firebase.database',
        ]
        credentials = service_account.Credentials.from_service_account_file(
            key_file_path,
            scopes=scopes,
        )
        
        self.authorized_session = AuthorizedSession(credentials)

        print('instantiated notifier')

    def notify(self, violation_info: dict):
        threading.Thread(
            target=notify,
            args=(self.authorized_session, self.url, violation_info)
        ).start()


if __name__ == '__main__':
    from datetime import datetime

    notifier = Notifier()
    notifier.notify({
        'camera_id': '0',
        'toilet_gender': 'female',
        'person_gender': 'male',
        'date_time': str(datetime.now()),
    })

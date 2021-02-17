import firebase_admin
import json
import os
import threading

from firebase_admin import credentials
from firebase_admin import db


def notify(
    database_reference: db.Reference,
    violation_info: dict,
):
    database_reference.push(violation_info)


class Notifier:
    def __init__(self):
        # config and key should be placed in same directory
        dir_path = os.path.dirname(__file__)

        config_file_path = os.path.join(dir_path, 'config.json')
        with open(config_file_path) as config_file:
            config = json.load(config_file)

        project_id = config['project_id']
        database_url = f'https://{project_id}.firebaseio.com/violations.json'

        key_file_path = os.path.join(dir_path, 'key.json')
        credential = credentials.Certificate(key_file_path)

        firebase_admin.initialize_app(
            credential=credential,
            options={
                'databaseURL': database_url,
            }
        )

        self.database_reference = db.reference('violations')

        print('instantiated notifier')

    def notify(self, violation_info: dict):
        threading.Thread(
            target=notify,
            args=(self.database_reference, violation_info)
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

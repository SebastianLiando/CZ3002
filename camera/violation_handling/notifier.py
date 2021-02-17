import firebase_admin
import json
import numpy as np
import os
import threading

from base64 import b64encode
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import storage
from uuid import uuid4


def notify(
    database_reference: db.Reference,
    storage_bucket,
    violation_info: dict,
    image: np.ndarray,
):
    image_id = str(uuid4())
    encoded_image = b64encode(image)

    blob = storage_bucket.blob(image_id)
    blob.upload_from_string(encoded_image, content_type='image/png')

    violation_info['imageId'] = image_id
    database_reference.push(violation_info)


class Notifier:
    def __init__(self):
        # config and key should be placed in same directory
        dir_path = os.path.dirname(__file__)

        config_file_path = os.path.join(dir_path, 'config.json')
        with open(config_file_path) as config_file:
            config = json.load(config_file)

        key_file_path = os.path.join(dir_path, 'key.json')
        credential = credentials.Certificate(key_file_path)

        firebase_admin.initialize_app(
            credential=credential,
            options={
                'databaseURL': config['databaseURL'],
                'storageBucket': config['storageBucket'],
            },
        )

        self.database_reference = db.reference('violations')
        self.storage_bucket = storage.bucket()

        print('instantiated notifier')

    def notify(self, violation_info: dict, image: np.ndarray):
        threading.Thread(
            target=notify,
            args=(
                self.database_reference,
                self.storage_bucket,
                violation_info,
                image,
            ),
        ).start()

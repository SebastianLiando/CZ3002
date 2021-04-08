import cv2
import firebase_admin
import io
import json
import numpy as np
import os
import threading

from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import messaging
from firebase_admin import storage
from uuid import uuid4


def notify(
    database_reference: db.Reference,
    storage_bucket: storage.storage.bucket.Bucket,
    violation_info: dict,
    image: np.ndarray,
):
    '''
    Notify frontend of violation

    :param database_reference: (db.Reference) the Realtime Data for sending violation information to  
    :param storage_bucket: the Google Storage Bucket for sending the image to  
    :param violation_info: (dict) information regarding the violation  
    :param image: (np.ndarray) image captured of the perpetrator
    '''
    try:
        image_id = str(uuid4())
        
        # encode image for sending
        _, encoded_image = cv2.imencode('.png', image)
        io_buffer = io.BytesIO(encoded_image)

        # send image to Cloud Storage
        blob = storage_bucket.blob(image_id)
        blob.upload_from_file(io_buffer, content_type='image/png')

        # update Realtime Database
        violation_info['imageId'] = image_id
        database_reference.push(violation_info)

        location = violation_info['location']
        message = messaging.Message(
            data={'imageId': image_id},
            notification=messaging.Notification(
                title='violation detected',
                body=f'location: {location}'
            ),
            topic=location,
        )

        # send notification with Cloud Messaging
        response = messaging.send(message)
        print(f'successfully sent notification: {response}')
    
    except Exception as error:
        print(f'failed to send notification - {error}')


class Notifier:
    '''
    Handles sending of notification when a violation occurs

    :param database_url: (str) database URL copied from Firebase console  
    :param storage_bucket: (str) storage bucket copied from Firebase console  
    :param key_file_path: (str) path to key for notification SDK  
    '''
    def __init__(
        self,
        database_url: str,
        storage_bucket: str,
        key_file_path: str,
    ):

        credential = credentials.Certificate(key_file_path)

        firebase_admin.initialize_app(
            credential=credential,
            options={
                'databaseURL': database_url,
                'storageBucket': storage_bucket,
            },
        )

        self.database_reference = db.reference('violations')
        self.storage_bucket = storage.bucket()

        print('instantiated notifier')

    def notify(self, violation_info: dict, image: np.ndarray):
        '''
        Notify frontend of violation

        :param violation_info: (dict) information regarding the violation  
        :param image: (np.ndarray) image captured of the perpetrator
        '''
        threading.Thread(
            target=notify,
            args=(
                self.database_reference,
                self.storage_bucket,
                violation_info,
                image,
            ),
        ).start()

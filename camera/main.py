'''
Demo for face detection and gender recognition using computer's webcam
'''
import argparse
import cv2
import json
import mxnet as mx
import numpy as np
import os

from camera import Camera
from face_detection import LFFD
from gender_classification import SSRNet
from violation_handling import Notifier


def parse_args():
    '''
    Parse arguments for this program
    '''
    parser = argparse.ArgumentParser(
        prog='camera',
        description='detect face and recognise gender using image from camera'
    )

    parser.add_argument(
        '--debug',
        action='store_true',
        help='whether to run in debug mode'
    )

    parser.add_argument(
        '--config_file_path',
        type=str,
        default='configurations/camera_config.json',
        help='path to configuration file for camera',
    )

    parser.add_argument(
        '--key_file_path',
        type=str,
        default='configurations/key.json',
        help='path to key for notification SDK',
    )

    return parser.parse_args()


def main(debug: bool, config: dict, key_file_path: str,):
    '''
    The main program

    :param debug: (bool) whether to show what the camera sees
    :param config: (dict) camera configurations
    :param key_file_path: (str) path to key for notification SDK  
    '''
    context = mx.cpu()
    
    face_detector_config = config['face_detection']
    face_detector = LFFD(
        symbol_file_path=face_detector_config['lffd_symbol_file_path'],
        model_file_path=face_detector_config['lffd_model_file_path'],
    )

    gender_classifier_config = config['gender_classification']
    gender_classifier = SSRNet(
        prefix=gender_classifier_config['ssrnet_prefix'],
        epoch=gender_classifier_config['ssrnet_epoch_num'],
    )

    notifier_config = config['violation_handling']
    notifier = Notifier(
        database_url=notifier_config['database_url'],
        storage_bucket=notifier_config['storage_bucket'],
        key_file_path=key_file_path,
    )

    camera_info = config['camera_information']
    camera = Camera(
        toilet_gender=camera_info['toilet_gender'],
        toilet_location=camera_info['toilet_location'],
        notification_interval=camera_info['notification_interval'],
        face_detector=face_detector,
        gender_classifier=gender_classifier,
        notifier=notifier,
    )
    camera.run(debug=debug)


if __name__ == '__main__':
    args = parse_args()

    with open(args.config_file_path) as config_file:
        config = json.load(config_file)

    main(args.debug, config, args.key_file_path)
    print('\ncamera stopped')

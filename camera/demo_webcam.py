'''
Demo for face detection and gender recognition using computer's webcam
'''
import argparse
import cv2
import mxnet as mx
import numpy as np
import os

from face_detection.lffd import LFFD
from gender_recognition.ssrnet import SSRNet


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
        '--use_cpu',
        action='store_true',
        help='whether to use cpu (poor results)',
    )

    parser.add_argument(
        '--lffd_symbol_file_path',
        type=str,
        default='face_detection/model/symbol_10_560_25L_8scales_v1_deploy.json',
        help='path to symbol file for face detection model',
    )

    parser.add_argument(
        '--lffd_model_file_path',
        type=str,
        default='face_detection/model/train_10_560_25L_8scales_v1_iter_1400000.params',
        help='path to model params for face detection model',
    )

    parser.add_argument(
        '--ssrnet_prefix',
        type=str,
        default='gender_recognition/ssr2_imdb_gender/model',
        help='prefix for gender recognition model',
    )

    return parser.parse_args()


def main(args):
    '''
    The main program
    '''
    video_capture = cv2.VideoCapture(0)

    if not video_capture.isOpened():
        print('failed to open camera')
        return

    # context = mx.gpu(0) if args.use_gpu else mx.cpu()

    if args.use_cpu:
        print('using CPU')
        context = mx.cpu()
    else:
        print('using GPU')
        context = mx.gpu(0)
    
    print()

    if not os.path.exists(args.lffd_symbol_file_path):
        print('The symbol file does not exist!')
        exit(1)
    
    if not os.path.exists(args.lffd_model_file_path):
        print('The model file does not exist!')
        exit(1)

    face_detector = LFFD(
        context=context,
        symbol_file_path=args.lffd_symbol_file_path,
        model_file_path=args.lffd_model_file_path,
    )

    # TODO: check params and symbol for SSRNet exist

    gender_recogniser = SSRNet(
        context=context,
        prefix=args.ssrnet_prefix,
        epoch=0,
    )

    try:
        while True:
            read_success, frame = video_capture.read()

            if not read_success:
                print('failed to read next frame from camera')
                break

            bboxes = face_detector.predict(
                frame,
                resize_scale=1,
                score_threshold=0.6,
                top_k=20,
                NMS_threshold=0.4,
                NMS_flag=True,
                skip_scale_branch_list=[],
            )

            for bbox in bboxes:
                confidence = bbox[-1]
                bbox = tuple(map(int, bbox[:4]))  # convert to int

                if bbox[2] <= bbox[0] or bbox[3] <= bbox[1]:
                    continue  # empty bbox

                face = frame[bbox[1]: bbox[3], bbox[0]: bbox[2]]
                gender = gender_recogniser.predict(face)

                if args.debug:
                    frame = cv2.rectangle(
                        frame,
                        (bbox[0], bbox[1]),  # top-left point
                        (bbox[2], bbox[3]),  # bottom-right point
                        color=(0, 255, 0),  # green
                        thickness=2,
                    )

                    frame = cv2.putText(
                        frame,
                        f'{gender} {confidence:.2f}',
                        (bbox[0], bbox[1] + 25),  # top-left point
                        fontFace=cv2.FONT_HERSHEY_SIMPLEX,
                        fontScale=1,
                        color=(0, 255, 0),  # green
                        thickness=2,
                    )

            if args.debug:
                cv2.imshow('camera', frame)

            if cv2.waitKey(1) == ord('q'):  # press q to stop
                break
    except KeyboardInterrupt:
        print('stopping camera...')
    finally:
        video_capture.release()
        cv2.destroyAllWindows()


if __name__ == '__main__':
    args = parse_args()
    main(args)
    print('\ncamera stopped')

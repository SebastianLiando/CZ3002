import cv2
import numpy as np
import time

from face_detection import FaceDetector
from gender_classification import GenderClassifier
from violation_handling import Notifier


class Camera:
    '''
    Simulated camera.
    Able to perform face detection and gender classification.

    :param toilet_gender: (int) toilet gender [1 for male, 0 for female]  
    :param toilet_location: (str) where the toilet is; no spaces allowed  
    :param notification_interval: (int) time in seconds between each notification  
    :param face_detector: (FaceDetector) face detection model  
    :param gender_classifier: (GenderClassifier) gender classification model  
    :param notifier: (Notifier) object that handles sending notification  
    '''
    def __init__(
        self,
        toilet_gender: int,
        toilet_location: str,
        notification_interval: int,
        face_detector: FaceDetector,
        gender_classifier: GenderClassifier,
        notifier: Notifier,
    ):
        self.gender_map = {1: 'male', 0: 'female'}

        self.toilet_gender = toilet_gender
        self.toilet_location = toilet_location
        self.notification_interval = notification_interval
        self.face_detector = face_detector
        self.gender_classifier = gender_classifier
        self.notifier = notifier

        # so that camera can notify immediately
        self.last_violation_time = time.time() - notification_interval

    def run(
        self,
        debug: bool = False,
        score_threshold: float = 0.8,
        top_k: int = 5,
        nms_threshold: float = 0.4,
        left_padding: int = 20,
        top_padding: int = 50,
        right_padding: int = 20,
        bottom_padding: int = 30,
    ):
        '''
        Run the camera

        :param debug: (bool) whether to show what the camera sees [default: False]  
        :param score_threshold: (float) threshold for face detection confidence score, below which bounding box is discarded [default: 0.8]  
        :param top_k: (int) maximum number of faces that can be detected [default: 5]  
        :param nms_threshold: (float) overlap threshold for non-maximum suppression, above which bounding box is discarded [default: 0.4]  
        :param left_padding: (int) amount to pad bounding box on the left by, when cropping image for gender classification [default: 20]  
        :param top_padding: (int) amount to pad bounding box on top by, when cropping image for gender classification [default: 50]  
        :param right_padding: (int) amount to pad bounding box on the right by, when cropping image for gender classification [default: 20]  
        :param bottom_padding: (int) amount to pad bounding box at the bottom by, when cropping image for gender classification [default: 30]  
        '''
        video_capture = cv2.VideoCapture(0)  # use web cam

        if not video_capture.isOpened():
            print('failed to open camera')
            return

        frame_width = int(video_capture.get(cv2.CAP_PROP_FRAME_WIDTH))
        frame_height = int(video_capture.get(cv2.CAP_PROP_FRAME_HEIGHT))

        try:
            while True:
                read_success, frame = video_capture.read()

                if not read_success:
                    print('failed to read next frame from camera')
                    break

                bboxes = self.face_detector.predict(
                    frame,
                    resize_scale=1,
                    score_threshold=score_threshold,
                    top_k=top_k,
                    nms_threshold=nms_threshold,
                    nms_flag=True,
                    skip_scale_branch_list=[],
                )

                self.__handle_bboxes(
                    debug=debug,
                    frame=frame,
                    bboxes=bboxes,
                    frame_width=frame_width,
                    frame_height=frame_height,
                    left_padding=left_padding,
                    top_padding=top_padding,
                    right_padding=right_padding,
                    bottom_padding=bottom_padding,
                )

                if cv2.waitKey(1) == ord('q'):  # press q to stop
                    raise KeyboardInterrupt()
            
        except KeyboardInterrupt:
            print('stopping camera...')
        finally:
            video_capture.release()
            cv2.destroyAllWindows()
    
    def __handle_bboxes(
        self,
        debug: bool,
        frame: np.ndarray,
        bboxes: list,
        frame_width: int,
        frame_height: int,
        left_padding: int,
        top_padding: int,
        right_padding: int,
        bottom_padding: int,
    ):
        # copy frame into another variable to avoid modifying it
        if debug:
            debug_frame = frame

        for bbox in bboxes:
            confidence = bbox[-1]
            bbox = tuple(map(int, bbox[:4]))  # convert to int

            if bbox[2] <= bbox[0] or bbox[3] <= bbox[1]:
                continue  # empty bbox

            bbox = (
                max(bbox[0] - left_padding, 0),
                max(bbox[1] - top_padding, 0),
                min(bbox[2] + right_padding, frame_width),
                min(bbox[3] + bottom_padding, frame_height),
            )

            face = frame[bbox[1]: bbox[3], bbox[0]: bbox[2]]
            gender, gender_score = self.gender_classifier.predict(face)

            self.__handle_gender(frame=frame, gender=gender)

            if debug:
                debug_frame = cv2.rectangle(
                    frame,
                    (bbox[0], bbox[1]),  # top-left point
                    (bbox[2], bbox[3]),  # bottom-right point
                    color=(0, 255, 0),  # green
                    thickness=2,
                )

                debug_frame = cv2.putText(
                    debug_frame,
                    f'{gender} {confidence:.2f} {gender_score:.2f}',
                    (bbox[0], bbox[1] + 25),  # top-left point
                    fontFace=cv2.FONT_HERSHEY_SIMPLEX,
                    fontScale=1,
                    color=(0, 255, 0),  # green
                    thickness=2,
                )
        
        if debug:
            cv2.imshow('camera', debug_frame)

    def __handle_gender(
        self,
        frame: np.ndarray,
        gender: int,
    ):
        if gender != self.toilet_gender:
            new_violation_time = time.time()

            # at least n seconds interval between each notification
            if new_violation_time - self.last_violation_time >= self.notification_interval:
                self.last_violation_time = new_violation_time

                print(
                    f'sending notification regarding violation: '
                    f'{self.gender_map[gender]} in '
                    f'{self.gender_map[self.toilet_gender]} toilet'
                )
                self.notifier.notify(
                    violation_info={
                        'detectedGender': gender,
                        'locationGender': self.toilet_gender,
                        'location': self.toilet_location,
                        'timestamp': new_violation_time,
                        'isTrue': True,
                        'verifiedBy': None,
                    },
                    image=frame,
                )

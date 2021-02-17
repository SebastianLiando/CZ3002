import cv2
import time

from face_detection.lffd import LFFD
from gender_recognition.ssrnet import SSRNet
from violation_handling.notifier import Notifier


class Camera:
    def __init__(
        self,
        toilet_gender: int,
        toilet_location: str,
        notification_interval: int,
        face_detector: LFFD,
        gender_recogniser: SSRNet,
        notifier: Notifier,
    ):
        self.gender_map = {1: 'male', 0: 'female'}

        self.toilet_gender = toilet_gender
        self.toilet_location = toilet_location
        self.notification_interval = notification_interval
        self.face_detector = face_detector
        self.gender_recogniser = gender_recogniser
        self.notifier = notifier

        # can notify immediately
        self.last_violation_time = time.time() - notification_interval

    def run(self, args):
        video_capture = cv2.VideoCapture(0)  # use web cam

        if not video_capture.isOpened():
            print('failed to open camera')
            return
        
        # TODO: refactor
        top_margin = 50
        bottom_margin = 30
        horizontal_margin = 20

        try:
            while True:
                read_success, frame = video_capture.read()

                if not read_success:
                    print('failed to read next frame from camera')
                    break

                bboxes = self.face_detector.predict(
                    frame,
                    resize_scale=1,
                    score_threshold=0.8,
                    top_k=5,
                    NMS_threshold=0.4,
                    NMS_flag=True,
                    skip_scale_branch_list=[],
                )

                for bbox in bboxes:
                    confidence = bbox[-1]
                    bbox = tuple(map(int, bbox[:4]))  # convert to int

                    if bbox[2] <= bbox[0] or bbox[3] <= bbox[1]:
                        continue  # empty bbox

                    bbox = (
                        max(bbox[0] - horizontal_margin, 0),
                        max(bbox[1] - top_margin, 0),
                        min(bbox[2] + horizontal_margin, 640),
                        min(bbox[3] + bottom_margin, 480),
                    )

                    face = frame[bbox[1]: bbox[3], bbox[0]: bbox[2]]
                    gender, gender_score = self.gender_recogniser.predict(face)

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
                            f'{gender} {confidence:.2f} {gender_score:.2f}',
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

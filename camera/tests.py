'''
Check if individual components work as expected
'''
import cv2
import mxnet as mx
import unittest

from face_detection import LFFD
from gender_classification import SSRNet


lffd_symbol_file_path = 'face_detection/model/symbol_10_560_25L_8scales_v1_deploy.json'
lffd_model_file_path = 'face_detection/model/train_10_560_25L_8scales_v1_iter_1400000.params'

ssrnet_prefix = 'gender_classification/ssr2_imdb_gender/model'
ssrnet_epoch_num = 0

female_image_path = 'images/Malala_Yousafzai.jpg'
female_face_image_path = 'images/Malala_Yousafzai_face.jpg'

male_image_path = 'images/Edward_Snowden.jpg'
male_face_image_path = 'images/Edward_Snowden_face.jpg'


class TestLFFD(unittest.TestCase):

    def test_detect_female(self):
        print('Testing detection of a female face')

        face_detector = LFFD(
            symbol_file_path=lffd_symbol_file_path,
            model_file_path=lffd_model_file_path,
        )

        image = cv2.imread(female_image_path)

        bboxes = face_detector.predict(
            image,
            resize_scale=1,
            score_threshold=0.8,
            top_k=5,
            nms_threshold=0.4,
            nms_flag=True,
            skip_scale_branch_list=[],
        )

        self.assertEqual(len(bboxes), 1)

    def test_detect_male(self):
        print('Testing detection of a male face')

        face_detector = LFFD(
            symbol_file_path=lffd_symbol_file_path,
            model_file_path=lffd_model_file_path,
        )

        image = cv2.imread(male_image_path)

        bboxes = face_detector.predict(
            image,
            resize_scale=1,
            score_threshold=0.8,
            top_k=5,
            nms_threshold=0.4,
            nms_flag=True,
            skip_scale_branch_list=[],
        )

        self.assertEqual(len(bboxes), 1)

class TestSSRNet(unittest.TestCase):

    def test_classify_female(self):
        print('Testing gender classification of a female face')

        gender_classifier = SSRNet(
            prefix=ssrnet_prefix,
            epoch=ssrnet_epoch_num,
        )

        image = cv2.imread(female_face_image_path)
        gender, gender_score = gender_classifier.predict(image)

        self.assertEqual(gender, 0)
        self.assertLessEqual(gender_score, 0.1)

    def test_classify_male(self):
        print('Testing gender classification of a male face')

        gender_classifier = SSRNet(
            prefix=ssrnet_prefix,
            epoch=ssrnet_epoch_num,
        )

        image = cv2.imread(male_face_image_path)
        gender, gender_score = gender_classifier.predict(image)

        self.assertEqual(gender, 1)
        self.assertGreaterEqual(gender_score, 0.9)


if __name__ == '__main__':
    unittest.main()

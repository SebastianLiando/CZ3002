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

ssrnet_prefix = 'gender_recognition/ssr2_imdb_gender/model'
ssrnet_epoch_num = 0

full_image_path = 'images/800px-Malala_Yousafzai.jpg'
face_image_path = 'images/cropped-Malala_Yousafzai.jpg'


class TestLFFD(unittest.TestCase):
    
    def test_cpu_detect(self):
        face_detector = LFFD(
            context=mx.cpu(),
            symbol_file_path=lffd_symbol_file_path,
            model_file_path=lffd_model_file_path,
        )

        image = cv2.imread(full_image_path)

        bboxes = face_detector.predict(
            image,
            resize_scale=1,
            score_threshold=0.9,
            top_k=5,
            NMS_threshold=0.4,
            NMS_flag=True,
            skip_scale_branch_list=[],
        )

        self.assertEqual(len(bboxes), 1)


class TestSSRNet(unittest.TestCase):

    def test_cpu_recognise(self):
        gender_classifier = SSRNet(
            context=mx.cpu(),
            prefix=ssrnet_prefix,
            epoch=ssrnet_epoch_num,
        )

        image = cv2.imread(face_image_path)
        gender, gender_score = gender_classifier.predict(image)

        self.assertEqual(gender, 'female')
        self.assertLessEqual(gender_score, 0.1)


if __name__ == '__main__':
    unittest.main()

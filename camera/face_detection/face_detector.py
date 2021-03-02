'''
Wrapper for face detection model
'''
import cv2
import mxnet as mx
import numpy as np

from . import config


class FaceDetector:
    '''
    Interface for face detection model.

    Must implement the predict() method.  
    predict() must accept an image (np.ndarray) as argument.
    '''
    def __init__(self):
        raise NotImplementedError('NotImplementedError: attempted usage of abstract face detector')

    def predict(self, image: np.ndarray):
        raise NotImplementedError('NotImplementedError: attempted usage of abstract face detector')


# empty data batch class for dynamical properties
class DataBatch:
    pass


class LFFD(FaceDetector):
    '''
    Light and Fast Face Detector  
    super class: FaceDetector

    :param symbol_file_path: (str) path to model's symbol file  
    :param model_file_path: (str) path to file containing model's parameters  
    :param input_height: (int) input height of the model  
    :param input_width: (int) input width of the model  
    '''
    def __init__(
        self,
        symbol_file_path: str,
        model_file_path: str,
        input_height: int = 480,
        input_width: int = 640,
    ):
        self.context = mx.cpu()

        self.receptive_field_list = config.param_receptive_field_list
        self.receptive_field_stride = config.param_receptive_field_stride
        self.bbox_small_list = config.param_bbox_small_list
        self.bbox_large_list = config.param_bbox_large_list
        self.receptive_field_center_start = config.param_receptive_field_center_start
        self.num_output_scales = config.param_num_output_scales
        
        self.constant = [i / 2.0 for i in self.receptive_field_list]

        self.input_height = input_height
        self.input_width = input_width

        self.model = self.__load_model(symbol_file_path, model_file_path)

    def __load_model(self, symbol_file_path: str, model_file_path: str):
        # load symbol and parameters
        print('loading LFFD...')
        
        self.symbol_net = mx.symbol.load(symbol_file_path)

        data_name = 'data'
        data_name_shape = (
            data_name,
            (1, 3, self.input_height, self.input_width),
        )

        module = mx.module.Module(
            symbol=self.symbol_net,
            data_names=[data_name],
            label_names=None,
            context=self.context,
            work_load_list=None,
        )

        module.bind(data_shapes=[data_name_shape], for_training=False)

        save_dict = mx.nd.load(model_file_path)

        self.arg_name_arrays = {}
        self.arg_name_arrays['data'] = mx.nd.zeros(
            (1, 3, self.input_height, self.input_width),
            self.context,
        )

        self.aux_name_arrays = {}
        for k, v in save_dict.items():
            tp, name = k.split(':', 1)
            if tp == 'arg':
                self.arg_name_arrays.update({name: v.as_in_context(self.context)})
            if tp == 'aux':
                self.aux_name_arrays.update({name: v.as_in_context(self.context)})
        module.init_params(
            arg_params=self.arg_name_arrays,
            aux_params=self.aux_name_arrays,
            allow_missing=True,
        )

        print('loaded LFFD successfully\n')
        return module

    def __perform_nms(self, boxes, overlap_threshold):
        '''
        Non-Maximum Suppression
        '''
        if boxes.shape[0] == 0:
            return boxes

        # if the bounding boxes integers, convert them to floats --
        # this is important since we'll be doing a bunch of divisions
        if boxes.dtype != np.float32:
            boxes = boxes.astype(np.float32)

        # initialize the list of picked indices
        pick = []
        # grab the coordinates of the bounding boxes
        x1 = boxes[:, 0]
        y1 = boxes[:, 1]
        x2 = boxes[:, 2]
        y2 = boxes[:, 3]
        sc = boxes[:, 4]
        widths = x2 - x1
        heights = y2 - y1

        # compute the area of the bounding boxes and sort the bounding
        # boxes by the bottom-right y-coordinate of the bounding box
        area = heights * widths
        indices = np.argsort(sc)  # 从小到大排序

        # keep looping while some indices still remain in the indices list
        while len(indices) > 0:
            # grab the last index in the indices list and add the
            # index value to the list of picked indices
            last = len(indices) - 1
            index = indices[-1]
            pick.append(index)

            # compare secend highest score boxes
            xx1 = np.maximum(x1[index], x1[indices[:last]])
            yy1 = np.maximum(y1[index], y1[indices[:last]])
            xx2 = np.minimum(x2[index], x2[indices[:last]])
            yy2 = np.minimum(y2[index], y2[indices[:last]])

            # compute the width and height of the bounding box
            w = np.maximum(0, xx2 - xx1 + 1)
            h = np.maximum(0, yy2 - yy1 + 1)

            # compute the ratio of overlap
            overlap = (w * h) / area[indices[:last]]

            # delete all indices from the index list that have
            indices = np.delete(
                indices,
                np.concatenate(([last], np.where(overlap > overlap_threshold)[0])),
            )

        # return only the bounding boxes that were picked using the
        # integer data type
        return boxes[pick]

    def predict(
        self,
        image: np.ndarray,
        resize_scale: float = 1,
        score_threshold: float = 0.8,
        top_k: int = 100,
        nms_threshold: float = 0.3,
        nms_flag: bool = True,
        skip_scale_branch_list: list = [],
    ) -> list:
        '''
        Forward pass / inference

        :param image: (np.ndarray) input image  
        :param resize_scale: (float) TODO [default: 1]  
        :param score_threshold: (float) TODO [default: 0.8]  
        :param top_k: (int) TODO [default: 100]  
        :param nms_threshold: (float) TODO [default: 0.3]  
        :param nms_flag: (bool) whether to perform non-maximum suppression [default: True]  
        :param skip_scale_branch_list: (list) TODO [default: empty list]  
        :return: (list) a list of tuples, each tuple having the following float values [top_left_x, top_left_y, bottom_right_x, bottom_right_y, confidence]
        '''
        if image.ndim != 3 or image.shape[2] != 3:
            print('Only RGB images are supported.')
            return None

        bbox_collection = []

        shorter_side = min(image.shape[:2])
        if shorter_side * resize_scale < 128:
            resize_scale = float(128) / shorter_side

        input_image = cv2.resize(
            image,
            (0, 0),
            fx=resize_scale,
            fy=resize_scale,
        )

        input_image = input_image.astype(dtype=np.float32)
        input_image = input_image[:, :, :, np.newaxis]
        input_image = input_image.transpose([3, 2, 0, 1])

        data_batch = DataBatch()
        data_batch.data = [mx.ndarray.array(input_image, self.context)]
        
        self.model.forward(data_batch=data_batch, is_train=False)
        results = self.model.get_outputs()
        outputs = []
        for output in results:
            outputs.append(output.asnumpy())

        for i in range(self.num_output_scales):
            if i in skip_scale_branch_list:
                continue

            score_map = np.squeeze(outputs[i * 2], (0, 1))

            bbox_map = np.squeeze(outputs[i * 2 + 1], 0)

            RF_center_Xs = np.array([self.receptive_field_center_start[i] + self.receptive_field_stride[i] * x for x in range(score_map.shape[1])])
            RF_center_Xs_mat = np.tile(RF_center_Xs, [score_map.shape[0], 1])
            RF_center_Ys = np.array([self.receptive_field_center_start[i] + self.receptive_field_stride[i] * y for y in range(score_map.shape[0])])
            RF_center_Ys_mat = np.tile(RF_center_Ys, [score_map.shape[1], 1]).T

            x_lt_mat = RF_center_Xs_mat - bbox_map[0, :, :] * self.constant[i]
            y_lt_mat = RF_center_Ys_mat - bbox_map[1, :, :] * self.constant[i]
            x_rb_mat = RF_center_Xs_mat - bbox_map[2, :, :] * self.constant[i]
            y_rb_mat = RF_center_Ys_mat - bbox_map[3, :, :] * self.constant[i]

            x_lt_mat = x_lt_mat / resize_scale
            x_lt_mat[x_lt_mat < 0] = 0
            y_lt_mat = y_lt_mat / resize_scale
            y_lt_mat[y_lt_mat < 0] = 0
            x_rb_mat = x_rb_mat / resize_scale
            x_rb_mat[x_rb_mat > image.shape[1]] = image.shape[1]
            y_rb_mat = y_rb_mat / resize_scale
            y_rb_mat[y_rb_mat > image.shape[0]] = image.shape[0]

            select_index = np.where(score_map > score_threshold)
            for idx in range(select_index[0].size):
                bbox_collection.append((
                    x_lt_mat[select_index[0][idx], select_index[1][idx]],
                    y_lt_mat[select_index[0][idx], select_index[1][idx]],
                    x_rb_mat[select_index[0][idx], select_index[1][idx]],
                    y_rb_mat[select_index[0][idx], select_index[1][idx]],
                    score_map[select_index[0][idx], select_index[1][idx]]
                ))

        # for NMS
        bbox_collection = sorted(
            bbox_collection,
            key=lambda item: item[-1],
            reverse=True,
        )
        if len(bbox_collection) > top_k:
            bbox_collection = bbox_collection[0:top_k]
        bbox_collection_numpy = np.array(bbox_collection, dtype=np.float32)

        if nms_flag:
            final_bboxes = self.__perform_nms(bbox_collection_numpy, nms_threshold)
            final_bboxes_ = []
            for i in range(final_bboxes.shape[0]):
                final_bboxes_.append((final_bboxes[i, 0], final_bboxes[i, 1], final_bboxes[i, 2], final_bboxes[i, 3], final_bboxes[i, 4]))

            return final_bboxes_
        else:
            return bbox_collection_numpy

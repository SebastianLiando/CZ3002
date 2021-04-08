'''
Wrapper for gender recognition model
'''
import cv2
import mxnet as mx
import numpy as np


class GenderClassifier:
    '''
    Interface for gender classification model.

    Must implement the predict() method.  
    predict() must accept an image (np.ndarray) as argument.
    '''
    def __init__(self):
        raise NotImplementedError('NotImplementedError: attempted usage of abstract gender classifier')

    def predict(self, image: np.ndarray):
        raise NotImplementedError('NotImplementedError: attempted usage of abstract gender classifier')


class SSRNet(GenderClassifier):
    '''
    Soft Stagewise Regression Network

    :param prefix: (str) prefix of path to model  
    :param epoch: (int) training epoch at which model is saved  
    :param input_height: (int) input height of the model  
    :param input_width: (int) input width of the model  
    '''
    def __init__(
        self,
        prefix: str,
        epoch: int,
        input_height: int = 64,
        input_width: int = 64
    ):
        self.context = mx.cpu()

        self.input_height = input_height
        self.input_width = input_width

        self.model = self.__load_model(prefix, epoch)

    def __load_model(self, prefix: str, epoch: int):
        print('loading SSR-Net...')
        symbol, arg_params, aux_params = mx.model.load_checkpoint(prefix, epoch)

        all_layers = symbol.get_internals()
        symbol = all_layers['_mulscalar16_output']

        module = mx.mod.Module(
            symbol=symbol,
            data_names=('data', 'stage_num0', 'stage_num1', 'stage_num2'),context=self.context,
            label_names = None,
        )

        module.bind(data_shapes=[
            ('data', (1, 3, self.input_height, self.input_width)),
            ('stage_num0', (1, 3)),
            ('stage_num1', (1, 3)),
            ('stage_num2', (1, 3)),
        ])
        module.set_params(arg_params, aux_params)

        print('loaded SSR-Net successfully\n')
        return module

    def __preprocess_image(self, image: np.ndarray) -> mx.io.DataBatch:
        image = cv2.resize(image, (self.input_height, self.input_width))

        image = image[:, :, ::-1]
        image = np.transpose(image, (2, 0, 1))

        input_blob = np.expand_dims(image, axis=0)
        data = mx.nd.array(input_blob)

        return mx.io.DataBatch(data=(
            data,
            mx.nd.array([[0, 1, 2]]),
            mx.nd.array([[0, 1, 2]]),
            mx.nd.array([[0, 1, 2]]),
        ))

    
    def predict(self, image: np.ndarray) -> (int, float):
        '''
        Forward pass / inference

        :param image: (np.ndarray) input image  
        :return: (tuple) gender, score  
        '''
        data_batch = self.__preprocess_image(image)

        self.model.forward(data_batch, is_train=False)

        gender_score = float(self.model.get_outputs()[0].asnumpy()[0])
        gender = 1 if gender_score > 0.5 else 0

        return gender, gender_score

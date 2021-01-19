'''
Wrapper for gender recognition model
'''
import cv2
import mxnet as mx
import numpy as np


class SSRNet:
    '''
    Soft Stagewise Regression Network
    '''
    def __init__(
        self,
        context: mx.Context,
        prefix: str,
        epoch: int,
        input_height: int = 64,
        input_width: int = 64
    ):
        self.input_height = input_height
        self.input_width = input_width

        self.model = self.__load_model(context, prefix, epoch)

    def __load_model(self, context: mx.Context, prefix: str, epoch: int):
        print('loading SSR-Net...')
        symbol, arg_params, aux_params = mx.model.load_checkpoint(prefix, epoch)

        all_layers = symbol.get_internals()
        symbol = all_layers['_mulscalar16_output']

        module = mx.mod.Module(
            symbol=symbol,
            data_names=('data', 'stage_num0', 'stage_num1', 'stage_num2'),context=context,
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
    
    def predict(self, image: np.ndarray) -> str:
        '''
        Forward pass / inference

        :param image: (np.ndarray) input image

        :return: (str) 'male' or 'female'
        '''
        image = cv2.resize(image, (self.input_height, self.input_width))

        image = image[:, :, ::-1]
        image = np.transpose(image, (2, 0, 1))

        input_blob = np.expand_dims(image, axis=0)
        data = mx.nd.array(input_blob)
        data_batch = mx.io.DataBatch(data=(
            data,
            mx.nd.array([[0, 1, 2]]),
            mx.nd.array([[0, 1, 2]]),
            mx.nd.array([[0, 1, 2]]),
        ))

        self.model.forward(data_batch, is_train=False)
        gender = self.model.get_outputs()[0].asnumpy()

        return 'male' if gender[0] > 0.5 else 'female'

'''
Crop face for testing SSR-Net
'''
import cv2


def crop_face():
    image = cv2.imread('Edward_Snowden.jpg')
    image = image[100:530, 200:460]

    # cv2.imshow('face', image)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()

    success = cv2.imwrite('Edward_Snowden_face.jpg', image)
    if not success:
        print('failed to save cropped image')


if __name__ == '__main__':
    crop_face()

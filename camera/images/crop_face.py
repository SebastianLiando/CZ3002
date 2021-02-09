'''
Crop face for testing SSR-Net
'''
import cv2


def crop_face():
    image = cv2.imread('800px-Malala_Yousafzai.jpg')
    image = image[90:270, 290:430]

    # cv2.imshow('face', image)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()

    success = cv2.imwrite('cropped-Malala_Yousafzai.jpg', image)
    if not success:
        print('failed to save cropped image')


if __name__ == '__main__':
    crop_face()

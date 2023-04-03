from google.cloud import vision
import io


def detect_text(content):
    client = vision.ImageAnnotatorClient()
    image_buffer = io.BytesIO(content)
    image_bytes = image_buffer.read()
    image = vision.Image(content=content)

    response = client.text_detection(image=image)
    texts = response.text_annotations

    if response.error.message:
        raise Exception(
            '{}\nFor more info on error messages, check: '
            'https://cloud.google.com/apis/design/errors'.format(
                response.error.message))
    else:
        return texts

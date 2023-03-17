
from roboflow import Roboflow
from PIL import Image
import os

def analyze(image):
    rf = Roboflow(api_key="q6YVWAZVYbczbL2e1K4n")
    project = rf.workspace("rizzlabzz").project("gymbro")
    model = project.version(1).model

    # infer on a local image
    output = model.predict(image, confidence=40, overlap=30).json()
    label = output["predictions"][0]["class"]

    model.predict(image, confidence=40, overlap=30).save("prediction.jpg")
    image2 = Image.open("prediction.jpg")
    os.remove("prediction.jpg")
    image2.show()
    return label, image2

    

#analyze("chestpress2.JPG")

# infer on an image hosted elsewhere
# print(model.predict("URL_OF_YOUR_IMAGE", hosted=True, confidence=40, overlap=30).json())
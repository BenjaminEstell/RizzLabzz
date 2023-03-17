
from roboflow import Roboflow

def analyze(image):
    rf = Roboflow(api_key="q6YVWAZVYbczbL2e1K4n")
    project = rf.workspace("rizzlabzz").project("gymbro")
    model = project.version(1).model

    # infer on a local image
    print(model.predict(image, confidence=40, overlap=30).json())

# visualize your prediction
# model.predict("your_image.jpg", confidence=40, overlap=30).save("prediction.jpg")

# infer on an image hosted elsewhere
# print(model.predict("URL_OF_YOUR_IMAGE", hosted=True, confidence=40, overlap=30).json())
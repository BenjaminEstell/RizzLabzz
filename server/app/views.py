from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
from django.core.files.storage import FileSystemStorage
import json
import os
import time
from app import ocr

# Create your views here.
@csrf_exempt
def postmachine(request):
        if request.method != 'POST':
                return HttpResponse(status=404)

        table = request.POST.get("table")
        name = request.POST.get("machine-type")
        instructions = request.POST.get("machine-instructions")
        muscles = request.POST.get("muscles")

        if request.FILES.get("machine-gif"):
                content = request.FILES['machine-gif']
                filename = str(time.time()) + ".gif"
                fs = FileSystemStorage()
                filename = fs.save(filename, content)
                gifurl = fs.url(filename)
        else:
                gifurl = None

        if request.FILES.get("muscle-image"):
                content = request.FILES['muscle-image']
                filename = str(time.time()) + ".jpeg"
                fs = FileSystemStorage()
                filename = fs.save(filename, content)
                imageurl = fs.url(filename)
        else:
                imageurl = None

        cursor = connection.cursor()
        if table == "titanfitness":
                cursor.execute('INSERT INTO titanfitness (name, instructions, machineurl, muscles, muscleurl) VALUES '
                '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        elif table == "cybex":
                cursor.execute('INSERT INTO cybex (name, instructions, machineurl, muscles, muscleurl) VALUES '
                '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        elif table == "lifefitness":
                cursor.execute('INSERT INTO lifefitness (name, instructions, machineurl, muscles, muscleurl) VALUES '
               '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        elif table == "matrix":
                cursor.execute('INSERT INTO matrix (name, instructions, machineurl, muscles, muscleurl) VALUES '
                '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        elif table == "hammerstrength":
                cursor.execute('INSERT INTO hammerstrength (name, instructions, machineurl, muscles, muscleurl) VALUES '
                '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        elif table == "generic":
                cursor.execute('INSERT INTO generic (name, instructions, machineurl, muscles, muscleurl) VALUES '
                '(%s, %s, %s, %s, %s);', (name, instructions, gifurl, muscles, imageurl))
                return HttpResponse(status=200)
        else:
                return HttpResponse(status=400)

def related(request):
        if request.method != 'GET':
                return HttpResponse(status=404)
        return HttpResponse(status=400)

@csrf_exempt
def getmachine(request, label):
        if request.method != 'POST':
                return HttpResponse(status=404)
        if label == "none" or label == "":
                return HttpResponse(status=505)
        manufacturer = "generic"
        texts = []
        texts_resp = []
        if request.FILES.get("image"):
                image = request.FILES['image'].read()
                # send image to text detection network
                texts = ocr.detect_text(image)
                for text in texts:
                        texts_resp.append(text.description.lower())
                # texts_resp is a list of strings found in the image
                # determine manufacturer from this list of strings
                # get manufacturer back from function
                brand_list = ["titan", "cybex", "scybex", "life", "hammer", "matrix", "titanfitness", "lifefitness", "hammerstrength"]
                brands = set(brand_list)
                for word in texts_resp:
                        if word in brands:
                                manufacturer = word
                                break
                if manufacturer == "life":
                        manufacturer = "lifefitness"
                elif manufacturer == "titan":
                        manufacturer = "titanfitness"
                elif manufacturer == "hammer":
                        manufacturer = "hammerstrength"
                elif manufacturer == "scybex":
                        manufacturer = "cybex"
        cursor = connection.cursor()
        if manufacturer == "titanfitness":
       	        cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM titanfitness WHERE name=%s', (label,))
        elif manufacturer == "cybex":
                cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM cybex WHERE name=%s', (label,))
        elif manufacturer == "lifefitness":
                cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM lifefitness WHERE name=%s', (label,))
        elif manufacturer == "matrix":
                cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM matrix WHERE name=%s', (label,))
        elif manufacturer == "hammerstrength":
                cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM hammerstrength WHERE name=%s', (label,))
        else:
                cursor.execute('SELECT name, instructions, machineurl, muscles, muscleurl FROM generic WHERE name=%s', (label,))
        data = cursor.fetchone()
        response = {}
        response['machine-info'] = data
        return JsonResponse(response, safe=False)

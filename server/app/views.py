from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
from django.core.files.storage import FileSystemStorage
import json
import os
import time

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

def getmachine(request, label):
	if request.method != 'GET':
		return HttpResponse(status=404)
	if label == "none" or label == "":
		return HttpResponse(status=505)
	cursor = connection.cursor()
	cursor.execute('SELECT name, instructions, machineurl FROM generic WHERE name = %s', (label,))
	data = cursor.fetchone()
	response = {}
	response['machine-info'] = data
	return JsonResponse(response)

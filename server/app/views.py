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

def machine(request):
	if request.method == 'GET':
		return getmachine(request)
	elif request.method == 'POST':
		return postmachine(request)
	else:
		return HttpResponse(status=404)

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
		filename = username + str(time.time()) + ".gif"
		fs = FileSystemStorage()
		filename = fs.save(filename, content)
		gifurl = fs.url(filename)
	else:
		gifurl = None
	
	if request.FILES.get("muscle-image"):
		content = request.FILES['muscle-image']
		filename = username + str(time.time()) + ".jpeg"
		fs = FileSystemStorage()
		filename = fs.save(filename, content)
		imageurl = fs.url(filename)
	else:
		imageurl = None

	cursor = connection.cursor()
	cursor.execute('INSERT INTO %s (name, instructions, machineurl, muscles, muscleurl) VALUES '
		'(%s, %s, %s, %s, %s);', (table, name, instructions, gifurl, muscles, imageurl))
	return JsonResponse(status=200)

def related(request):
	if request.method != 'GET':
		return HttpResponse(status=404)
	return HttpResponse(status=400)

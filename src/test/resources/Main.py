from pygame import *
import pygame
from Picture import Picture, add_noise
from random import randint
from time import sleep

PIXEL_SIZE = 10
DISPLAY = [PIXEL_SIZE*32, PIXEL_SIZE*32]


def get256_2(n):
    return n/256, n%256


def get256_4(n):
    return get256_2(n/256**2)+get256_2(n%256**2)


def generate_pics(folders, number):
    files = {}
    for folder in range(len(folders)):
        files[folder] = []
        for n in range(number):
            files[folder].append('pictures/samples/'+folders[folder]+'/0'+str(n+1)+'.txt')
    models = {}
    for a in range(len(folders)):
        models[a] = []
        for file in files[a]:
            p = Picture()
            p.load(file)
            models[a].append(p)
            p = Picture()
            p.load(file)
            p.mirror()
            models[a].append(p)
    return models


def test():
    f = open(folder+'20x1/train-images.idx3-ubyte')
    t = f.read()
    f.close()
    pygame.init()
    screen = pygame.display.set_mode(DISPLAY)
    pygame.display.set_caption("Pictures")
    t = t[16:]
    for i in range(512):
        rc = t[:1024]
        t = t[1024:]
        for y in range(32):
            for x in range(32):
                r = Rect(x*PIXEL_SIZE, y*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE)
                clr = Color(255-ord(rc[y*32+x]), 255-ord(rc[y*32+x]), 255-ord(rc[y*32+x]))
                pygame.draw.rect(screen, clr, r)
        pygame.display.update()
        sleep(0.1)


def get_set(folder, iterations, repeats, learning=True):
    pygame.init()
    screen = pygame.display.set_mode(DISPLAY)
    pygame.display.set_caption("Pictures")
    background = Surface(DISPLAY)
    background.fill(Color("#000000"))
    p = Picture()
    p.load('pictures/samples/car/01.txt')
    raw_set = generate_pics(['bird', 'cactus', 'car', 'cup', 'helicopter', 'loco', 'octopus', 'tree'], 8)
    #getting learning set
    pics_text = ''
    labels_text = ''
    for label in raw_set:
        for img in raw_set[label]:
            p = img
            for r in range(repeats):
                pic = Picture(p.body)
                for i in range(iterations):
                    pic = add_noise(pic, 20)
                    pic = add_noise(pic, -20)
                pics_text += str(pic)
                labels_text += chr(label+1)
                pic.show(screen, PIXEL_SIZE)
                pygame.display.update()
    p_txt, l_txt = '', ''
    if learning:
        p_name = 'train-images.idx3-ubyte'
        magic_number = get256_4(2051)
        for i in magic_number:
            p_txt += chr(i)
        number = get256_4(128 * repeats)
        for i in number:
            p_txt += chr(i)
        p_txt += chr(0)+chr(0)+chr(0)+chr(32)
        p_txt += chr(0)+chr(0)+chr(0)+chr(32)
        p_txt += pics_text
        f = open(folder+'/'+p_name, 'w')
        f.write(p_txt)
        f.close()
        l_name = 'train-labels.idx1-ubyte'
        magic_number = get256_4(2049)
        for i in magic_number:
            l_txt += chr(i)
        number = get256_4(128 * repeats)
        for i in number:
            l_txt += chr(i)
        l_txt += labels_text
        f = open(folder+'/'+l_name, 'w')
        f.write(l_txt)
        f.close()
    else:
        p_name = 't10k-images.idx3-ubyte'
        magic_number = get256_4(2051)
        for i in magic_number:
            p_txt += chr(i)
        number = get256_4(128 * repeats)
        for i in number:
            p_txt += chr(i)
        p_txt += chr(0)+chr(0)+chr(0)+chr(32)
        p_txt += chr(0)+chr(0)+chr(0)+chr(32)
        p_txt += pics_text
        f = open(folder+'/'+p_name, 'w')
        f.write(p_txt)
        f.close()
        l_name = 't10k-labels.idx1-ubyte'
        magic_number = get256_4(2049)
        for i in magic_number:
            l_txt += chr(i)
        number = get256_4(128 * repeats)
        for i in number:
            l_txt += chr(i)
        l_txt += labels_text
        f = open(folder+'/'+l_name, 'w')
        f.write(l_txt)
        f.close()

folder = 'pictures/packages/'
get_set(folder+'20x1', 1, 4)
get_set(folder+'20x1', 1, 1, False)
test()
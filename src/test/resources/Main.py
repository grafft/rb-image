from pygame import *
import pygame
from Picture import Picture, add_noise
import struct

PIXEL_SIZE = 10
DISPLAY = [PIXEL_SIZE*32, PIXEL_SIZE*32]

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
    f = open(folder+'20x1/train-images.idx3-ubyte', 'rb')
    print "number:", struct.unpack('>i', f.read(4))[0]
    print struct.unpack('>i', f.read(4))[0]
    print struct.unpack('>i', f.read(4))[0]

    t = f.read()
    f.close()
    pygame.init()
    screen = pygame.display.set_mode(DISPLAY)
    pygame.display.set_caption("Pictures")
    for i in range(1):
        rc = t[:1024]
        t = t[1024:]
        for y in range(32):
            for x in range(32):
                val = struct.unpack_from('B', rc[y*32+x])[0]
                r = Rect(x*PIXEL_SIZE, y*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE)
                clr = Color(255-val, 255-val, 255-val)
                pygame.draw.rect(screen, clr, r)
                print val,
            print
        pygame.display.update()


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

    if learning:
        p_name = 'train-images.idx3-ubyte'
        l_name = 'train-labels.idx1-ubyte'
    else:
        p_name = 't10k-images.idx3-ubyte'
        l_name = 't10k-labels.idx1-ubyte'
    p_f = open(folder+'/'+p_name, 'wb')
    l_f = open(folder+'/'+l_name, 'wb')

    l_f.write(struct.pack('>i', 128*repeats))

    p_f.write(struct.pack('>i', 128*repeats))
    p_f.write(struct.pack('>i', 32))
    p_f.write(struct.pack('>i', 32))

    for label in raw_set:
        for img in raw_set[label]:
            p = img
            for r in range(repeats):
                pic = Picture(p.body)
                for i in range(iterations):
                    pic = add_noise(pic, 20)
                    pic = add_noise(pic, -20)
                for row in pic.body:
                    for i in row:
                        p_f.write(struct.pack('B', i))
                l_f.write(struct.pack('B', label+1))
                pic.show(screen, PIXEL_SIZE)
                pygame.display.update()

    p_f.close()
    l_f.close()

folder = 'pictures/packages/'
get_set(folder+'20x1', 1, 4)
get_set(folder+'20x1', 1, 1, False)
#test()
from random import randint
from pygame import *
import pygame

class Picture():
    def __init__(self, body=[]):
        if body:
            self.body = [[body[j][i] for i in range(32)] for j in range(32)]
        else:
            self.body = [[255 for i in range(32)] for j in range(32)]

    def load(self, path):
        f = open(path)
        raw = f.read().split('\n')
        self.body = [[255*(int(raw[j][i])) for i in range(32)] for j in range(32)]
        f.close()

    def get_moved_left(self):
        new_body = [[255 for i in range(32)] for j in range(32)]
        for y in range(32):
            for x in range(0, 31):
                new_body[y][x] = self.body[y][x+1]
        return Picture(new_body)

    def get_moved_right(self):
        new_body = [[255 for i in range(32)] for j in range(32)]
        for y in range(32):
            for x in range(1, 32):
                new_body[y][x] = self.body[y][x-1]
        return Picture(new_body)

    def get_moved_up(self):
        new_body = [[255 for i in range(32)] for j in range(32)]
        for y in range(31):
            for x in range(32):
                new_body[y][x] = self.body[y+1][x]
        return Picture(new_body)

    def get_moved_down(self):
        new_body = [[255 for i in range(32)] for j in range(32)]
        for y in range(1, 32):
            for x in range(32):
                new_body[y][x] = self.body[y-1][x]
        return Picture(new_body)

    def mirror(self):
        new_body = [[255 for i in range(32)] for j in range(32)]
        for y in range(32):
            for x in range(32):
                new_body[y][x] = self.body[y][31-x]
        self.body = new_body

    def show(self, screen, size):
        for y in range(32):
            for x in range(32):
                rect = pygame.Rect(x*size, y*size, size, size)
                pygame.draw.rect(screen, Color("#"+get_double_16(self.body[y][x])+get_double_16(self.body[y][x])+get_double_16(self.body[y][x])), rect)

    def __str__(self):
        output = ''
        for row in self.body:
            for i in row:
                output += chr(i)
        return output


def get16(i):
    if i < 10:
        return str(i)
    else:
        alf = 'ABCDEF'
        return alf[i-10]


def get_double_16(n):
    if n >= 256:
        print n, '> 255'
        raise ValueError
    n = 255-n
    a = n / 16
    b = n % 16
    return get16(a) + get16(b)


def add_noise(pic, strength):
    new_pic = Picture([[pic.body[j][i] for i in range(32)] for j in range(32)])
    if strength>0:
        noise = [[randint(0, strength) for i in range(32)] for j in range(32)]
    elif strength<0:
        noise = [[randint(strength, 0) for i in range(32)] for j in range(32)]
    for i in range(32):
        for j in range(32):
            if strength>0:
                new_pic.body[j][i] = min(new_pic.body[j][i] + noise[i][j], 255)
            elif strength<0:
                new_pic.body[j][i] = max(new_pic.body[j][i] + noise[i][j], 0)
    return new_pic

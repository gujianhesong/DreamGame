# Generate TinySoundFont example minimal SF2 (484 bytes)
TEN0 = [0] * 10

def c(*xs):
    return list(xs)

data = []
data += c(ord('R'), ord('I'), ord('F'), ord('F'), 220, 1, 0, 0, ord('s'), ord('f'), ord('b'), ord('k'))
data += c(ord('L'), ord('I'), ord('S'), ord('T'), 88, 1, 0, 0, ord('p'), ord('d'), ord('t'), ord('a'))
data += c(ord('p'), ord('h'), ord('d'), ord('r'), 76)
data += TEN0 + TEN0 + TEN0 + TEN0
data += c(0, 0, 0, 0) + TEN0
data += c(0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 255, 0, 1) + TEN0
data += c(0, 0, 0)
data += c(ord('p'), ord('b'), ord('a'), ord('g'), 8, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0)
data += c(ord('p'), ord('m'), ord('o'), ord('d'), 10) + TEN0
data += c(0, 0, 0)
data += c(ord('p'), ord('g'), ord('e'), ord('n'), 8, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0)
data += c(ord('i'), ord('n'), ord('s'), ord('t'), 44) + TEN0 + TEN0
data += c(0, 0, 0, 0, 0, 0, 0, 0, 0, 0) + TEN0
data += c(0, 0, 0, 0, 0, 0, 0, 0, 1, 0)
data += c(ord('i'), ord('b'), ord('a'), ord('g'), 8, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0)
data += c(ord('i'), ord('m'), ord('o'), ord('d'), 10) + TEN0
data += c(0, 0, 0)
data += c(ord('i'), ord('g'), ord('e'), ord('n'), 12, 0, 0, 0, 54, 0, 1, 0, 53, 0, 0, 0, 0, 0, 0, 0)
data += c(ord('s'), ord('h'), ord('d'), ord('r'), 92) + TEN0 + TEN0
data += c(0, 0, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0)
data += c(34, 86, 0, 0, 60, 0, 0, 0, 1) + TEN0 + TEN0 + TEN0 + TEN0
data += c(0, 0, 0, 0, 0, 0, 0)
data += c(ord('L'), ord('I'), ord('S'), ord('T'), 112, 0, 0, 0, ord('s'), ord('d'), ord('t'), ord('a'))
data += c(ord('s'), ord('m'), ord('p'), ord('l'), 100, 0, 0, 0)
data += [86, 0, 119, 3, 31, 7, 147, 10, 43, 14, 169, 17, 58, 21, 189, 24, 73, 28, 204, 31, 73, 35, 249, 38, 46, 42, 71, 46, 250, 48, 150, 53, 242, 55, 126, 60, 151, 63, 108, 66, 126, 72, 207]
data += [70, 86, 83, 100, 72, 74, 100, 163, 39, 241, 163, 59, 175, 59, 179, 9, 179, 134, 187, 6, 186, 2, 194, 5, 194, 15, 200, 6, 202, 96, 206, 159, 209, 35, 213, 213, 216, 45, 220, 221, 223, 76, 227, 221, 230, 91, 234, 242, 237, 105, 241, 8, 245, 118, 248, 32, 252]

out = r"app\src\main\assets\keyboard\builtin_min.sf2"
with open(out, "wb") as f:
    f.write(bytes(data))
print(len(data), out)
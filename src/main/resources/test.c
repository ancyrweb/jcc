int main() {
    int a = 1 + ((2 + 4) / 3) + square(4 * 5);
    int *b = &a;
    a = *b + 1;
}

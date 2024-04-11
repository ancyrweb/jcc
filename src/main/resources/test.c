int main() {
    int a = 1 + ((2 + 4) / 3) + (4 * 5);
    int *b = &a;
    a = *b + 1;
}
